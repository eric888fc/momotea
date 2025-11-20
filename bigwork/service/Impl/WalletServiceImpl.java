package demo.bigwork.service.Impl;

import demo.bigwork.dao.BankAccountDAO;
import demo.bigwork.dao.WalletDAO;
import demo.bigwork.dao.WalletTransactionDAO;
import demo.bigwork.model.enums.TransactionType;
import demo.bigwork.model.po.BankAccountPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.po.WalletPO;
import demo.bigwork.model.po.WalletTransactionPO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {

	private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

	private final WalletDAO walletDAO;
	private final AuthHelperService authHelperService;
	private final WalletTransactionDAO walletTransactionDAO;
	private final BankAccountDAO bankAccountDAO;

	@Autowired
    public WalletServiceImpl(WalletDAO walletDAO, AuthHelperService authHelperService,
                             WalletTransactionDAO walletTransactionDAO, 
                             BankAccountDAO bankAccountDAO) { // <-- 新增
        this.walletDAO = walletDAO;
        this.authHelperService = authHelperService;
        this.walletTransactionDAO = walletTransactionDAO;
        this.bankAccountDAO = bankAccountDAO; // <-- 新增
    }

	/**
	 * (私有輔助方法) 取得當前使用者的錢包
	 */
	private WalletPO getWalletForCurrentUser() throws EntityNotFoundException {
		// 1. 取得登入者
		UserPO currentUser = authHelperService.getCurrentAuthenticatedUser();

		// 2. 查詢錢包
		return walletDAO.findByUser_UserId(currentUser.getUserId())
				.orElseThrow(() -> new EntityNotFoundException("找不到使用者的錢包: " + currentUser.getEmail()));
	}

	@Override
	@Transactional(readOnly = true) // 查詢
	public WalletPO getMyWallet() throws EntityNotFoundException {
		logger.info("正在查詢 {} 的錢包", authHelperService.getCurrentAuthenticatedUser().getEmail());
		return getWalletForCurrentUser();
	}

	/**
     * (關鍵 - 修改)
     * 我們將在這個方法中「同時」更新餘額並「新增」交易紀錄
     *
     * @Transactional 標籤會確保這「兩次」資料庫寫入
     * 要嘛同時成功、要嘛同時失敗
     */
    @Override
    @Transactional // (這個標籤至關重要)
    public WalletPO topUpMyWallet(BigDecimal amount) throws EntityNotFoundException {
        // 1.取得錢包
        WalletPO wallet = getWalletForCurrentUser();
        
        // 2.執行加值
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        
        // 3.儲存「錢包」的變更 (UPDATE wallets ...)
        WalletPO savedWallet = walletDAO.save(wallet);
        
        // 4. (新) 建立「交易紀錄」
        // (注意：amount 是正數，代表「入帳」)
        WalletTransactionPO transaction = new WalletTransactionPO(
                savedWallet,          // (關聯)
                TransactionType.TOPUP, // (類型)
                amount                 // (金額)
        );
        
        // 5. (新) 儲存「交易紀錄」 (INSERT INTO wallet_transactions ...)
        walletTransactionDAO.save(transaction);
        
        logger.info("使用者 {} 儲值 {} 成功，新餘額: {} (交易 ID: {})", 
                wallet.getUser().getEmail(), amount, newBalance, transaction.getTxId());
        
        // 6. (不變) 回傳更新後的錢包
        return savedWallet;
    }
    @Override
    @Transactional // (S極度重要) 提款必須是「交易」
    public WalletPO withdrawFromMyWallet(BigDecimal amount) throws Exception {
        
        // 1. (安全) 驗證「角色」
        // (注意) 我們呼叫的是 .getCurrentAuthenticatedSeller()
        // 這會自動拒絕 BUYER
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (關鍵 - "連接" 1) 驗證「收款帳戶」
        // 檢查賣家是否已設定收款帳戶
        Optional<BankAccountPO> accountOpt = bankAccountDAO.findByUser_UserId(seller.getUserId());
        if (accountOpt.isEmpty()) {
            logger.warn("提款失敗：賣家 {} 尚未設定收款帳戶", seller.getEmail());
            throw new Exception("提款失敗：請先至「賣家中心」設定您的收款銀行帳戶");
        }

        // 3. (業務) 取得錢包
        // (我們重用這個輔助方法，但因為上面已驗證過，這裡只是取資料)
        WalletPO wallet = getWalletForCurrentUser();
        
        // 4. (關鍵 - "連接" 2) 驗證「餘額」
        // (使用 .compareTo() 比較 BigDecimal)
        // wallet.getBalance().compareTo(amount) < 0 
        // 代表 balance < amount
        if (wallet.getBalance().compareTo(amount) < 0) {
            logger.warn("提款失敗：賣家 {} 餘額不足 (餘額: {}, 提款: {})", 
                        seller.getEmail(), wallet.getBalance(), amount);
            throw new Exception("提款失敗：餘額不足");
        }

        // 5. (執行) 更新餘額 (使用 .subtract() )
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        
        // 6. (執行) 儲存「錢包」 (UPDATE wallets ...)
        WalletPO savedWallet = walletDAO.save(wallet);

        // 7. (執行) 建立「交易紀錄」
        // (關鍵) 提款 (出帳) 的 amount 必須是「負數」
        // 我們使用 amount.negate() 來取得負值
        WalletTransactionPO transaction = new WalletTransactionPO(
                savedWallet,
                TransactionType.WITHDRAWAL, // (類型)
                amount.negate()             // (金額：負數)
        );
        
        // 8. (執行) 儲存「交易紀錄」 (INSERT ...)
        walletTransactionDAO.save(transaction);
        
        logger.info("賣家 {} 提款 {} 成功，新餘額: {} (交易 ID: {})", 
                seller.getEmail(), amount, newBalance, transaction.getTxId());
        
        // 9. (完成) 回傳更新後的錢包
        return savedWallet;
    }
}
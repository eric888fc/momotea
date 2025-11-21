package demo.bigwork.service.Impl;

import demo.bigwork.dao.BankAccountDAO;
import demo.bigwork.model.po.BankAccountPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.BankAccountRequestVO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BankAccountServiceImpl implements BankAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);
    
    private final BankAccountDAO bankAccountDAO;
    private final AuthHelperService authHelperService; // (重用我們的安全輔助)

    @Autowired
    public BankAccountServiceImpl(BankAccountDAO bankAccountDAO, AuthHelperService authHelperService) {
        this.bankAccountDAO = bankAccountDAO;
        this.authHelperService = authHelperService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BankAccountPO> getMyBankAccount() throws AccessDeniedException {
        // 1. (安全) 取得「賣家」
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (業務) 查詢
        return bankAccountDAO.findByUser_UserId(seller.getUserId());
    }

    @Override
    @Transactional
    public BankAccountPO createOrUpdateMyBankAccount(BankAccountRequestVO requestVO) throws AccessDeniedException {
        // 1. (安全) 取得「賣家」
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (業務) 
        // 嘗試找出「現有的」帳戶
        // "orElse(new BankAccountPO())"
        // (如果找不到，就 new 一個新的 PO)
        BankAccountPO account = bankAccountDAO.findByUser_UserId(seller.getUserId())
                                      .orElse(new BankAccountPO());
        
        // 3. (組裝) 
        // 不論是 new 還是 old，都用 VO 的新資料覆蓋
        account.setUser(seller); // (關鍵) 確保關聯
        account.setBankName(requestVO.getBankName());
        account.setAccountHolderName(requestVO.getAccountHolderName());
        account.setAccountNumber(requestVO.getAccountNumber());
        
        // 4. (儲存)
        // (JPA 會自動判斷)
        // 如果 accountId 是 null -> 執行 INSERT
        // 如果 accountId 不是 null -> 執行 UPDATE
        logger.info("賣家 {} 正在更新收款帳戶", seller.getEmail());
        return bankAccountDAO.save(account);
    }
}
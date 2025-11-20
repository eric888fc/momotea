package demo.bigwork.service;

import demo.bigwork.model.po.BankAccountPO;
import demo.bigwork.model.vo.BankAccountRequestVO;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

public interface BankAccountService {
    
    /**
     * (新) 取得「當前登入賣家」的收款帳戶
     * (一個賣家只會有一個)
     *
     * @return Optional<BankAccountPO> (可能存在，也可能還沒設定)
     */
    Optional<BankAccountPO> getMyBankAccount() throws AccessDeniedException;

    /**
     * (新) 建立或更新「當前登入賣家」的收款帳戶
     *
     * @param requestVO 帳戶資料
     * @return 儲存後的 BankAccountPO
     */
    BankAccountPO createOrUpdateMyBankAccount(BankAccountRequestVO requestVO) throws AccessDeniedException;
}
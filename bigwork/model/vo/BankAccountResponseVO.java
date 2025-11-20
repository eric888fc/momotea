package demo.bigwork.model.vo;

import demo.bigwork.model.po.BankAccountPO;
import lombok.Data;

/**
 * (VO) 用於「回傳」收款帳戶資訊
 * (我們過濾掉 PO 中的 user 敏感資訊)
 */
@Data
public class BankAccountResponseVO {

    private Long accountId;
    private Long userId; // (只回傳 ID)
    private String bankName;
    private String accountHolderName;
    
    // (安全)
    // 我們「不」回傳完整的帳號
    // 我們只回傳「遮罩」過的帳號 (e.g., ****1234)
    private String accountNumberMasked;

    // (轉換器建構子)
    public BankAccountResponseVO(BankAccountPO po) {
        this.accountId = po.getAccountId();
        this.userId = po.getUser().getUserId();
        this.bankName = po.getBankName();
        this.accountHolderName = po.getAccountHolderName();
        
        // (關鍵 - 遮罩邏輯)
        this.accountNumberMasked = maskAccountNumber(po.getAccountNumber());
    }
    
    /**
     * (輔助) 遮罩帳號，只顯示末 4 碼
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        int length = accountNumber.length();
        return "****" + accountNumber.substring(length - 4);
    }
}
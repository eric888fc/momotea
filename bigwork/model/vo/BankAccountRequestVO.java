package demo.bigwork.model.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * (VO) 用於「新增」或「更新」收款帳戶
 * Controller 接收的 JSON 請求
 */
@Data
public class BankAccountRequestVO {

    @NotEmpty(message = "銀行名稱不可為空")
    @Size(max = 100)
    private String bankName;

    @NotEmpty(message = "戶名不可為空")
    @Size(max = 100)
    private String accountHolderName;

 // (★ 關鍵修改 ★)
    @NotEmpty(message = "銀行帳號不可為空")
    // 我們用 @Pattern 取代了 @Size
    // [0-9] 代表 0-9 的任一數字
    // {8}   代表「剛好 8 次」
    @Pattern(regexp = "[0-9]{8}", message = "銀行帳號必須為 8 位數字")
    private String accountNumber;
}
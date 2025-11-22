package demo.bigwork.model.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern; // (關鍵) 匯入 @Pattern
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 註冊請求 (Request) 的 JSON 格式
 * (我們在這裡加入 RegEx 驗證)
 */
@Data
public class RegisterRequestVO {
    
    @NotEmpty(message = "名稱不可為空")
    private String name;

    /**
     * @Email 標籤本身就是一個複雜的 RegEx，我們保留它
     */
    @NotEmpty(message = "Email 不可為空")
    @Email(message = "Email 格式錯誤")
    private String email;

    /**
     * (關鍵) 密碼的 RegEx
     * 我們不再只用 @Size，而是用 @Pattern
     * regexp = "...": 
     * (?=.*[A-Za-z]) - 必須至少包含一個英文字母
     * (?=.*\d) - 必須至少包含一個數字
     * .{8,} - 總長度至少 8 碼
     */
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", 
             message = "密碼格式錯誤：長度至少 8 碼，且必須同時包含英文和數字")
    private String password; // (你可以移除之前的 @NotEmpty 和 @Size)
    
    /**
     * (關鍵) 手機的 RegEx
     * 這是一個「選填」欄位，所以我們的 RegEx 必須允許「空字串」或「null」
     * regexp = "...":
     * ^$ - 允許空字串 (empty string)
     * | - 或者 (OR)
     * ^09\d{8}$ - 必須是 09 開頭，後面接著 8 個數字 (總共 10 碼)
     */
    @Pattern(regexp = "^$|^09\\d{8}$", 
             message = "手機號碼格式錯誤：必須為 09 開頭的 10 位數字")
    private String phone;
    
    /**
     * (關鍵) 驗證碼的 RegEx
     * 我們不再只用 @NotEmpty，而是精確要求「6 個數字」
     * regexp = "...":
     * ^\d{6}$ - 必須剛好是 6 個「數字」(0-9)
     */
    @Pattern(regexp = "^\\d{6}$", 
             message = "驗證碼格式錯誤：必須為 6 位數字")
    private String code; // (你可以移除之前的 @NotEmpty)

    // (選填) 地址也可以加上 @NotEmpty (如果它是必填的話)
    @NotEmpty(message = "地址不可為空")
    private String address; 
}
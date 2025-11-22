package demo.bigwork.model.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequestVO {
    
    @NotEmpty(message = "Token 不可為空")
    private String token;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", 
             message = "新密碼格式錯誤：長度至少 8 碼，且必須同時包含英文和數字")
    private String newPassword;
}
package demo.bigwork.model.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 登入請求 (Request) 的 JSON 格式
 */
@Data
public class LoginRequestVO {

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String password;
}
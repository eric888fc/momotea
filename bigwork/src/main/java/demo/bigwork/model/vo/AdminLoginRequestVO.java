package demo.bigwork.model.vo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 系統管理員登入請求
 */
@Data
public class AdminLoginRequestVO {

    @NotEmpty
    private String adminCode; // 管理員原編

    @NotEmpty
    private String password;  // 管理員密碼
}

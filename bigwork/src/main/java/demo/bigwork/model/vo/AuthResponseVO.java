package demo.bigwork.model.vo;

import demo.bigwork.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入與驗證相關的回傳物件
 * 會在 AuthController.login() / adminLogin() 使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor   // 會自動生成含所有欄位的建構子
public class AuthResponseVO {

    /** 提示訊息，例如：登入成功、管理員登入成功 */
    private String message;

    /** 使用者 ID */
    private Long userId;

    /** 使用者姓名 */
    private String name;

    /** 使用者 Email */
    private String email;

    /** 角色：BUYER / SELLER / ADMIN */
    private UserRole role;

    /** JWT Token */
    private String token;

    /** 管理員原編（只有 ADMIN 會有值，其它角色為 null） */
    private String adminCode;
}

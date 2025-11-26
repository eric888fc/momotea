package demo.bigwork.model.vo;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 後台「會員管理」列表用 VO
 */
@Data
public class AdminUserSummaryVO {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String defaultAddress;
    private Timestamp createdAt;
    private UserRole role;

    public AdminUserSummaryVO(UserPO po) {
        this.userId = po.getUserId();
        this.name = po.getName();
        this.email = po.getEmail();
        this.phone = po.getPhone();
        this.defaultAddress = po.getDefaultAddress();
        this.createdAt = po.getCreatedAt();
        this.role = po.getRole();
    }
}
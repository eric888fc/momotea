package demo.bigwork.model.vo;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;
import lombok.Data;

/**
 * (VO) 用於「回傳」個人資料
 * (過濾掉密碼等敏感資訊)
 */
@Data
public class ProfileResponseVO {
    
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private UserRole role;

    // (轉換器建構子)
    public ProfileResponseVO(UserPO user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.address = user.getDefaultAddress();
        this.role = user.getRole();
    }
}
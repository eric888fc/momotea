package demo.bigwork.model.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * (VO) 用於「更新」個人資料
 * Controller 接收的 JSON 請求
 * (注意：我們不允許在這裡更改 Email 或 Role)
 */
@Data
public class ProfileRequestVO {

    @NotEmpty(message = "姓名不可為空")
    @Size(max = 100)
    private String name;

    @NotEmpty(message = "電話不可為空")
    @Pattern(regexp = "^09\\d{8}$", message = "電話格式必須為 09 開頭的 10 位數字")
    private String phone;

    @NotEmpty(message = "地址不可為空")
    private String address;
}
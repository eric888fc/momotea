package demo.bigwork.model.vo;

import demo.bigwork.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // (Lombok) 自動生成全參數建構子
public class AuthResponseVO {
    
    private String message;
    private Long userId;
    private String name;
    private String email;
    private UserRole role;
    
    // (關鍵) 新增 "token" 欄位
    private String token; 
    
    // (教授提醒) 由於 Lombok 的 @AllArgsConstructor
    // 會自動更新，我們不需要手動改建構子。
    // 但如果沒有用 Lombok，你就必須手動更新建構子。
}
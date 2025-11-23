package demo.bigwork.model.po;

import java.sql.Timestamp;

// --- Lombok 標籤 (Annotation) ---
import lombok.Data; // 相當於 @Getter + @Setter + @ToString + @EqualsAndHashCode
import lombok.NoArgsConstructor; // 自動生成「無參數建構子」
import lombok.AllArgsConstructor; // 自動生成「全參數建構子」

// --- JPA 標籤 (Annotation) ---
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.hibernate.annotations.CreationTimestamp; // Hibernate 提供的自動時間戳

import demo.bigwork.model.enums.UserRole;

/**
 * PO (Persistence Object) - 使用 JPA 和 Lombok
 *
 * @Entity 告訴 Spring：這是一個「實體類別」，請 JPA (Hibernate) 來管理它。
 * @Table(name = "users") 將這個類別「明確指定」對應到資料庫的 "users" 資料表。
 * @Data (Lombok) 自動生成所有欄位的 getter/setter、toString() 等方法。
 * @NoArgsConstructor (Lombok) JPA 在讀取資料時需要一個無參數建構子。
 * @AllArgsConstructor (Lombok) 方便我們在測試或建立物件時使用。
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPO {

    /**
     * @Id 標記此欄位為 Primary Key (主鍵)。
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * 告訴 JPA 使用資料庫的「自動增長(AUTO_INCREMENT)」策略來生成 ID。
     * @Column(name = "user_id")
     * 將 Java 的 "userId" 屬性明確對應到資料庫的 "user_id" 欄位。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * @Column(name = "name", nullable = false, length = 100)
     * 對應 "name" 欄位，設定為不允許 null，長度 100。
     * (教授提醒：這有助於 ddl-auto=validate 進行驗證)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * @Column(name = "email", nullable = false, unique = true, length = 255)
     * 對應 "email" 欄位，設定為不允許 null、必須唯一。
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * @Column(name = "password", nullable = false, length = 255)
     * 對應 "password" 欄位 (儲存雜湊過的密碼)。
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * (新欄位) 角色
     * @Enumerated(EnumType.STRING) 
     * 告訴 JPA：將 Enum 轉換為「字串」 ("BUYER", "SELLER")
     * 儲存到資料庫的 VARCHAR 欄位中。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /**
     * @Column(name = "phone", length = 20)
     * 對應 "phone" 欄位 (允許 null)。
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * @Column(name = "default_address", length = 500)
     * 對應 "default_address" 欄位 (允許 null)。
     */
    @Column(name = "default_address", length = 500)
    private String defaultAddress;

    /**
     * @CreationTimestamp (Hibernate 特有功能)
     * 告訴 Hibernate：當「新增 (INSERT)」這筆資料時，
     * 自動將「當前時間」填入此欄位。
     * @Column(name = "created_at", nullable = false, updatable = false)
     * 對應 "created_at" 欄位，設定為不允許 null，且「禁止更新 (updatable = false)」。
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    // --- 教授提醒 ---
    // 你不需要寫任何 Getter / Setter / toString() / Constructor ...
    // Lombok 已經在背景幫你全部自動生成了！
    // 這就是現代化 Java 開發的威力。
}
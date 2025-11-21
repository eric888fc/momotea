package demo.bigwork.model.po;

import java.sql.Timestamp;

// --- Lombok 標籤 (Annotation) ---
import lombok.Data;               // 相當於 @Getter + @Setter + @ToString + @EqualsAndHashCode
import lombok.NoArgsConstructor;  // 自動生成「無參數建構子」
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

import org.hibernate.annotations.CreationTimestamp;

import demo.bigwork.model.enums.UserRole;

/**
 * PO (Persistence Object) - 使用 JPA 和 Lombok
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 使用 Enum 儲存角色：BUYER / SELLER / ADMIN
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /**
     * 管理員原編（管理員編號）
     * 只有 role = ADMIN 才會有值，其它角色可以是 null
     */
    @Column(name = "admin_code", length = 50, unique = true)
    private String adminCode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "default_address", length = 500)
    private String defaultAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
}


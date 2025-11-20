package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetTokenPO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    /**
     * @OneToOne (一對一)
     * fetch = FetchType.EAGER: 取得 Token 時，立刻載入 User
     * @JoinColumn: 指定外鍵欄位
     */
    @OneToOne(targetEntity = UserPO.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserPO user;

    @Column(nullable = false, name = "expiry_date")
    private Timestamp expiryDate;

    // (建構子) 方便 Service 使用
    public PasswordResetTokenPO(String token, UserPO user, Timestamp expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }
}
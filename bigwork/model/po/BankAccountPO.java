package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PO (Entity) - 對應 `bank_accounts` 收款帳戶資料表
 */
@Entity
@Table(name = "bank_accounts")
@Data
@NoArgsConstructor
public class BankAccountPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    /**
     * (關鍵 - 一對一)
     * * @OneToOne: 一個帳戶「屬於」一個使用者
     * @JoinColumn(name = "user_id"):
     * 告訴 JPA，這個關聯是透過 `user_id` 欄位來維護的
     *
     * (注意) 
     * 我們不需要在 UserPO 中加入 @OneToOne(mappedBy="user")
     * 除非你「非常」需要從 UserPO 反向查詢 BankAccountPO
     * 保持「單向」關聯更簡單
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPO user;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;
}
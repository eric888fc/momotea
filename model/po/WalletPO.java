package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * PO (Entity) - 對應 `wallets` 模擬錢包資料表
 */
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
public class WalletPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    /**
     * @OneToOne: 一個錢包「屬於」一個使用者
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPO user;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    // (方便 Service 使用的建構子)
    public WalletPO(UserPO user, BigDecimal initialBalance) {
        this.user = user;
        this.balance = initialBalance;
    }
}
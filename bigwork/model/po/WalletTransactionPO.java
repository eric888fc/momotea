package demo.bigwork.model.po;

import demo.bigwork.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
public class WalletTransactionPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long txId;

    /**
     * @ManyToOne: 多筆交易「屬於」一個錢包
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletPO wallet;

    /**
     * @Enumerated(EnumType.STRING): 
     * * 告訴 JPA 將 Enum 存為「字串」(e.g., "TOPUP")
     * * (預設是存為數字 0, 1, 2，可讀性差)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    /**
     * 金額 (正數=入帳, 負數=出帳)
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Timestamp timestamp;

    // (方便 Service 使用的建構子)
    public WalletTransactionPO(WalletPO wallet, TransactionType type, BigDecimal amount) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
    }
}
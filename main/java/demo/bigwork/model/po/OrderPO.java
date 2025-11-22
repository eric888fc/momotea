package demo.bigwork.model.po;

import demo.bigwork.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*; // (關鍵) 匯入
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
// (關鍵) 絕對不要用 @Data
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"buyer", "seller", "items"}) // (關鍵) 排除所有關聯
@EqualsAndHashCode(exclude = {"buyer", "seller", "items"}) // (關鍵) 排除所有關聯
public class OrderPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    /**
     * @ManyToOne: 多筆訂單「屬於」一個買家
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserPO buyer;

    /**
     * @ManyToOne: 多筆訂單「屬於」一個賣家
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserPO seller;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING) // (關鍵) 將 Enum 存為字串
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @CreationTimestamp // (關鍵) 自動填入建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    /**
     * @OneToMany: 一筆訂單「擁有多個」訂單項目
     * (關鍵) mappedBy = "order"
     * (關鍵) cascade = CascadeType.ALL (當儲存 OrderPO 時，自動儲存所有 items)
     */
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY, // (關鍵) 必須是 LAZY
        orphanRemoval = true
    )
    private Set<OrderItemPO> items = new HashSet<>();

    // (輔助方法) 
    // 當我們建立 OrderItemPO 時，
    // 我們會呼叫這個方法來「自動」設定雙向關聯
    public void addOrderItem(OrderItemPO item) {
        items.add(item);
        item.setOrder(this);
    }
}
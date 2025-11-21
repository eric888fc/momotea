package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.*; // (關鍵) 匯入
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "product_ratings")
// (關鍵) 絕對不要用 @Data
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"product", "buyer", "orderItem"}) // (關鍵) 排除所有關聯
@EqualsAndHashCode(exclude = {"product", "buyer", "orderItem"}) // (關鍵) 排除所有關聯
public class ProductRatingPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long ratingId;

    /**
     * @ManyToOne: 多筆評價「屬於」一個商品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductPO product;

    /**
     * @ManyToOne: 多筆評價「屬於」一個買家
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserPO buyer;

    /**
     * (關鍵)
     * @OneToOne: 一筆評價「綁定」一筆訂單明細
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItemPO orderItem;

    @Column(name = "rating_stars", nullable = false)
    private Integer ratingStars; // (e.g., 1-5)

    @Lob // (Lob = Large Object, 適用於 TEXT 欄位)
    @Column(name = "comment", columnDefinition = "TEXT") // (S關鍵) 明確指定型別
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
}
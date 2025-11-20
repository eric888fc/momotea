package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.*; // (關鍵) 匯入

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
// (關鍵) 絕對不要用 @Data
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"order", "product", "productRating"}) 
@EqualsAndHashCode(exclude = {"order", "product", "productRating"})
public class OrderItemPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    /**
     * @ManyToOne: 多筆項目「屬於」一筆訂單
     * (關鍵) 這是「擁有方 (Owning Side)」
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderPO order;

    /**
     * @ManyToOne: (多筆項目可以指向) 一個商品
     * (注意) @JoinColumn(nullable = true) 
     * * 允許商品 ID 為空 (因為我們在 SQL 中設定了 ON DELETE SET NULL)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true) 
    private ProductPO product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit; // (價格快照)
    
    @OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY) 
    private ProductRatingPO productRating;
}
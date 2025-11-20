package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // (關鍵) 匯入
import lombok.NoArgsConstructor;
import lombok.ToString; // (關鍵) 匯入
import lombok.Getter; // (關鍵) 匯入
import lombok.Setter; // (關鍵) 匯入

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
// @Data // (關鍵) 移除 @Data
@Getter // (關鍵) 手動加入
@Setter // (關鍵) 手動加入
@NoArgsConstructor
@ToString(exclude = {"cart", "product"}) // (關鍵) 排除關聯
@EqualsAndHashCode(exclude = {"cart", "product"}) // (關鍵) 排除關聯
public class CartItemPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartPO cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductPO product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
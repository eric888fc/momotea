package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // (關鍵) 匯入
import lombok.NoArgsConstructor;
import lombok.ToString; // (關鍵) 匯入
import lombok.Getter; // (關鍵) 匯入
import lombok.Setter; // (關鍵) 匯入

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
// @Data // (關鍵) 移除 @Data
@Getter // (關鍵) 手動加入
@Setter // (關鍵) 手動加入
@NoArgsConstructor
@ToString(exclude = {"user", "items"}) // (關鍵) 排除關聯，防止 toString 無限迴圈
@EqualsAndHashCode(exclude = {"user", "items"}) // (關鍵) 排除關聯，防止 hashCode 無限迴圈
public class CartPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPO user;

    @OneToMany(
        mappedBy = "cart",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY, // (關鍵) 必須是 LAZY
        orphanRemoval = true
    )
    private Set<CartItemPO> items = new HashSet<>();
    
    public CartPO(UserPO user) {
        this.user = user;
    }
}
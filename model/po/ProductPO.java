package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal; // (關鍵) 處理「金錢」
import java.sql.Timestamp;

/**
 * PO (Entity) - 對應 `products` 商品資料表
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class ProductPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    /**
     * (關鍵 - 關聯 1)
     * 這個商品「屬於」哪一個「賣家 (UserPO)」
     *
     * @ManyToOne: 多個商品 -> 一個賣家
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false) // 外鍵欄位
    private UserPO seller;

    /**
     * (關鍵 - 關聯 2)
     * 這個商品「屬於」哪一個「分類 (CategoryPO)」
     *
     * @ManyToOne: 多個商品 -> 一個分類
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // 外鍵欄位
    private CategoryPO category;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT") // (大文字)
    private String description;

    /**
     * (關鍵) 對於「金錢」，永遠使用 BigDecimal
     * * precision = 10: 總共 10 位數
     * scale = 2: 小數點後 2 位
     * (e.g., 12345678.99)
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl; // Lombok @Data 會自動生成 get/set

    /**
     * (方便) 讓 JPA 在「新增」時自動填入當前時間
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    // Lombok 會自動生成所有 Getters / Setters
}
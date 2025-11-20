package demo.bigwork.model.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PO (Entity) - 對應 `categories` 商品分類資料表
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
public class CategoryPO {

    /**
     * @Id: 主鍵
     * @GeneratedValue: 自動增長
     * @Column: 對應 "category_id" 欄位
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId; // (注意：我們設計的是 INT，不是 BIGINT)

    /**
     * @Column: 對應 "name" 欄位
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * (關鍵) 
     * 一個「父分類」ID
     * 這是一個「自我參考」的關聯
     *
     * @ManyToOne: 多個子分類 -> 一個父分類
     * fetch = FetchType.LAZY:
     * (效能優化) 只有在「明確」呼叫 getParentCategory() 時，
     * JPA 才去資料庫撈這個父分類的資料
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id") // 此外鍵欄位
    private CategoryPO parentCategory;

    // (教授提醒)
    // 我們也可以在這裡加入 @OneToMany
    // private Set<CategoryPO> subCategories;
    // 來表示「一個父分類有多個子分類」，但目前可以先省略
    
    // Lombok 的 @Data 會自動生成 Getters, Setters, toString ...
}
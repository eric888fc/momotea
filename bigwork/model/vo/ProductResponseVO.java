package demo.bigwork.model.vo;

import demo.bigwork.model.po.ProductPO;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * (VO) 用於「回傳」商品資訊的 JSON 格式
 */
@Data
public class ProductResponseVO {
    
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl; // 圖片路徑
    private Timestamp createdAt;
    
    // (關鍵) 我們只回傳 ID 和名稱，而不是整個 PO
    private Integer categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;

    /**
     * (輔助) 建立一個「轉換器」建構子
     * 讓我們可以輕鬆地從 PO 轉換到 VO
     */
    public ProductResponseVO(ProductPO po) {
        this.productId = po.getProductId();
        this.name = po.getName();
        this.description = po.getDescription();
        this.price = po.getPrice();
        this.stock = po.getStock();
        this.imageUrl = po.getImageUrl();
        this.createdAt = po.getCreatedAt();
        
        // (關鍵) 安全地取得關聯資料
        if (po.getCategory() != null) {
            this.categoryId = po.getCategory().getCategoryId();
            this.categoryName = po.getCategory().getName();
        }
        if (po.getSeller() != null) {
            this.sellerId = po.getSeller().getUserId();
            this.sellerName = po.getSeller().getName();
        }
    }
}
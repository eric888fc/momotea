package demo.bigwork.model.vo;

import demo.bigwork.model.po.ProductPO;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 後台會員管理 → 查看賣家「上架商品」列表用 VO
 */
@Data
public class AdminProductSummaryVO {

    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Timestamp createdAt;

    public AdminProductSummaryVO(ProductPO po) {
        this.productId = po.getProductId();
        this.name = po.getName();
        this.price = po.getPrice();
        this.stock = po.getStock();
        this.createdAt = po.getCreatedAt();
    }
}
package demo.bigwork.model.vo;

import demo.bigwork.model.po.ProductRatingPO;
import lombok.Data;

import java.sql.Timestamp;

/**
 * (VO) 輸出的「評價」
 */
@Data
public class RatingResponseVO {

    private Long ratingId;
    private Long productId;
    private Long buyerId;
    private String buyerName; // (我們顯示買家名稱，但不顯示 Email)
    private Integer ratingStars;
    private String comment;
    private Timestamp createdAt;
    private String productName;
    private String productImageUrl;

    /**
     * (ProductRatingPO -> VO 轉換器)
     * (我們會在 Service 層的 @Transactional 內部呼叫)
     */
    public RatingResponseVO(ProductRatingPO ratingPO) {
        this.ratingId = ratingPO.getRatingId();
        this.productId = ratingPO.getProduct().getProductId();
        this.buyerId = ratingPO.getBuyer().getUserId();
        this.buyerName = ratingPO.getBuyer().getName(); // (安全地取得關聯)
        this.ratingStars = ratingPO.getRatingStars();
        this.comment = ratingPO.getComment();
        this.createdAt = ratingPO.getCreatedAt();
        if (ratingPO.getProduct() != null) {
            this.productId = ratingPO.getProduct().getProductId();
            this.productName = ratingPO.getProduct().getName();
            this.productImageUrl = ratingPO.getProduct().getImageUrl();
        } else {
            this.productId = null;
            this.productName = "【商品已失效】";
            this.productImageUrl = null;
        }
    }
}
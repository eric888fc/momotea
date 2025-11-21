package demo.bigwork.dao;

import demo.bigwork.model.po.ProductRatingPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRatingDAO extends JpaRepository<ProductRatingPO, Long> {

    /**
     * (關鍵 - 安全) 
     * 檢查「某筆訂單明細」是否已被評價
     * "SELECT * FROM product_ratings WHERE order_item_id = ?"
     */
    Optional<ProductRatingPO> findByOrderItem_OrderItemId(Long orderItemId);

    /**
     * (查詢) 
     * 查詢「某個商品」的所有評價
     * "SELECT * FROM product_ratings WHERE product_id = ?"
     */
    List<ProductRatingPO> findByProduct_ProductId(Long productId);

    /**
     * (查詢) 
     * 查詢「某個買家」的所有評價
     * "SELECT * FROM product_ratings WHERE buyer_id = ?"
     */
    List<ProductRatingPO> findByBuyer_UserId(Long buyerId);
    
    List<ProductRatingPO> findByProduct_Seller_UserId(Long sellerId);
}
package demo.bigwork.dao;

import demo.bigwork.model.po.CartItemPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemDAO extends JpaRepository<CartItemPO, Long> {

    /**
     * (關鍵) 查詢「某個購物車」中的「某個商品」
     * (這在「新增/更新購物車」時會用到，用來檢查商品是否已存在)
     *
     * "SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?"
     */
    Optional<CartItemPO> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);

    /**
     * (關鍵修正) 查詢某個購物車的「所有」項目，並按照 cartItemId 升序排列
     * 確保購物車項目列表的順序是固定的 (加入時間順序)
     */
    List<CartItemPO> findByCart_CartIdOrderByCartItemIdAsc(Long cartId);
    
    /*
     * @Modifying 告訴 JPA 這是「寫入」操作 (DELETE/UPDATE)
     * @Query 讓我們手動撰寫 HQL (類似 SQL)
     */
    @Modifying
    @Query("DELETE FROM CartItemPO c WHERE c.cart.cartId = :cartId")
    void deleteAllByCart_CartId(@Param("cartId") Long cartId);
}
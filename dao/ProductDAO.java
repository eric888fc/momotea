package demo.bigwork.dao;

import demo.bigwork.model.po.CategoryPO;
import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository (DAO) for ProductPO
 *
 * (關鍵) JpaRepository<ProductPO, Long>
 * - ProductPO: 這個 DAO 是管 `ProductPO` 的
 * - Long:        `ProductPO` 的主鍵 (productId) 型別是 `Long`
 */
@Repository
public interface ProductDAO extends JpaRepository<ProductPO, Long> {

    // --- (關鍵) 我們未來一定會用到的「方法名稱查詢」 ---

    /**
     * 查詢某個「賣家」的「所有商品」
     * (JpaRepository 會自動分析 POJO 的關聯)
     *
     * "SELECT * FROM products WHERE seller_id = ?"
     */
    List<ProductPO> findBySeller(UserPO seller);
    
    /**
     * (重載) 查詢某個「賣家」的「所有商品」(透過 ID)
     *
     * "SELECT * FROM products WHERE seller_id = ?"
     */
    List<ProductPO> findBySeller_UserId(Long sellerId);

    /**
     * 查詢某個「分類」下的「所有商品」
     *
     * "SELECT * FROM products WHERE category_id = ?"
     */
    List<ProductPO> findByCategory(CategoryPO category);
    
    /**
     * (重載) 查詢某個「分類」下的「所有商品」(透過 ID)
     *
     * "SELECT * FROM products WHERE category_id = ?"
     */
    List<ProductPO> findByCategory_CategoryId(Integer categoryId);
    
    /**
     * (關鍵新增！)
     * 查詢「多個」 分類 ID 下的 「所有商品」
     *
     * "SELECT * FROM products WHERE category_id IN (?, ?, ?, ...)"
     */
    List<ProductPO> findByCategory_CategoryIdIn(List<Integer> categoryIds);

    /**
     * (範例) 
     * 透過「商品名稱」進行模糊查詢 (e.g., 搜尋功能)
     * "SELECT * FROM products WHERE name LIKE ?"
     * (e.g., findByNameContaining("iPhone"))
     */
    List<ProductPO> findByNameContaining(String name);
}
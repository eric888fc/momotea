package demo.bigwork.dao;

import demo.bigwork.model.po.CategoryPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) for CategoryPO
 *
 * (關鍵) JpaRepository<CategoryPO, Integer>
 * - CategoryPO: 這個 DAO 是管 `CategoryPO` 的
 * - Integer:    `CategoryPO` 的主鍵 (categoryId) 型別是 `Integer`
 */
@Repository
public interface CategoryDAO extends JpaRepository<CategoryPO, Integer> {

    /**
     * (教授建議) Spring Data JPA 的「方法名稱查詢」
     *
     * (範例) 
     * 我們未來會需要「查詢所有第一層分類 (根分類)」
     * 也就是 `parent_category_id` 為 NULL 的分類
     *
     * JpaRepository 會自動將此方法名轉換為 SQL:
     * "SELECT * FROM categories WHERE parent_category_id IS NULL"
     */
    List<CategoryPO> findByParentCategoryIsNull();

    /**
     * (範例) 
     * 查詢某個父分類下的所有「子分類」
     * "SELECT * FROM categories WHERE parent_category_id = ?"
     */
    List<CategoryPO> findByParentCategory_CategoryId(Integer parentId);

    /**
     * (範例) 
     * 透過名稱查詢分類 (用於檢查分類是否已存在)
     * "SELECT * FROM categories WHERE name = ?"
     */
    Optional<CategoryPO> findByName(String name);
}
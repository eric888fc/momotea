package demo.bigwork.service;

import demo.bigwork.model.vo.CategoryNodeVO; // (修改) 匯入新的 VO
import java.util.List;

public interface CategoryService {

    /**
     * (修正) 獲取「所有」的「樹狀」商品分類
     * (我們只會回傳「根節點」，但它們會包含所有的子節點)
     *
     * @return  根節點 分類的列表 (包含子孫)
     */
    List<CategoryNodeVO> getCategoryTree();
    
    /**
     * (關鍵新增！)
     * （遞迴） 獲取某個分類 「及其所有子孫」 的 ID 列表
     *
     * @param categoryId 父 ID
     * @return 包含 `categoryId` 本身，以及所有子孫 ID 的 List
     */
    List<Integer> getAllChildCategoryIds(Integer categoryId);
}
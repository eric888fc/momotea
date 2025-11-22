package demo.bigwork.model.vo;

import demo.bigwork.dao.CategoryDAO;
import demo.bigwork.model.po.CategoryPO;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (新) 用於回傳「樹狀」巢狀分類的 VO
 */
@Data
public class CategoryNodeVO {
    
    // (我們保留和 PO 一樣的欄位)
    private Integer categoryId;
    private String name;
    
    // (關鍵！) 這是 PO 沒有的：一個儲存「子分類」的列表
    private List<CategoryNodeVO> children = new ArrayList<>();

    /**
     * 建構子：將一個 PO 轉換為 VO
     */
    public CategoryNodeVO(CategoryPO po) {
        this.categoryId = po.getCategoryId();
        this.name = po.getName();
    }
    
    /**
     * （輔助方法） 遞迴 (Recursive) 建構子
     * 這是核心：它會自動抓取所有的子孫
     */
    public CategoryNodeVO(CategoryPO po, CategoryDAO categoryDAO) {
        this(po); // 呼叫上面的基本建構子
        
        // (關鍵)  使用你在 CategoryDAO 中已經定義好的方法！
        List<CategoryPO> childPOs = categoryDAO.findByParentCategory_CategoryId(po.getCategoryId());
        
        // 對於每一個「子 PO」， 再次呼叫這個「遞迴建構子」
        this.children = childPOs.stream()
                .map(childPO -> new CategoryNodeVO(childPO, categoryDAO)) // 遞迴 開始
                .collect(Collectors.toList());
    }
}
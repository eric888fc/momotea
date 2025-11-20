package demo.bigwork.service.Impl;

import demo.bigwork.dao.CategoryDAO;
import demo.bigwork.model.po.CategoryPO;
import demo.bigwork.model.vo.CategoryNodeVO; // (修改) 匯入新的 VO
import demo.bigwork.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; // (修改) 匯入

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryDAO categoryDAO;

    @Autowired
    public CategoryServiceImpl(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /**
     * 實作：獲取「樹狀」分類
     */
    @Override
    @Transactional(readOnly = true) 
    public List<CategoryNodeVO> getCategoryTree() {
        logger.info("正在查詢「樹狀」商品分類...");
        
        // 1. (關鍵！) 使用你的 DAO 方法，只抓取「第一層」（父類別為 NULL 的）
        List<CategoryPO> rootCategoryPOs = categoryDAO.findByParentCategoryIsNull();
        
        logger.info("找到{}個根分類。",rootCategoryPOs.size());
        
        // 2. (關鍵！) 將這些 PO 轉換為「 VOS 」
        // 在轉換的過程中， CategoryNodeVO 的「遞迴建構子」會「自動」去 categoryDAO 中 
        // 抓取所有的子孫
        return rootCategoryPOs.stream()
                .map(rootPO -> new CategoryNodeVO(rootPO, categoryDAO)) // 遞迴 在此觸發
                .collect(Collectors.toList());
    }
    
    /**
     * (關鍵新增！) 
     * 實作：獲取 ID 列表（公開方法）
     */
    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAllChildCategoryIds(Integer categoryId) {
        // 1. 建立一個 List 來收集 ID
        List<Integer> categoryIds = new java.util.ArrayList<>();
        
        // 2. 首先， 把 「父 ID 本身」 加進去
        categoryIds.add(categoryId);
        
        // 3. 呼叫「私有的」 遞迴輔助方法， 來「填滿」 這個 List
        findChildrenRecursive(categoryId, categoryIds);
        
        logger.info("分類 ID: {} 及其所有子孫 ID 共有： {}",categoryId, categoryIds);
        return categoryIds;
    }

    /**
     * (關鍵新增！) 
     * 遞迴輔助方法： 不斷向下 尋找 子 ID 並 加入 List
     *
     * @param parentId 目前要查詢的 「父 ID」
     * @param categoryIds  「共用的」 結果 List
     */
    private void findChildrenRecursive(Integer parentId, List<Integer> categoryIds) {
        // 1. 使用你的 DAO 方法， 查詢 「直接子代」
        // ( `findByParentCategory_CategoryId` )
        List<CategoryPO> children = categoryDAO.findByParentCategory_CategoryId(parentId);

        // 2.  `forEach`  所有 「直接子代」
        for (CategoryPO child : children) {
            // 3. 將 「子 ID」 加入 List
            categoryIds.add(child.getCategoryId());
            
            // 4. (遞迴) 然後， 「以這個子代為新的父代」， 再次呼叫「它自己」
            findChildrenRecursive(child.getCategoryId(), categoryIds);
        }
    }
}
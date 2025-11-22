package demo.bigwork.controller;

import demo.bigwork.model.vo.CategoryNodeVO; 
import demo.bigwork.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * API 端點：(公開) 獲取「樹狀」商品分類
     * GET http://localhost:8080/api/public/categories
     */
    @GetMapping
    public ResponseEntity<?> getCategoryTree() { // (修改) 呼叫新的服務方法
        try {
            List<CategoryNodeVO> categoryTree = categoryService.getCategoryTree();
            return ResponseEntity.ok(categoryTree);

        } catch (Exception e) {
            logger.error("獲取樹狀分類時發生錯誤", e);
            return ResponseEntity.internalServerError().body("伺服器錯誤： " + e.getMessage());
        }
    }
}
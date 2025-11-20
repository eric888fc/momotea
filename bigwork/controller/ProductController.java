package demo.bigwork.controller;

import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.vo.ProductRequestVO;
import demo.bigwork.model.vo.ProductResponseVO; // (匯入 1)
import demo.bigwork.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // (匯入 2)
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
// (關鍵) 基礎路徑。此 Controller 下的所有 API 都受 SecurityConfig 保護
@RequestMapping("/api/products") 
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * API 端點：新增商品
     * POST http://localhost:8080/api/products
     * (注意：路徑是 /，因為基礎路徑已是 /api/products)
     *
     * (安全) 此 API 已被 SecurityConfig 保護，
     * 只有 "SELLER" 角色的 Token 才
     * 能訪問
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequestVO requestVO) {
        
        try {
            // 1. 呼叫 Service，Service 內部會處理安全驗證
            ProductPO newProduct = productService.addProduct(requestVO);
            
            // 2. (關鍵) 轉換為「乾淨」的 VO 才回傳
            ProductResponseVO responseVO = new ProductResponseVO(newProduct);
            
            // 3. 回傳 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(responseVO);
        
        } catch (AccessDeniedException e) {
            // (安全) Service 層拋出「權限不足」
            logger.warn("權限不足：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
            
        } catch (Exception e) {
            // (業務) e.g., 分類 ID 找不到
            logger.error("新增商品失敗", e);
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }
    
    /**
     * (新) API 端點：查詢「我的」(賣家) 所有商品
     * GET http://localhost:8080/api/products
     *
     * (安全) 此 API 已被 SecurityConfig 保護 (hasRole('SELLER'))
     */
    @GetMapping
    public ResponseEntity<?> getMyProducts() {
        try {
            // 1. 呼叫 Service
            List<ProductPO> myProducts = productService.getMyProducts();
            
            // 2. (關鍵) 
            // 將 List<ProductPO> 轉換為 List<ProductResponseVO>
            // 以避免回傳敏感資料或 JAP Lazy Loading 錯誤
            List<ProductResponseVO> responseList = myProducts.stream()
                    .map(ProductResponseVO::new) // 呼叫 VO 的 (PO) 建構子
                    .collect(Collectors.toList());
            
            // 3. 回傳 200 OK
            return ResponseEntity.ok(responseList);

        } catch (AccessDeniedException e) {
            // (安全) 如果是買家 Token 呼叫
            logger.warn("權限不足 (getMyProducts)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
            
        } catch (Exception e) {
            logger.error("查詢商品失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }
    
    /**
     * (新) API 端點：更新「我的」商品
     * PUT http://localhost:8080/api/products/{productId}
     *
     * @PathVariable Long productId:
     * (關鍵) 告訴 Spring Boot 從 URL 路徑中 (e.g., /api/products/123)
     * 讀取 123 並將其注入 productId 變數
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId, 
            @Valid @RequestBody ProductRequestVO requestVO) {
        
        try {
            // 1. 呼叫 Service
            ProductPO updatedProduct = productService.updateProduct(productId, requestVO);
            
            // 2. 轉換為 VO 回傳
            ProductResponseVO responseVO = new ProductResponseVO(updatedProduct);
            
            // 3. 回傳 200 OK
            return ResponseEntity.ok(responseVO);

        } catch (AccessDeniedException e) {
            // (安全) e.g., "您沒有權限更新此商品"
            logger.warn("權限不足 (updateProduct)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
            
        } catch (EntityNotFoundException e) {
            // (業務) e.g., "找不到商品" 或 "分類找不到"
            logger.warn("找不到實體 (updateProduct)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            
        } catch (Exception e) {
            // (其他) e.g., 驗證失敗
            logger.error("更新商品失敗", e);
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }
    
    /**
     * (新) API 端點：刪除「我的」商品
     * DELETE http://localhost:8080/api/products/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        
        try {
            // 1. 呼叫 Service
            productService.deleteProduct(productId);
            
            // 2. (成功) 
            // (RESTful 最佳實踐)
            // 刪除成功後，回傳 204 No Content
            // (代表「我成功了，但我沒有任何內容要回傳給你」)
            return ResponseEntity.noContent().build(); // HTTP 204

        } catch (AccessDeniedException e) {
            // (安全) e.g., "您沒有權限刪除此商品"
            logger.warn("權限不足 (deleteProduct)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
            
        } catch (EntityNotFoundException e) {
            // (業務) e.g., "找不到商品"
            logger.warn("找不到實體 (deleteProduct)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            
        } catch (Exception e) {
            // (其他)
            logger.error("刪除商品失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }
    
    /**
     * (新) API 端點：上傳商品圖片
     * POST http://localhost:8080/api/products/{productId}/image
     *
     * (關鍵) 
     * @RequestParam("file") MultipartFile file
     * 告訴 Spring Boot：
     * 1. 這個 API 接收的格式是 "multipart/form-data"
     * 2. 去 "form-data" 裡面找一個叫做 "file" 的欄位
     * 3. 將這個 "file" 欄位轉換為 MultipartFile 物件
     */
    @PostMapping("/{productId}/image")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // (檢查檔案是否為空)
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("上傳檔案不可為空");
            }
            
            // 1. 呼叫 Service
            ProductPO updatedProduct = productService.updateProductImage(productId, file);
            
            // 2. 轉換為 VO 回傳
            ProductResponseVO responseVO = new ProductResponseVO(updatedProduct);
            
            // 3. 回傳 200 OK (更新成功)
            return ResponseEntity.ok(responseVO);

        } catch (AccessDeniedException e) {
            // (安全) e.g., "您沒有權限更新此商品"
            logger.warn("權限不足 (uploadImage)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
            
        } catch (EntityNotFoundException e) {
            // (業務) e.g., "找不到商品"
            logger.warn("找不到實體 (uploadImage)：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
            
        } catch (Exception e) {
            // (檔案) e.g., 檔案儲存失敗
            logger.error("上傳圖片失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上傳圖片失敗: " + e.getMessage()); // 500
        }
    }
}
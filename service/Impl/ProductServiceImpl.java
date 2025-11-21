package demo.bigwork.service.Impl;

import demo.bigwork.dao.CategoryDAO;
import demo.bigwork.dao.ProductDAO;
import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.CategoryPO;
import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.ProductRequestVO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.CategoryService;
import demo.bigwork.service.FileStorageService;
import demo.bigwork.service.ProductService;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final UserDAO userDAO; // (我們需要 UserDAO 來驗證賣家身分)
    private final FileStorageService fileStorageService; // (關鍵) 注入檔案服務
    private final AuthHelperService authHelperService;
    private final CategoryService categoryService;

    // (關鍵) 更新建構子
    @Autowired
    public ProductServiceImpl(ProductDAO productDAO, CategoryDAO categoryDAO, UserDAO userDAO,
                              FileStorageService fileStorageService,
                              AuthHelperService authHelperService,
                              CategoryService categoryService) { // <-- 新增
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.userDAO = userDAO;
        this.fileStorageService = fileStorageService; // <-- 新增
        this.authHelperService = authHelperService;
        this.categoryService = categoryService;
    }

    /**
     * (關鍵) 
     * @Transactional 
     * 確保「新增商品」是一個完整的交易
     */
    @Override
    @Transactional
    public ProductPO addProduct(ProductRequestVO requestVO) throws AccessDeniedException, Exception {
        
        // 1. (安全) 取得當前登入的賣家
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (業務) 驗證 Category 是否存在
        CategoryPO category = categoryDAO.findById(requestVO.getCategoryId())
                .orElseThrow(() -> new Exception("分類 (Category) 找不到, ID: " + requestVO.getCategoryId()));

        // 3. (組裝) 從 VO 轉換到 PO
        ProductPO newProduct = new ProductPO();
        newProduct.setName(requestVO.getName());
        newProduct.setDescription(requestVO.getDescription());
        newProduct.setPrice(requestVO.getPrice());
        newProduct.setStock(requestVO.getStock());
        
        // 4. (關鍵) 設定關聯
        newProduct.setSeller(seller);   // (設定賣家)
        newProduct.setCategory(category); // (設定分類)
        
        // 5. (儲存) 呼叫 DAO
        logger.info("賣家 '{}' 正在新增商品: {}", seller.getEmail(), newProduct.getName());
        return productDAO.save(newProduct);
    }
    
    @Override
    @Transactional(readOnly = true) // (效能) 查詢功能，設為 readOnly
    public List<ProductPO> getMyProducts() throws AccessDeniedException {
        
        // 1. (安全) (關鍵)
        // 我們 100% 重用「新增商品」時寫的「安全驗證」輔助方法
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        logger.info("賣家 '{}'  đang查詢他所有的商品...", seller.getEmail());

        // 2. (業務) 
        // 呼叫我們在 DAO 中定義的「方法名稱查詢」
        // (findBySeller_UserId 會自動轉為 "WHERE seller_id = ?")
        return productDAO.findBySeller_UserId(seller.getUserId());
    }
    
    @Override
    @Transactional // (重要) 更新操作必須是交易
    public ProductPO updateProduct(Long productId, ProductRequestVO requestVO) 
            throws AccessDeniedException, EntityNotFoundException, Exception {
        
        // 1. (安全) 取得當前登入的賣家
        // (我們 100% 重用這個輔助方法)
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (業務) 找出「商品」
        ProductPO productToUpdate = productDAO.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("找不到商品, ID: " + productId));

        // 3. (關鍵 - 安全) 
        // 驗證「所有權」
        // 檢查「商品的賣家 ID」是否等於「當前登入的賣家 ID」
        if (!productToUpdate.getSeller().getUserId().equals(seller.getUserId())) {
            logger.warn("權限不足：賣家 {} 試圖更新「不屬於」他的商品 (ID: {})", 
                         seller.getEmail(), productId);
            throw new AccessDeniedException("您沒有權限更新此商品");
        }

        // 4. (業務) 驗證新的 Category 是否存在
        CategoryPO newCategory = categoryDAO.findById(requestVO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("分類 (Category) 找不到, ID: " + requestVO.getCategoryId()));
        
        // 5. (執行更新) 
        // 從 VO 更新 PO 的資料
        productToUpdate.setName(requestVO.getName());
        productToUpdate.setDescription(requestVO.getDescription());
        productToUpdate.setPrice(requestVO.getPrice());
        productToUpdate.setStock(requestVO.getStock());
        productToUpdate.setCategory(newCategory); // (更新分類關聯)
        
        // 6. (儲存) 
        // (關鍵) 因為 productToUpdate 是從 JPA 查出來的 (Attached 狀態)，
        // 呼叫 save() 會自動執行「UPDATE」SQL
        logger.info("賣家 {} 正在更新商品 (ID: {})", seller.getEmail(), productId);
        return productDAO.save(productToUpdate);
    }
    
    @Override
    @Transactional // (重要) 刪除操作必須是交易
    public void deleteProduct(Long productId) 
            throws AccessDeniedException, EntityNotFoundException {
        
        // 1. (安全) 取得當前登入的賣家
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (業務) 找出「商品」
        ProductPO productToDelete = productDAO.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("找不到商品, ID: " + productId));

        // 3. (關鍵 - 安全) 
        // 驗證「所有權」
        if (!productToDelete.getSeller().getUserId().equals(seller.getUserId())) {
            logger.warn("權限不足：賣家 {} 試圖刪除「不屬於」他的商品 (ID: {})", 
                         seller.getEmail(), productId);
            throw new AccessDeniedException("您沒有權限刪除此商品");
        }

        // 4. (執行刪除) 
        // 驗證通過，執行刪除
        productDAO.delete(productToDelete);
        
        // (或者) productDAO.deleteById(productId); 
        // 兩者皆可，但前者更明確
        
        logger.info("賣家 {} 已成功刪除商品 (ID: {})", seller.getEmail(), productId);
    }
    
    @Override
    @Transactional // (重要) 這是一個更新操作
    public ProductPO updateProductImage(Long productId, MultipartFile file)
            throws AccessDeniedException, EntityNotFoundException, Exception {

        // 1. (安全) 取得當前登入的賣家
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        
        // 2. (業務) 找出「商品」
        ProductPO productToUpdate = productDAO.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("找不到商品, ID: " + productId));

        // 3. (關鍵 - 安全) 
        // 驗證「所有權」(我們重用和 update/delete 一樣的邏輯)
        if (!productToUpdate.getSeller().getUserId().equals(seller.getUserId())) {
            logger.warn("權限不足：賣家 {} 試圖上傳「不屬於」他的商品圖片 (ID: {})", 
                         seller.getEmail(), productId);
            throw new AccessDeniedException("您沒有權限更新此商品");
        }

        // 4. (關鍵 - 檔案) 
        // 呼叫 FileStorageService 儲存檔案
        // (如果儲存失敗，FileStorageService 會拋出 Exception)
        String imageUrl = fileStorageService.store(file);
        
        // (未來) 
        // 這裡可以加入「刪除舊圖片」的邏輯
        // if (productToUpdate.getImageUrl() != null) {
        //   fileStorageService.deleteFile(productToUpdate.getImageUrl());
        // }

        // 5. (儲存) 
        // 將 Service 回傳的「路徑」存入 PO
        productToUpdate.setImageUrl(imageUrl);
        
        // 6. (更新) 
        // 呼叫 save() 來執行 UPDATE SQL
        logger.info("賣家 {} 已成功更新商品圖片 (ID: {})，路徑: {}", 
                     seller.getEmail(), productId, imageUrl);
        return productDAO.save(productToUpdate);
    }
    
    /**
     * (新) 實作：(公開) 取得所有商品列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductPO> getAllPublicProducts() {
        logger.info("正在查詢「所有」公開商品列表...");
        
        // (效能) 
        // 我們不只是 findAll()，我們還指定「排序」
        // 讓最新的商品 (created_at 欄位) 排在最前面
        return productDAO.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * (新) 實作：(公開) 透過 ID 取得單一商品
     */
    @Override
    @Transactional(readOnly = true)
    public ProductPO getPublicProductById(Long productId) throws EntityNotFoundException {
        logger.info("正在查詢「公開」商品, ID: {}", productId);
        
        return productDAO.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("找不到商品, ID: " + productId));
    }
    
    /**
     * (關鍵新增！)實作：(公開)透過Category ID取得商品
     * 實作：(公開) 透過 Category ID 取得商品（包含子孫）
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductPO> getPublicProductsByCategory(Integer categoryId) {
        
        // 1. (新) 呼叫 CategoryService 來「遞迴 獲取 所有 ID」
        List<Integer> allCategoryIds = categoryService.getAllChildCategoryIds(categoryId);
        
        logger.info("正在查詢 「分類 ID 列表：{}」 的公開商品...",allCategoryIds);
        
        // 2. (新) 呼叫 ProductDAO 新增的 「 IN (...) 」 查詢 
        return productDAO.findByCategory_CategoryIdIn(allCategoryIds);
    }
    
}
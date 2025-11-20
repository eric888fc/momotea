package demo.bigwork.service;

import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.vo.ProductRequestVO;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List; // (關鍵) 匯入 List

public interface ProductService {

    /**
     * (已完成) 業務邏輯：新增商品
     */
    ProductPO addProduct(ProductRequestVO requestVO) throws AccessDeniedException, Exception;
    
    /**
     * (新) 業務邏輯：查詢「當前登入賣家」的所有商品
     *
     * @return 該賣家的商品列表 (List)
     * @throws AccessDeniedException 如果呼叫者不是 SELLER
     */
    List<ProductPO> getMyProducts() throws AccessDeniedException;
    
    /**
     * (新) 業務邏輯：更新「屬於我」的商品
     *
     * @param productId 要更新的商品 ID
     * @param requestVO 包含新資料的 VO
     * @return 更新成功後的 ProductPO
     * @throws AccessDeniedException (安全) 如果 Token 不是 SELLER 或「不擁有」此商品
     * @throws EntityNotFoundException (業務) 如果 productId 或 categoryId 找不到
     */
    ProductPO updateProduct(Long productId, ProductRequestVO requestVO) 
            throws AccessDeniedException, EntityNotFoundException, Exception;
    /**
     * (新) 業務邏輯：刪除「屬於我」的商品
     *
     * @param productId 要刪除的商品 ID
     * @throws AccessDeniedException (安全) 如果 Token 不是 SELLER 或「不擁有」此商品
     * @throws EntityNotFoundException (業務) 如果 productId 找不到
     */
    void deleteProduct(Long productId) 
            throws AccessDeniedException, EntityNotFoundException;
    ProductPO updateProductImage(Long productId, MultipartFile file)
            throws AccessDeniedException, EntityNotFoundException, Exception;
    /**
     * (新) 業務邏輯：(公開) 取得所有商品列表
     */
    List<ProductPO> getAllPublicProducts();

    /**
     * (新) 業務邏輯：(公開) 透過 ID 取得單一商品
     */
    ProductPO getPublicProductById(Long productId) throws EntityNotFoundException;
    
    /**
     * (關鍵新增！)
     * 我們需要一個新的服務方法來「專門處理篩選」
     * 這將會呼叫`productDAO.findByCategory_CategoryId(categoryId)`
     */
    List<ProductPO> getPublicProductsByCategory(Integer categoryId);
}
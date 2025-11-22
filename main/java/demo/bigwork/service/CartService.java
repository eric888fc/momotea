package demo.bigwork.service;

import demo.bigwork.model.vo.AddCartItemRequestVO;
import demo.bigwork.model.vo.CartResponseVO; // (關鍵) 匯入
import org.springframework.security.access.AccessDeniedException;

public interface CartService {

    /**
     * (修改) 回傳型別改為 VO
     */
    CartResponseVO getMyCart() throws AccessDeniedException;

    /**
     * (修改) 回傳型別改為 VO
     */
    CartResponseVO addOrUpdateItemInMyCart(AddCartItemRequestVO requestVO) throws AccessDeniedException, Exception;

    /**
     * (修改) 回傳型別改為 VO
     */
    CartResponseVO updateItemQuantityInMyCart(Long cartItemId, Integer quantity) throws AccessDeniedException, Exception;

    /**
     * (不變) 刪除
     */
    void deleteItemFromMyCart(Long cartItemId) throws AccessDeniedException;
    
    /**
     * (不變) 清空
     */
    void clearMyCart() throws AccessDeniedException;
}
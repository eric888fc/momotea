package demo.bigwork.service.Impl;

import demo.bigwork.dao.CartDAO;
import demo.bigwork.dao.CartItemDAO;
import demo.bigwork.dao.ProductDAO;
import demo.bigwork.model.po.CartItemPO;
import demo.bigwork.model.po.CartPO;
import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.AddCartItemRequestVO;
import demo.bigwork.model.vo.CartResponseVO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.CartService;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final AuthHelperService authHelperService; // (我們的安全保全)

    @Autowired
    public CartServiceImpl(CartDAO cartDAO, CartItemDAO cartItemDAO, ProductDAO productDAO, AuthHelperService authHelperService) {
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
        this.authHelperService = authHelperService;
    }

    /**
     * (輔助方法) 取得或建立購物車
     */
    @Transactional
    private CartPO getOrCreateCartForBuyer() {
        logger.debug("正在取得或建立購物車...");
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        return cartDAO.findByUser_UserId(buyer.getUserId())
                .orElseGet(() -> {
                    logger.info("買家 {} 首次使用購物車，自動建立新購物車...", buyer.getEmail());
                    CartPO newCart = new CartPO(buyer);
                    return cartDAO.save(newCart);
                });
    }

    /**
    * (修正) 查詢「我的」購物車
    */
   @Override
   @Transactional
   public CartResponseVO getMyCart() throws AccessDeniedException {
       logger.info("Service: 正在查詢我的購物車...");
       CartPO cart = getOrCreateCartForBuyer();
       
       // ***** [修正點 1: 移除 Hibernate.initialize，改用 DAO 獲取排序列表] *****
       logger.debug("Service: 正在從 DAO 獲取排序後的 items 列表...");
       Long cartId = cart.getCartId();
       // 呼叫帶有排序的新 DAO 方法
       List<CartItemPO> sortedItems = cartItemDAO.findByCart_CartIdOrderByCartItemIdAsc(cartId);
       
       // 將排序後的列表設定回 CartPO 的 items 集合中，確保轉換 VO 時順序正確
       cart.getItems().clear(); // 清空舊的集合 (如果它被載入了)
       cart.getItems().addAll(sortedItems); // 填充排序後的列表
       // *******************************************************************
       
       logger.info("Service: 查詢完成，正在轉換為 VO...");
       return new CartResponseVO(cart); // (在交易內轉換)
   }

    /**
     * (修正) 新增/更新 商品到「我的」購物車 (改為累加模式)
     */
    @Override
    @Transactional
    public CartResponseVO addOrUpdateItemInMyCart(AddCartItemRequestVO requestVO) throws AccessDeniedException, Exception {
        // (注意：現在這個方法的核心職責是「新增或累積」)
        logger.info("Service: 開始新增/累積項目 (PID: {}, Qty: {})", requestVO.getProductId(), requestVO.getQuantity());

        // 1. 取得購物車 (這會驗證買家)
        CartPO cart = getOrCreateCartForBuyer();
        
        // 2. 檢查商品
        ProductPO product = productDAO.findById(requestVO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("找不到商品, ID: " + requestVO.getProductId()));
        logger.debug("Service: 商品檢查通過 (Name: {})", product.getName());

        // 3. (關鍵變更) 預設只檢查「要加入的數量」是否超過單項庫存 (這是必要的最低檢查)
        if (product.getStock() < requestVO.getQuantity() && product.getStock() > 0) {
            // 如果商品庫存 <= 0 應該在前台就不能點選，但後端依然要擋
            logger.warn("Service: 庫存不足 (Stock: {}, Req: {})", product.getStock(), requestVO.getQuantity());
            throw new Exception("商品庫存不足 (庫存: " + product.getStock() + ")，您嘗試加入的數量: " + requestVO.getQuantity());
        }
        logger.debug("Service: 基礎數量檢查通過");

        // 4. (安全修正) 手動初始化 items 集合
        logger.debug("Service: 正在手動初始化 items 集合 (CartID: {})...", cart.getCartId());
        Hibernate.initialize(cart.getItems());
        
        // 5. 在「記憶體」中尋找現有項目
        Optional<CartItemPO> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(requestVO.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // (A) 已存在 -> 累積數量 (核心變更點)
            CartItemPO existingItem = existingItemOpt.get();
            
            // --- 核心累加邏輯 ---
            int quantityToAdd = requestVO.getQuantity(); 
            int newAccumulatedQuantity = existingItem.getQuantity() + quantityToAdd; // 舊數量 + 新數量
            
            logger.info("Service: 商品已在購物車中，從 {} 累加 {} 到總數 {}", 
                        existingItem.getQuantity(), quantityToAdd, newAccumulatedQuantity);

            // 重新檢查「累積後」的總數量是否超過庫存
            if (newAccumulatedQuantity > product.getStock()) {
                logger.warn("Service: 累積後庫存不足 (Stock: {}, New Total: {})", product.getStock(), newAccumulatedQuantity);
                throw new Exception("累積數量 (" + newAccumulatedQuantity + ") 已超過庫存上限 (" + product.getStock() + ")，請減少數量。");
            }

            // 設定新的累積數量
            existingItem.setQuantity(newAccumulatedQuantity);
            cartItemDAO.save(existingItem); 
            // ------------------

        } else {
            // (B) 不存在 -> 建立新項目 (保持不變)
            logger.info("Service: 新增商品到購物車: {}", product.getName());
            CartItemPO newItem = new CartItemPO();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(requestVO.getQuantity()); 
            
            cartItemDAO.save(newItem); 
            cart.getItems().add(newItem); 
        }

        logger.info("Service: 操作成功，正在轉換為 VO...");
        return new CartResponseVO(cart); 
    }

    /**
     * (修改) 更新「我的」購物車中某個「項目」的數量
     */
    @Override
    @Transactional
    public CartResponseVO updateItemQuantityInMyCart(Long cartItemId, Integer quantity) throws AccessDeniedException, Exception {
        logger.info("Service: 開始更新項目 (ItemID: {}, Qty: {})", cartItemId, quantity);

        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();

        CartItemPO item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("找不到購物車項目, ID: " + cartItemId));
        
        // (驗證所有權)
        if (!item.getCart().getUser().getUserId().equals(buyer.getUserId())) {
            logger.warn("Service: 權限不足！買家 {} 試圖更新別人的項目 {}", buyer.getEmail(), cartItemId);
            throw new AccessDeniedException("您沒有權限操作此項目");
        }
        logger.debug("Service: 所有權驗證通過");

        // (驗證庫存)
        if (item.getProduct().getStock() < quantity) {
            logger.warn("Service: 庫存不足 (Stock: {}, Req: {})", item.getProduct().getStock(), quantity);
            throw new Exception("商品庫存不足 (庫存: " + item.getProduct().getStock() + ")");
        }
        
        // (執行) 
        item.setQuantity(quantity);
        cartItemDAO.save(item); // 儲存
        logger.info("Service: 項目 {} 數量更新成功", cartItemId);
        
        // (回傳)
        // 取得「父」購物車
        CartPO updatedCart = item.getCart();
        
        // (安全) 
        // 轉換前，初始化父購物車的「所有」項目
        logger.debug("Service: 正在手動初始化 items 集合 (CartID: {})...", updatedCart.getCartId());
        Hibernate.initialize(updatedCart.getItems());
        
        logger.info("Service: 轉換為 VO...");
        return new CartResponseVO(updatedCart);
    }

    @Override
    @Transactional
    public void deleteItemFromMyCart(Long cartItemId) throws AccessDeniedException {
        // 1. (安全) 取得買家
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();

        // 2. (業務) 找出「項目」
        CartItemPO item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("找不到購物車項目, ID: " + cartItemId));
        
        // 3. (關鍵 - 安全) 
        // 驗證「所有權」
        if (!item.getCart().getUser().getUserId().equals(buyer.getUserId())) {
            throw new AccessDeniedException("您沒有權限操作此項目");
        }
        
        // 4. (執行) 刪除
        logger.info("從購物車刪除項目: {}", cartItemId);
        cartItemDAO.delete(item);
        // (或者) cartItemDAO.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearMyCart() throws AccessDeniedException {
        // 1. (安全 & 業務) 取得購物車 (已包含 "BUYER" 驗證)
        CartPO cart = getOrCreateCartForBuyer();
        
        // 2. (執行)
        // 呼叫我們在 DAO 中建立的「高效能」刪除方法
        logger.info("正在清空買家 {} 的購物車 (Cart ID: {})", 
                     cart.getUser().getEmail(), cart.getCartId());
        
        cartItemDAO.deleteAllByCart_CartId(cart.getCartId());
    }
}
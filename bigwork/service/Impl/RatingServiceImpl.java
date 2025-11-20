package demo.bigwork.service.Impl;

import demo.bigwork.dao.OrderItemDAO;
import demo.bigwork.dao.ProductRatingDAO;
import demo.bigwork.model.po.OrderItemPO;
import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.po.ProductRatingPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.CreateRatingRequestVO;
import demo.bigwork.model.vo.RatingResponseVO;
import demo.bigwork.model.vo.UpdateRatingRequestVO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.RatingService;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate; // (關鍵) 匯入 Hibernate
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final ProductRatingDAO productRatingDAO;
    private final OrderItemDAO orderItemDAO;
    private final AuthHelperService authHelperService;

    @Autowired
    public RatingServiceImpl(ProductRatingDAO productRatingDAO, OrderItemDAO orderItemDAO, AuthHelperService authHelperService) {
        this.productRatingDAO = productRatingDAO;
        this.orderItemDAO = orderItemDAO;
        this.authHelperService = authHelperService;
    }

    /**
     * (核心業務) 買家「建立」一筆新評價
     */
    @Override
    @Transactional // (S極度重要)
    public RatingResponseVO createRating(CreateRatingRequestVO requestVO) throws AccessDeniedException, Exception {
        
        // 1. (安全) 驗證「角色」
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("買家 {} 正在為訂單項目 {} 建立評價", buyer.getEmail(), requestVO.getOrderItemId());

        // 2. (驗證) 取得「訂單明細」
        OrderItemPO orderItem = orderItemDAO.findById(requestVO.getOrderItemId())
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單項目, ID: " + requestVO.getOrderItemId()));
        
        // 3. (安全 - 關鍵) 驗證「所有權」
        // (我們必須手動初始化 OrderPO)
        Hibernate.initialize(orderItem.getOrder());
        if (!orderItem.getOrder().getBuyer().getUserId().equals(buyer.getUserId())) {
            logger.warn("權限不足：買家 {} 試圖評價「不屬於」他的訂單項目 (ID: {})", 
                         buyer.getEmail(), requestVO.getOrderItemId());
            throw new AccessDeniedException("您沒有權限評價此訂單項目");
        }
        logger.debug("評價：所有權驗證通過");

        // 4. (業務 - 關鍵) 驗證「是否已被評價過」
        Optional<ProductRatingPO> existingRating = productRatingDAO
                .findByOrderItem_OrderItemId(requestVO.getOrderItemId());
        
        if (existingRating.isPresent()) {
            logger.warn("評價失敗：訂單項目 {} 已被評價過 (Rating ID: {})", 
                         requestVO.getOrderItemId(), existingRating.get().getRatingId());
            throw new Exception("您已經評價過此商品");
        }
        logger.debug("評價：唯一性驗證通過");
        
        // --- (所有驗證通過，開始建立) ---
        
        // 5. (安全) 手動初始化「商品」
        Hibernate.initialize(orderItem.getProduct());
        ProductPO product = orderItem.getProduct();
        if (product == null) {
            // (雖然S資料庫允許，但S邏輯上不應發生)
            throw new Exception("評價失敗：此商品已不存在");
        }

        // 6. 建立 PO
        ProductRatingPO newRating = new ProductRatingPO();
        newRating.setBuyer(buyer);
        newRating.setProduct(product);
        newRating.setOrderItem(orderItem);
        newRating.setRatingStars(requestVO.getRatingStars());
        newRating.setComment(requestVO.getComment());
        
        // 7. 儲存
        ProductRatingPO savedRating = productRatingDAO.save(newRating);
        logger.info("買家 {} 評價成功 (Rating ID: {})", buyer.getEmail(), savedRating.getRatingId());

        // 8. (安全) 回傳 VO
        // (因為 buyer 和 product 都已在「交易」中載入，
        //  VO 建構子可以安全地存取它們)
        return new RatingResponseVO(savedRating);
    }

    /**
     * (公開查詢) 取得「某個商品」的所有評價
     */
    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseVO> getRatingsForProduct(Long productId) {
        logger.info("正在查詢商品 {} 的所有評價...", productId);
        List<ProductRatingPO> ratings = productRatingDAO.findByProduct_ProductId(productId);
        
        // (S關鍵) 
        // 迴圈「手動初始化」所有評價的 LAZY buyer
        // (product 我們不需要，因為 VO 建構子只會用到 ratingPO.getProduct().getProductId())
        for (ProductRatingPO rating : ratings) {
            Hibernate.initialize(rating.getBuyer());
        }
        
        // (安全轉換)
        return ratings.stream()
                .map(RatingResponseVO::new)
                .collect(Collectors.toList());
    }

    /**
     * (買家查詢) 取得「我 (買家)」的所有評價
     */
    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseVO> getMyRatings() throws AccessDeniedException {
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("正在查詢買家 {} 的所有評價...", buyer.getEmail());
        
        List<ProductRatingPO> ratings = productRatingDAO.findByBuyer_UserId(buyer.getUserId());
        
        // (S關鍵) 
        // 迴圈「手動初始化」所有評價的 LAZY product
        for (ProductRatingPO rating : ratings) {
            Hibernate.initialize(rating.getProduct());
        }
        
        // (安全轉換)
        return ratings.stream()
                .map(RatingResponseVO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RatingResponseVO updateMyRating(Long ratingId, UpdateRatingRequestVO requestVO) 
            throws AccessDeniedException, EntityNotFoundException {
        
        // 1. (安全) 驗證「角色」
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("買家 {} 正在更新評價 (ID: {})", buyer.getEmail(), ratingId);

        // 2. (驗證) 取得「評價」
        ProductRatingPO rating = productRatingDAO.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評價, ID: " + ratingId));
        
        // 3. (安全 - 關鍵) 驗證「所有權」
        // (我們必須手動初始化 LAZY 的 buyer 欄位)
        Hibernate.initialize(rating.getBuyer());
        if (!rating.getBuyer().getUserId().equals(buyer.getUserId())) {
            logger.warn("權限不足：買家 {} 試圖更新「不屬於」他的評價 (ID: {})", 
                         buyer.getEmail(), ratingId);
            throw new AccessDeniedException("您沒有權限更新此評價");
        }
        logger.debug("評價：所有權驗證通過");

        // 4. (執行) 更新欄位
        rating.setRatingStars(requestVO.getRatingStars());
        rating.setComment(requestVO.getComment());
        
        // 5. 儲存
        ProductRatingPO savedRating = productRatingDAO.save(rating);
        logger.info("買家 {} 評價更新成功 (ID: {})", buyer.getEmail(), savedRating.getRatingId());

        // 6. (安全) 回傳 VO
        // (VO 建構子需要 Product，我們手動初始化它)
        Hibernate.initialize(savedRating.getProduct()); 
        return new RatingResponseVO(savedRating);
    }
    @Override
    @Transactional
    public void deleteMyRating(Long ratingId) 
            throws AccessDeniedException, EntityNotFoundException {
        
        // 1. (安全) 驗證「角色」
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("買家 {} 正在刪除評價 (ID: {})", buyer.getEmail(), ratingId);

        // 2. (驗證) 取得「評價」
        ProductRatingPO rating = productRatingDAO.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評價, ID: " + ratingId));
        
        // 3. (安全 - 關鍵) 驗證「所有權」
        // (我們必須手動初始化 LAZY 的 buyer 欄位)
        Hibernate.initialize(rating.getBuyer());
        if (!rating.getBuyer().getUserId().equals(buyer.getUserId())) {
            logger.warn("權限不足：買家 {} 試圖刪除「不屬於」他的評價 (ID: {})", 
                         buyer.getEmail(), ratingId);
            throw new AccessDeniedException("您沒有權限刪除此評價");
        }
        logger.debug("評價：所有權驗證通過");

        // 4. (執行) 刪除
        productRatingDAO.delete(rating);
        logger.info("買家 {} 評價刪除成功 (ID: {})", buyer.getEmail(), ratingId);
    }
    
    /**
     * (新) 實作：賣家查詢「所有」S收到的評價
     */
    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseVO> getRatingsForMyProducts() 
            throws AccessDeniedException {
        
        // 1. (安全) 驗證「角色」
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        logger.info("賣家 {} 正在查詢S收到的所有評價", seller.getEmail());

        // 2. (查詢) 呼叫我們在 DAO 建立的高效能方法
        List<ProductRatingPO> ratings = productRatingDAO
                .findByProduct_Seller_UserId(seller.getUserId());
        
        // 3. (S關鍵 - 安全) 
        // 迴圈「手動初始化」所有評價的 LAZY 欄位
        // (VO 建構子需要 buyer 和 product)
        for (ProductRatingPO rating : ratings) {
            Hibernate.initialize(rating.getBuyer());
            Hibernate.initialize(rating.getProduct());
        }
        
        // 4. (安全轉換)
        logger.info("賣家 {} 共查詢到 {} 筆評價", seller.getEmail(), ratings.size());
        return ratings.stream()
                .map(RatingResponseVO::new)
                .collect(Collectors.toList());
    }
}
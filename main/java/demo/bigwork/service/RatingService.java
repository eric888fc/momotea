package demo.bigwork.service;

import demo.bigwork.model.vo.CreateRatingRequestVO;
import demo.bigwork.model.vo.RatingResponseVO;
import demo.bigwork.model.vo.UpdateRatingRequestVO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface RatingService {

    /**
     * (核心業務) 
     * 買家「建立」一筆新評價
     * (Service 內部會驗證：1. 你是買家 2. 你擁有這筆訂單 3. 這筆訂單尚未被評價)
     *
     * @param requestVO 評價內容
     * @return 建立成功的新評價
     * @throws AccessDeniedException (如果不是 BUYER 或「不擁有」此訂單明細)
     * @throws EntityNotFoundException (如果訂單明細不存在)
     * @throws Exception (e.g., "此訂單已被評價過")
     */
    RatingResponseVO createRating(CreateRatingRequestVO requestVO) throws AccessDeniedException, Exception;

    /**
     * (公開查詢) 
     * 取得「某個商品」的所有評價
     * (這會被 PublicProductController 呼叫)
     *
     * @param productId 商品 ID
     * @return
     */
    List<RatingResponseVO> getRatingsForProduct(Long productId);

    /**
     * (買家查詢) 
     * 取得「我 (買家)」的所有評價
     *
     * @return
     * @throws AccessDeniedException (如果不是 BUYER)
     */
    List<RatingResponseVO> getMyRatings() throws AccessDeniedException;
    
    RatingResponseVO updateMyRating(Long ratingId, UpdateRatingRequestVO requestVO) 
            throws AccessDeniedException, EntityNotFoundException;
    
    void deleteMyRating(Long ratingId) 
            throws AccessDeniedException, EntityNotFoundException;
    
    List<RatingResponseVO> getRatingsForMyProducts() 
            throws AccessDeniedException;
}
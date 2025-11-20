package demo.bigwork.controller;

import demo.bigwork.model.vo.CreateRatingRequestVO;
import demo.bigwork.model.vo.RatingResponseVO;
import demo.bigwork.model.vo.UpdateRatingRequestVO;
import demo.bigwork.service.RatingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (新) 評價控制器 (買家專用)
 * (安全) 此 Controller 的路徑已被 SecurityConfig
 * 設定為 .hasRole("BUYER")
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * API 端點：(核心) 買家「建立」一筆新評價
     * POST http://localhost:8080/api/ratings
     */
    @PostMapping
    public ResponseEntity<?> createRating(@Valid @RequestBody CreateRatingRequestVO requestVO) {
        try {
            // (Service 內部會驗證所有權 和 是否重複)
            RatingResponseVO newRating = ratingService.createRating(requestVO);
            
            // (成功) 回傳 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(newRating);

        } catch (AccessDeniedException e) {
            // (e.g., 試圖評價「不屬於」你的訂單)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (EntityNotFoundException e) {
            // (e.g., 訂單項目 ID 找不到)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            // (關鍵) 
            // e.g., "您已經評價過此商品"
            logger.warn("建立評價失敗：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        }
    }
    
    @PutMapping("/{ratingId}")
    public ResponseEntity<?> updateMyRating(
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequestVO requestVO) {
        
        try {
            // (Service 內部會驗證所有權)
            RatingResponseVO updatedRating = ratingService.updateMyRating(ratingId, requestVO);
            
            // (成功) 回傳 200 OK
            return ResponseEntity.ok(updatedRating);

        } catch (AccessDeniedException e) {
            // (e.g., 試圖更新「不屬於」你的評價)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (EntityNotFoundException e) {
            // (e.g., 評價 ID 找不到)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            logger.error("更新評價失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    /**
     * API 端點：查詢「我 (買家)」的所有評價
     * GET http://localhost:8080/api/ratings/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyRatings() {
        try {
            List<RatingResponseVO> myRatings = ratingService.getMyRatings();
            return ResponseEntity.ok(myRatings);

        } catch (AccessDeniedException e) {
            // (這不應發生，因為 SecurityConfig 已攔截)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        }
    }
    
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<?> deleteMyRating(@PathVariable Long ratingId) {
        
        try {
            // (Service 內部會驗證所有權)
            ratingService.deleteMyRating(ratingId);
            
            // (成功) 回傳 204 No Content
            return ResponseEntity.noContent().build(); // 204

        } catch (AccessDeniedException e) {
            // (e.g., 試圖刪除「不屬於」你的評價)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (EntityNotFoundException e) {
            // (e.g., 評價 ID 找不到)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }
}
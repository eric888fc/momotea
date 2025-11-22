package demo.bigwork.controller;

import demo.bigwork.model.vo.RatingResponseVO;
import demo.bigwork.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * (新) 賣家評價控制器 (賣家專用)
 * (安全) 此 Controller 的路徑已被 SecurityConfig
 * 設定為 .hasRole("SELLER")
 */
@RestController
@RequestMapping("/api/seller/ratings") // (注意我們的路徑)
public class SellerRatingController {

    private static final Logger logger = LoggerFactory.getLogger(SellerRatingController.class);
    private final RatingService ratingService;

    @Autowired
    public SellerRatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * API 端點：查詢「我 (賣家)」收到的所有評價
     * GET http://localhost:8080/api/seller/ratings/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProductsRatings() {
        try {
            List<RatingResponseVO> myRatings = ratingService.getRatingsForMyProducts();
            return ResponseEntity.ok(myRatings);

        } catch (AccessDeniedException e) {
            // (這不應發生，因為 SecurityConfig 已攔截)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        }
    }
}
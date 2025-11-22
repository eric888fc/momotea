package demo.bigwork.controller;

import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.ProfileRequestVO;
import demo.bigwork.model.vo.ProfileResponseVO;
import demo.bigwork.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

/**
 * (新) 個人資料控制器
 * (安全) 此 Controller 已被 SecurityConfig
 * 以 /api/profile/** * 設定為 .authenticated() (必須登入)
 */
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private final UserService userService; // (重用 UserService)

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API 端點：查詢「我的」個人資料
     * GET http://localhost:8080/api/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        try {
            UserPO user = userService.getMyProfile();
            // (成功) 轉換為 VO 並回傳
            return ResponseEntity.ok(new ProfileResponseVO(user));
            
        } catch (AccessDeniedException e) {
            // (安全) e.g., Token 無效或過期
            return ResponseEntity.status(401).body("未登入或 Token 無效"); // 401
        }
    }

    /**
     * API 端點：更新「我的」個人資料
     * PUT http://localhost:8080/api/profile/me
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody ProfileRequestVO requestVO) {
        try {
            UserPO updatedUser = userService.updateMyProfile(requestVO);
            // (成功) 轉換為 VO 並回傳
            return ResponseEntity.ok(new ProfileResponseVO(updatedUser));
            
        } catch (AccessDeniedException e) {
            // (安全)
            return ResponseEntity.status(401).body("未登入或 Token 無效"); // 401
        } catch (Exception e) {
            // (其他) e.g., 驗證失敗
            logger.error("更新個人資料失敗", e);
            return ResponseEntity.badRequest().body("更新失敗：" + e.getMessage()); // 400
        }
    }
}
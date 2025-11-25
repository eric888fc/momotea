package demo.bigwork.controller;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.bigwork.dao.UserDAO;
import demo.bigwork.exception.EmailAlreadyExistsException;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.AdminLoginRequestVO;
import demo.bigwork.model.vo.AuthResponseVO;
import demo.bigwork.model.vo.LoginRequestVO;
import demo.bigwork.model.vo.RegisterRequestVO;
import demo.bigwork.model.vo.ResetPasswordRequestVO;
import demo.bigwork.service.UserService;
import demo.bigwork.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(UserService userService,
                          JwtService jwtService,
                          UserDAO userDAO,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    // ========== 註冊 / 驗證碼 ==========

    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Email 格式錯誤");
        }

        try {
            userService.sendVerificationCode(email);
            return ResponseEntity.ok("驗證碼已發送至: " + email);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("發送驗證碼失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("發送驗證碼失敗，請稍後再試");
        }
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody RegisterRequestVO requestVO) {
        try {
            UserPO user = userService.registerBuyer(requestVO);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("買家註冊失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/register/seller")
    public ResponseEntity<?> registerSeller(@Valid @RequestBody RegisterRequestVO requestVO) {
        try {
            UserPO user = userService.registerSeller(requestVO);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("賣家註冊失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || !email.contains("@") || code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Email 或驗證碼格式錯誤");
        }

        try {
            userService.verifyEmail(email, code);
            return ResponseEntity.ok("Email 驗證成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Email 格式錯誤");
        }

        try {
            // 這裡改成呼叫原本就有的 sendVerificationCode
            userService.sendVerificationCode(email);
            return ResponseEntity.ok("新的驗證碼已發送至: " + email);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("重新發送驗證碼失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("重新發送驗證碼失敗，請稍後再試");
        }
    }

    // ========== 一般登入 (BUYER / SELLER) ==========

    /**
     * 統一登入（買家 / 賣家）
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestVO requestVO) {
        try {
            // 1. 呼叫 Service 驗證帳號密碼
            UserPO user = userService.login(requestVO);

            // 2. 產生 JWT Token（統一由 JwtService 處理）
            String token = jwtService.generateToken(user);

            // 3. 回傳包含 token + adminCode 的資訊
            return ResponseEntity.ok(
                    new AuthResponseVO(
                            "登入成功",
                            user.getUserId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole(),
                            token,
                            user.getAdminCode() // 一般使用者通常是 null
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // ========== ADMIN 專用登入 ==========

    /**
     * 系統管理員登入（用管理員原編 + 密碼）
     * POST /api/auth/admin-login
     */
    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody AdminLoginRequestVO requestVO) {
        try {
            // 1. 用 adminCode 找出管理員
            UserPO admin = userDAO.findByAdminCode(requestVO.getAdminCode())
                    .orElseThrow(() -> new BadCredentialsException("管理員編號錯誤"));




            // 2. 比對密碼
            if (!passwordEncoder.matches(requestVO.getPassword(), admin.getPassword())) {
                throw new BadCredentialsException("管理員密碼錯誤");
            }

            // 3. 產生 JWT：subject 一律用 Email，並由 JwtService 統一處理
            String token = jwtService.generateToken(admin);

            // 4. 回傳給前端（共用 AuthResponseVO）
            AuthResponseVO resp = new AuthResponseVO(
                    "登入成功",
                    admin.getUserId(),
                    admin.getName(),
                    admin.getEmail(),
                    admin.getRole(),      // UserRole.ADMIN
                    token,
                    admin.getAdminCode()
            );

            return ResponseEntity.ok(resp);

        } catch (BadCredentialsException e) {
            // 帳號或密碼錯誤 → 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // 其它非預期錯誤 → 500
            logger.error("管理員登入失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("管理員登入失敗，請稍後再試");
        }
    }

    // ========== 忘記密碼 / 重設密碼 (原本就有) ==========

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Email 格式錯誤");
        }

        try {
            userService.createPasswordResetToken(email);
            return ResponseEntity.ok("重設密碼連結已寄出，請檢查信箱。");
        } catch (Exception e) {
            logger.error("建立重設密碼 Token 失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("寄送重設密碼信件失敗");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestVO requestVO) {
        try {
            userService.resetPassword(requestVO.getToken(), requestVO.getNewPassword());
            return ResponseEntity.ok("密碼重設成功，您現在可以用新密碼登入。");
        } catch (Exception e) {
            logger.warn("密碼重設失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
package demo.bigwork.controller;

import jakarta.validation.Valid; // 匯入 @Valid

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.bigwork.exception.EmailAlreadyExistsException;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.AuthResponseVO;
import demo.bigwork.model.vo.LoginRequestVO;
import demo.bigwork.model.vo.RegisterRequestVO;
import demo.bigwork.model.vo.ResetPasswordRequestVO;
import demo.bigwork.service.UserService;
import demo.bigwork.util.JwtUtil;

@RestController // (關鍵) 告訴 Spring 這是一個回傳 JSON 的 Controller
@RequestMapping("/api/auth") // (關鍵) 此 Controller 下的所有 API 都在 /api/auth 路徑下
public class AuthController {

	private final UserService userService;
	private final JwtUtil jwtUtil; // (關鍵) 注入 JwtUtil
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	public AuthController(UserService userService, JwtUtil jwtUtil) {

		// (關鍵 3) 確保你「同時」初始化了這兩個變數
		this.userService = userService;
		this.jwtUtil = jwtUtil;
	}

	// (新)
	/**
	 * API 端點：發送註冊驗證碼 POST http://localhost:8080/api/auth/send-code
	 */
	@PostMapping("/send-code")
	public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody Map<String, String> request) {
		// (教授提醒) 為了只接收 email，我們用 Map 就好，不用再建一個 VO
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
			// (關鍵) 處理郵件發送失敗 (例如你的 Gmail 密碼錯誤)
			logger.error("發送驗證碼失敗", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("郵件伺服器錯誤: " + e.getMessage());
		}
	}

	/**
	 * API 端點：註冊買家 POST http://localhost:8080/api/auth/register/buyer
	 */
	@PostMapping("/register/buyer")
	public ResponseEntity<?> registerBuyer(@Valid @RequestBody RegisterRequestVO requestVO) {
		try {
			UserPO user = userService.registerBuyer(requestVO);
			String token = jwtUtil.generateToken(user);
			// 回傳 201 Created (HTTP 狀態碼)
			return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseVO("買家註冊成功", user.getUserId(),
					user.getName(), user.getEmail(), user.getRole(), token));

		} catch (EmailAlreadyExistsException e) {
			// 回傳 409 Conflict (Email 衝突)
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (Exception e) {
			// 回傳 500 伺服器內部錯誤
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * API 端點：註冊賣家 POST http://localhost:8080/api/auth/register/seller
	 */
	@PostMapping("/register/seller")
	public ResponseEntity<?> registerSeller(@Valid @RequestBody RegisterRequestVO requestVO) {
		try {
			UserPO user = userService.registerSeller(requestVO);
			String token = jwtUtil.generateToken(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseVO("賣家註冊成功", user.getUserId(),
					user.getName(), user.getEmail(), user.getRole(), token));

		} catch (EmailAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * API 端點：統一登入 (此方法已被修改)
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequestVO requestVO) {
		try {
			// 1. (不變) 呼叫 Service 驗證密碼
			UserPO user = userService.login(requestVO);

			// 2. (關鍵-新增)
			// 驗證成功後，呼叫 JwtUtil 生成 Token
			String token = jwtUtil.generateToken(user);

			// 3. (關鍵-修改)
			// 回傳包含 Token 的 AuthResponseVO
			return ResponseEntity.ok(
					new AuthResponseVO("登入成功", user.getUserId(),
							user.getName(),
							user.getEmail(),
							user.getRole(),
							token // <--將Token放入回應																									// 
					));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	/**
	 * API 端點：忘記密碼 (請求重設) POST http://localhost:8080/api/auth/forgot-password
	 */
	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		if (email == null || !email.contains("@")) {
			return ResponseEntity.badRequest().body("Email 格式錯誤");
		}

		try {
			userService.createPasswordResetToken(email);
			// (安全) 永遠回傳 200，不讓駭客知道 Email 是否存在
			return ResponseEntity.ok("如果 Email 存在，一封重設郵件將會寄出。");
		} catch (Exception e) {
			logger.error("請求密碼重設時發生錯誤", e);
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// (新)
	/**
	 * API 端點：執行密碼重設 POST http://localhost:8080/api/auth/reset-password
	 */
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestVO requestVO) {
		try {
			userService.resetPassword(requestVO.getToken(), requestVO.getNewPassword());
			return ResponseEntity.ok("密碼重設成功，您現在可以用新密碼登入。");
		} catch (Exception e) {
			logger.warn("密碼重設失敗: {}", e.getMessage());
			// (安全) 向使用者顯示明確的錯誤
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
package demo.bigwork.controller;

// (刪除) import demo.bigwork.model.po.CartPO; // <-- 不再需要
import demo.bigwork.model.vo.AddCartItemRequestVO;
import demo.bigwork.model.vo.CartResponseVO;
import demo.bigwork.model.vo.UpdateCartItemRequestVO;
import demo.bigwork.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	private static final Logger logger = LoggerFactory.getLogger(CartController.class);
	private final CartService cartService;

	@Autowired
	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	public ResponseEntity<?> getMyCart() {
		try {
			// (修改) Service 直接回傳 VO
			CartResponseVO cartVO = cartService.getMyCart();
			// (修改) Controller 不再需要轉換
			return ResponseEntity.ok(cartVO);

		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@PostMapping("/items")
	public ResponseEntity<?> addOrUpdateItemInMyCart(@Valid @RequestBody AddCartItemRequestVO requestVO) {

		try {
			// (修改) Service 直接回傳 VO
			CartResponseVO updatedCartVO = cartService.addOrUpdateItemInMyCart(requestVO);
			// (修改) Controller 不再需要轉換
			return ResponseEntity.ok(updatedCartVO);

		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			// (關鍵 - 除錯)
			// 我們把你要求的「除錯」log 加到這裡
			logger.error("!!! [CartController] 捕捉到未預期的錯誤 !!!", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PutMapping("/items/{cartItemId}")
	public ResponseEntity<?> updateItemQuantityInMyCart(@PathVariable Long cartItemId,
			@Valid @RequestBody UpdateCartItemRequestVO requestVO) {

		try {
			// (修改) Service 直接回傳 VO
			CartResponseVO updatedCartVO = cartService.updateItemQuantityInMyCart(cartItemId, requestVO.getQuantity());
			// (修改) Controller 不再需要轉換
			return ResponseEntity.ok(updatedCartVO);

		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("!!! [CartController] 更新項目失敗 !!!", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/items/{cartItemId}")
	public ResponseEntity<?> deleteItemFromMyCart(@PathVariable Long cartItemId) {
		try {
			cartService.deleteItemFromMyCart(cartItemId);
			return ResponseEntity.noContent().build();
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@DeleteMapping
	public ResponseEntity<?> clearMyCart() {
		try {
			cartService.clearMyCart();
			return ResponseEntity.noContent().build();
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}
}
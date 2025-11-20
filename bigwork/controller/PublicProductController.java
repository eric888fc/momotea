package demo.bigwork.controller;

import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.vo.ProductResponseVO; // (關鍵) 重用 VO
import demo.bigwork.model.vo.RatingResponseVO;
import demo.bigwork.service.ProductService;
import demo.bigwork.service.RatingService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (新) 公開的商品控制器
 */
@RestController
@RequestMapping("/api/public/products")
public class PublicProductController {

	private static final Logger logger = LoggerFactory.getLogger(PublicProductController.class);
	private final ProductService productService;
	private final RatingService ratingService; // (關鍵) 3. 宣告

	@Autowired
	public PublicProductController(ProductService productService, RatingService ratingService) { // (關鍵) 4. 注入
		this.productService = productService;
		this.ratingService = ratingService; // (關鍵) 5. 賦值
	}

	/**
	 * API 端點：(公開) 查詢所有商品列表 GET http://localhost:8080/api/public/products
	 */
	@GetMapping
	public ResponseEntity<List<ProductResponseVO>> getAllPublicProducts() {

		// 1. 呼叫 Service (公開方法)
		List<ProductPO> products = productService.getAllPublicProducts();

		// 2. (關鍵)
		// 將 List<ProductPO> 轉換為 List<ProductResponseVO>
		// (我們在賣家的 Controller 也做過一樣的事)
		List<ProductResponseVO> responseList = products.stream().map(ProductResponseVO::new) // 呼叫 VO 的 (PO) 建構子
				.collect(Collectors.toList());

		// 3. 回傳 200 OK
		return ResponseEntity.ok(responseList);
	}

	/**
	 * (關鍵新增！) API 端點：(公開)查詢「某個分類下」的所有商品 GET
	 * http://localhost:8080/api/public/products/category/{categoryId}
	 */
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<ProductResponseVO>> getPublicProductsByCategory(@PathVariable Integer categoryId) {

		// 1. 呼叫我們「新增」的 Service 方法
		List<ProductPO> products = productService.getPublicProductsByCategory(categoryId);

		// 2. 轉換為 VO
		List<ProductResponseVO> responseList = products.stream().map(ProductResponseVO::new)
				.collect(Collectors.toList());

		return ResponseEntity.ok(responseList);
	}

	/**
	 * API 端點：(公開) 查詢單一商品詳情 GET
	 * http://localhost:8080/api/public/products/{productId}
	 */
	@GetMapping("/{productId}")
	public ResponseEntity<?> getPublicProductById(@PathVariable Long productId) {
		try {
			// 1. 呼叫 Service (公開方法)
			ProductPO product = productService.getPublicProductById(productId);

			// 2. 轉換為 VO 回傳
			ProductResponseVO responseVO = new ProductResponseVO(product);

			// 3. 回傳 200 OK
			return ResponseEntity.ok(responseVO);

		} catch (EntityNotFoundException e) {
			// (業務) e.g., "找不到商品"
			logger.warn("查詢公開商品失敗：{}", e.getMessage());
			return ResponseEntity.status(404).body(e.getMessage()); // 404

		} catch (Exception e) {
			logger.error("查詢公開商品失敗", e);
			return ResponseEntity.status(500).body(e.getMessage()); // 500
		}
	}

	/**
	 * (新) API 端點：(公開) 查詢「某商品」的所有評價 GET
	 * http://localhost:8080/api/public/products/{productId}/ratings
	 */
	@GetMapping("/{productId}/ratings")
	public ResponseEntity<List<RatingResponseVO>> getRatingsForProduct(@PathVariable Long productId) {

		// (我們不需要驗證商品是否存在，如果不存在，Service 會回傳空列表)
		List<RatingResponseVO> ratings = ratingService.getRatingsForProduct(productId);

		// 回傳 200 OK (可能是空列表 [])
		return ResponseEntity.ok(ratings);
	}
}
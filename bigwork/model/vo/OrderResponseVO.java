package demo.bigwork.model.vo;

import demo.bigwork.model.enums.OrderStatus;
import demo.bigwork.model.po.OrderItemPO;
import demo.bigwork.model.po.OrderPO;
import demo.bigwork.model.po.ProductRatingPO;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (VO) 輸出的「完整」訂單
 */
@Data
public class OrderResponseVO {

	private Long orderId;
	private Long buyerId;
	private String buyerName;
	private Long sellerId;
	private String sellerName;
	private BigDecimal totalPrice;
	private OrderStatus status;
	private Timestamp createdAt;

	// (巢狀) 訂單明細
	private List<OrderItemResponseVO> items;

	/**
	 * (VO) 輸出的「訂單項目」 (這是 OrderResponseVO 的內部類別)
	 */
	@Data
	public static class OrderItemResponseVO {
		private Long orderItemId;
		private Long productId;
		private Integer quantity;
		private BigDecimal pricePerUnit; // (價格快照)

		// --- ( ★ 關鍵新增！ ★ ) ---
		private String productName; // (新增商品名稱)
		private String productImageUrl; // (新增商品圖片 URL)
		// --------------------------
		private Long ratingId; // (如果評價過，這裡會有 ID)
		private Integer currentRatingStars; // (如果評價過，這裡有星數)
		private String currentRatingComment; // (如果評價過，這裡有評論)

		// (OrderItemPO -> VO 轉換器)
		public OrderItemResponseVO(OrderItemPO itemPO) {
			this.orderItemId = itemPO.getOrderItemId();
			this.quantity = itemPO.getQuantity();
			this.pricePerUnit = itemPO.getPricePerUnit();

			// (安全地取得商品資訊)
			if (itemPO.getProduct() != null) {
				this.productId = itemPO.getProduct().getProductId();

				// --- ( ★ 關鍵新增！ ★ ) ---
				// (從`itemPO.getProduct()`中補資料)
				this.productName = itemPO.getProduct().getName();
				this.productImageUrl = itemPO.getProduct().getImageUrl();
				// --------------------------

			} else {
				this.productId = null; // (商品已被刪除)

				// --- ( ★ 關鍵新增！ ★ ) ---
				this.productName = "【商品已失效】";
				this.productImageUrl = null;
				// --------------------------
			}

			ProductRatingPO rating = itemPO.getProductRating();
			if (rating != null) {
				// (已評價過)
				this.ratingId = rating.getRatingId();
				this.currentRatingStars = rating.getRatingStars();
				this.currentRatingComment = rating.getComment();
			} else {
				// (尚未評價)
				this.ratingId = null;
				this.currentRatingStars = null;
				this.currentRatingComment = null;
			}
		}
	}

	/**
	 * (OrderPO -> VO 轉換器) (不變 - 這個建構子已是正確的)
	 */
	public OrderResponseVO(OrderPO orderPO) {
		this.orderId = orderPO.getOrderId();
		this.totalPrice = orderPO.getTotalPrice();
		this.status = orderPO.getStatus();
		this.createdAt = orderPO.getCreatedAt();

		// (安全地取得關聯)
		if (orderPO.getBuyer() != null) {
			this.buyerId = orderPO.getBuyer().getUserId();
			this.buyerName = orderPO.getBuyer().getName();
		}
		if (orderPO.getSeller() != null) {
			this.sellerId = orderPO.getSeller().getUserId();
			this.sellerName = orderPO.getSeller().getName();
		}

		// (關鍵)
		// 轉換「巢狀」的 items
		// (這會觸發 OrderItemPO 的懶載入，並呼叫上面「已更新」的建構子)
		this.items = orderPO.getItems().stream().map(OrderItemResponseVO::new).collect(Collectors.toList());
	}
}
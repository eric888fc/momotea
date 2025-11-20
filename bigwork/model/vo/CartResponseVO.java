package demo.bigwork.model.vo;

import demo.bigwork.model.po.CartItemPO;
import demo.bigwork.model.po.CartPO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (VO) 輸出的「完整」購物車
 */
@Data
public class CartResponseVO {
    
    private Long cartId;
    private Long userId;
    private List<CartItemResponseVO> items; // (巢狀 VO)
    private BigDecimal totalPrice; // (總金額)

    /**
     * (VO) 輸出的「購物車項目」
     * (這是 CartResponseVO 的內部類別或獨立檔案)
     */
    @Data
    public static class CartItemResponseVO {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private String productImageUrl;
        private Integer quantity;
        private BigDecimal unitPrice; // (商品單價)
        private BigDecimal itemTotalPrice; // (此項目總價 = 數量 * 單價)

        // (CartItemPO -> VO 轉換器)
        public CartItemResponseVO(CartItemPO itemPO) {
            this.cartItemId = itemPO.getCartItemId();
            this.quantity = itemPO.getQuantity();

            // (安全地從關聯 PO 中取得商品資訊)
            if (itemPO.getProduct() != null) {
                this.productId = itemPO.getProduct().getProductId();
                this.productName = itemPO.getProduct().getName();
                this.productImageUrl = itemPO.getProduct().getImageUrl();
                this.unitPrice = itemPO.getProduct().getPrice();
            } else {
                // (如果商品被刪除或不存在)
                this.productId = null;
                this.productName = "【商品已失效】";
                this.unitPrice = BigDecimal.ZERO;
            }
            
            // (計算)
            this.itemTotalPrice = this.unitPrice.multiply(new BigDecimal(this.quantity));
        }
    }

    /**
     * (CartPO -> VO 轉換器)
     * 這是最關鍵的轉換器，它會：
     * 1. 轉換所有 items
     * 2. 計算「整張購物車」的總金額
     */
    public CartResponseVO(CartPO cartPO) {
        this.cartId = cartPO.getCartId();
        this.userId = cartPO.getUser().getUserId();
        
        // (1. 轉換所有 items)
        this.items = cartPO.getItems().stream()
                .map(CartItemResponseVO::new) // (呼叫 CartItemResponseVO 的建構子)
                .collect(Collectors.toList());

        // (2. 計算總金額)
        // (使用 Java Stream API)
        this.totalPrice = this.items.stream()
                .map(CartItemResponseVO::getItemTotalPrice) // 取得每個項目的「總價」
                .reduce(BigDecimal.ZERO, BigDecimal::add); // (全部加總)
    }
}
package demo.bigwork.model.vo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * (VO) 用於「新增」或「更新」商品時
 * Controller 接收的 JSON 請求主體 (Body)
 */
@Data
public class ProductRequestVO {

    @NotEmpty(message = "商品名稱不可為空")
    @Size(max = 255, message = "商品名稱過長")
    private String name;

    private String description;

    /**
     * @NotNull: 必須提供
     * @Positive: 必須是正數 (e.g., > 0)
     */
    @NotNull(message = "價格不可為空")
    @Positive(message = "價格必須為正數")
    private BigDecimal price;

    /**
     * @Min(0): 庫存可以是 0，但不能是負數
     */
    @NotNull(message = "庫存不可為空")
    @Min(value = 0, message = "庫存不能為負數")
    private Integer stock;

    /**
     * 賣家必須指定商品要放在「哪個分類」
     */
    @NotNull(message = "分類 ID 不可為空")
    private Integer categoryId;
    
    // (教授提醒)
    // 我們「不需要」 sellerId
    // 因為 Service 會自動從「已登入的 Token」中抓取
}
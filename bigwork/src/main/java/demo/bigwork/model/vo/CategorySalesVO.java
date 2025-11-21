package demo.bigwork.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 熱銷商品類別資訊
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesVO {

    private String categoryName;   // 類別名稱
    private long totalQuantity;    // 銷售總數量
}

package demo.bigwork.model.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequestVO {

    @NotNull(message = "商品 ID 不可為空")
    private Long productId;

    @NotNull(message = "數量不可為空")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;
}
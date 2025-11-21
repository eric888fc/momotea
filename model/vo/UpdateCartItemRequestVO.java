package demo.bigwork.model.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequestVO {

    @NotNull(message = "數量不可為空")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;
}
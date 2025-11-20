package demo.bigwork.model.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRatingRequestVO {

    @NotNull(message = "訂單項目 ID (orderItemId) 不可為空")
    private Long orderItemId;

    @NotNull(message = "評分不可為空")
    @Min(value = 1, message = "評分最低為 1 顆星")
    @Max(value = 5, message = "評分最高為 5 顆星")
    private Integer ratingStars;

    private String comment; // (評論可為空)
}
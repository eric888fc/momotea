package demo.bigwork.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopUpRequestVO {
    
    @NotNull(message = "儲值金額不可為空")
    @Positive(message = "儲值金額必須為正數")
    private BigDecimal amount;
}
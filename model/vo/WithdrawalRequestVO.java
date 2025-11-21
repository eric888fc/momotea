package demo.bigwork.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawalRequestVO {
    
    @NotNull(message = "提款金額不可為空")
    @Positive(message = "提款金額必須為正數")
    private BigDecimal amount;
}
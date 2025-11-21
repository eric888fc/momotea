package demo.bigwork.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系統管理員營運報表（周報 / 季報）＋ 熱銷商品類別
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportVO {

    private LocalDate startDate;
    private LocalDate endDate;

    private long totalOrderCount;
    private BigDecimal totalOrderAmount;
    private BigDecimal averageOrderAmount;

    private long smallOrderCount;
    private long mediumOrderCount;
    private long largeOrderCount;

    private long newBuyerCount;
    private long newSellerCount;
    private long totalNewUserCount;

    /** 熱銷商品類別 Top5（依數量） */
    private List<CategorySalesVO> topCategories;
}

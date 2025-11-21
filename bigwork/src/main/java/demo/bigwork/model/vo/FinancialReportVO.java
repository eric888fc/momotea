package demo.bigwork.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 財務報表 VO：本期 vs 上期（周 / 季）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportVO {

    /** 本期起始日期 */
    private LocalDate currentStartDate;

    /** 本期結束日期 */
    private LocalDate currentEndDate;

    /** 上期起始日期（上一週或上一季） */
    private LocalDate previousStartDate;

    /** 上期結束日期（上一週或上一季） */
    private LocalDate previousEndDate;

    /** 本期營收（完成訂單總金額） */
    private BigDecimal currentRevenue;

    /** 上期營收 */
    private BigDecimal previousRevenue;

    /** 營收差額 = 本期 - 上期 */
    private BigDecimal revenueDiff;

    /** 營收成長率（百分比，單位 %，可能為負） */
    private BigDecimal revenueGrowthRate;

    /** 本期訂單數（完成訂單數） */
    private long currentOrderCount;

    /** 上期訂單數 */
    private long previousOrderCount;

    /** 訂單數差異 = 本期 - 上期 */
    private long orderCountDiff;

    /** 訂單數成長率（百分比，單位 %，可能為負） */
    private BigDecimal orderCountGrowthRate;
}

package demo.bigwork.service;

import java.time.LocalDate;

import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.FinancialReportVO;

public interface AdminReportService {

    /**
     * 給定起訖日期，產生營運報表（周報 / 季報）
     */
    AdminReportVO generateReport(LocalDate startDate, LocalDate endDate);

    /**
     * 產生財務報表：本期 vs 上期
     * period = "weekly" 或 "quarterly"
     */
    FinancialReportVO generateFinancialReport(LocalDate startDate,
                                              LocalDate endDate,
                                              String period);
}

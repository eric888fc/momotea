package demo.bigwork.service;



import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.FinancialReportVO;

import java.time.LocalDate;

public interface AdminReportService {




    AdminReportVO generateReport(LocalDate startDate, LocalDate endDate);

    AdminReportVO generateReportForCurrentWeek();

    AdminReportVO generateReportForCurrentQuarter();

    // 這一行一定要有，而且只收一個 String
    FinancialReportVO generateFinancialReport(String period);

}
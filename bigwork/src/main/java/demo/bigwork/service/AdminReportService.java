package demo.bigwork.service;

import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.FinancialReportVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

public interface AdminReportService {

    AdminReportVO generateReport(LocalDate startDate, LocalDate endDate);

    AdminReportVO generateReportForCurrentWeek();

    AdminReportVO generateReportForCurrentQuarter();

    // 這一行一定要有，而且只收一個 String
    FinancialReportVO generateFinancialReport(String period);
    
    void exportReport(String period, HttpServletResponse response) throws IOException;
}
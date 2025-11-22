package demo.bigwork.controller;

import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.FinancialReportVO;
import demo.bigwork.service.AdminReportService;
import demo.bigwork.service.AuthHelperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final AuthHelperService authHelperService;

    public AdminReportController(AdminReportService adminReportService,
                                 AuthHelperService authHelperService) {
        this.adminReportService = adminReportService;
        this.authHelperService = authHelperService;
    }

    @GetMapping("/weekly")
    public AdminReportVO getWeeklyReport() {
        authHelperService.getCurrentAuthenticatedAdmin(); 
        return adminReportService.generateReportForCurrentWeek();
    }

    @GetMapping("/quarterly")
    public AdminReportVO getQuarterlyReport() {
        authHelperService.getCurrentAuthenticatedAdmin(); 
        return adminReportService.generateReportForCurrentQuarter();
    }

    @GetMapping("/financial")
    public FinancialReportVO getFinancialReport(@RequestParam String period) {
        authHelperService.getCurrentAuthenticatedAdmin(); 
        return adminReportService.generateFinancialReport(period);
    }
}
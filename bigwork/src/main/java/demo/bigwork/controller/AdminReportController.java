package demo.bigwork.controller;

import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.FinancialReportVO;
import demo.bigwork.service.AdminReportService;
import demo.bigwork.service.AuthHelperService;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    // ===== 本週營運報表 =====
    @GetMapping("/weekly")
    public AdminReportVO getWeeklyReport() {
        // 確認目前登入的是 ADMIN，否則丟 AccessDeniedException → 403
        authHelperService.getCurrentAuthenticatedAdmin();
        return adminReportService.generateReportForCurrentWeek();
    }

    // ===== 本季營運報表 =====
    @GetMapping("/quarterly")
    public AdminReportVO getQuarterlyReport() {
        authHelperService.getCurrentAuthenticatedAdmin();
        return adminReportService.generateReportForCurrentQuarter();
    }

    // ===== 財務比較 (本期 vs 上期) =====
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportVO> getFinancialReport(@RequestParam String period) {
        // 這裡也要檢查 ADMIN，否則一般登入的人也能看財務報表
        authHelperService.getCurrentAuthenticatedAdmin();

        try {
            FinancialReportVO vo = adminReportService.generateFinancialReport(period);
            return ResponseEntity.ok(vo);
        } catch (IllegalArgumentException e) {
            // period 不是 weekly / quarterly 時丟 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    // ===== 匯出 Excel 報表 =====
    @GetMapping("/export")
    public void exportReport(@RequestParam String period,
                             HttpServletResponse response) {
        // 匯出 Excel 一樣限制 ADMIN
        authHelperService.getCurrentAuthenticatedAdmin();

        try {
            adminReportService.exportReport(period, response);
        } catch (IllegalArgumentException e) {
            // 不支援的 period
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            // 寫檔失敗
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "匯出報表失敗",
                    e
            );
        }
    }
}
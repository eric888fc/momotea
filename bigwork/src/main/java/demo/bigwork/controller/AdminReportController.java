package demo.bigwork.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.CategorySalesVO;
import demo.bigwork.service.AdminReportService;
import demo.bigwork.service.AuthHelperService;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    @Autowired
    private AuthHelperService authHelperService;

    @Autowired
    private AdminReportService adminReportService;

    /**
     * 周報表
     * GET /api/admin/reports/weekly
     * 可選 ?weekStart=2025-11-17，沒給就抓本週一
     */
    @GetMapping("/weekly")
    public AdminReportVO getWeeklyReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {

        // 確認呼叫者是 ADMIN（沒權限會直接拋例外）
        UserPO admin = authHelperService.getCurrentAuthenticatedAdmin();

        LocalDate start = (weekStart != null)
                ? weekStart
                : LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);

        return adminReportService.generateReport(start, end);
    }

    /**
     * 季報表
     * GET /api/admin/reports/quarterly
     * 可選 ?year=2025&quarter=4，沒給就用今年、當季
     */
    @GetMapping("/quarterly")
    public AdminReportVO getQuarterlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter) {

        UserPO admin = authHelperService.getCurrentAuthenticatedAdmin();

        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int q = (quarter != null) ? quarter : ((now.getMonthValue() - 1) / 3 + 1);

        int startMonth = (q - 1) * 3 + 1;
        LocalDate start = LocalDate.of(y, startMonth, 1);
        LocalDate end = start.plusMonths(3).minusDays(1);

        return adminReportService.generateReport(start, end);
    }
    
    /**
     * 匯出目前報表為 Excel (XLSX)
     * GET /api/admin/reports/export?period=weekly|quarterly
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(@RequestParam String period) {

        // 1. 驗證目前登入的是 ADMIN
        authHelperService.getCurrentAuthenticatedAdmin();

        // 2. 決定要匯出的起訖日期（跟 weekly/quarterly API 同邏輯）
        LocalDate startDate;
        LocalDate endDate;

        if ("weekly".equalsIgnoreCase(period)) {
            // 這週一 ~ 週日
            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
            startDate = monday;
            endDate = monday.plusDays(6);
        } else if ("quarterly".equalsIgnoreCase(period)) {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int q = (now.getMonthValue() - 1) / 3 + 1;   // 第幾季
            int startMonth = (q - 1) * 3 + 1;            // 1,4,7,10
            startDate = LocalDate.of(year, startMonth, 1);
            endDate = startDate.plusMonths(3).minusDays(1); // 該季最後一天
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        // 3. 產生營運報表資料（裡面已經含 Top 類別）
        AdminReportVO report = adminReportService.generateReport(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Report");

            int rowIdx = 0;

            // ===== 報表標題 & 期間 =====
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue(
                    "系統管理員" + ("weekly".equalsIgnoreCase(period) ? "週報表" : "季報表"));

            Row rangeRow = sheet.createRow(rowIdx++);
            rangeRow.createCell(0).setCellValue(
                    "期間：" + report.getStartDate() + " ~ " + report.getEndDate());

            rowIdx++; // 空一行

            // ===== 營運數字 Summary =====
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.createCell(0).setCellValue("項目");
            headerRow.createCell(1).setCellValue("數值");

            Row r1 = sheet.createRow(rowIdx++);
            r1.createCell(0).setCellValue("訂單總數（完成）");
            r1.createCell(1).setCellValue(report.getTotalOrderCount());

            Row r2 = sheet.createRow(rowIdx++);
            r2.createCell(0).setCellValue("訂單總金額");
            r2.createCell(1).setCellValue(
                    report.getTotalOrderAmount() != null
                            ? report.getTotalOrderAmount().doubleValue()
                            : 0.0);

            Row r3 = sheet.createRow(rowIdx++);
            r3.createCell(0).setCellValue("平均訂單金額");
            r3.createCell(1).setCellValue(
                    report.getAverageOrderAmount() != null
                            ? report.getAverageOrderAmount().doubleValue()
                            : 0.0);

            Row r4 = sheet.createRow(rowIdx++);
            r4.createCell(0).setCellValue("小額訂單數 (≤500)");
            r4.createCell(1).setCellValue(report.getSmallOrderCount());

            Row r5 = sheet.createRow(rowIdx++);
            r5.createCell(0).setCellValue("中額訂單數 (500~2000)");
            r5.createCell(1).setCellValue(report.getMediumOrderCount());

            Row r6 = sheet.createRow(rowIdx++);
            r6.createCell(0).setCellValue("大額訂單數 (≥2000)");
            r6.createCell(1).setCellValue(report.getLargeOrderCount());

            Row r7 = sheet.createRow(rowIdx++);
            r7.createCell(0).setCellValue("新增買家數");
            r7.createCell(1).setCellValue(report.getNewBuyerCount());

            Row r8 = sheet.createRow(rowIdx++);
            r8.createCell(0).setCellValue("新增賣家數");
            r8.createCell(1).setCellValue(report.getNewSellerCount());

            Row r9 = sheet.createRow(rowIdx++);
            r9.createCell(0).setCellValue("新增會員總數");
            r9.createCell(1).setCellValue(report.getTotalNewUserCount());

            // ===== 熱銷商品類別 Top5 =====
            rowIdx += 2; // 空兩行
            Row catTitle = sheet.createRow(rowIdx++);
            catTitle.createCell(0).setCellValue("熱銷商品類別 Top 5（依銷售數量）");

            Row catHeader = sheet.createRow(rowIdx++);
            catHeader.createCell(0).setCellValue("排行");
            catHeader.createCell(1).setCellValue("類別名稱");
            catHeader.createCell(2).setCellValue("銷售總數量");

            int rank = 1;
            if (report.getTopCategories() != null) {
                for (CategorySalesVO cat : report.getTopCategories()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rank++);
                    row.createCell(1).setCellValue(cat.getCategoryName());
                    row.createCell(2).setCellValue(cat.getTotalQuantity());
                }
            }

            // 欄寬自動調整
            for (int i = 0; i <= 2; i++) {
                sheet.autoSizeColumn(i);
            }

            // 4. 轉成 byte[]
            workbook.write(baos);
            byte[] bytes = baos.toByteArray();

            // 5. 組 Response（讓瀏覽器下載）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            String fileName = "weekly".equalsIgnoreCase(period)
                    ? "weekly-report.xlsx"
                    : "quarterly-report.xlsx";

            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"");

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            // 發生例外的話回 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    
}


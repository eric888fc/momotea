package demo.bigwork.service.Impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import demo.bigwork.dao.OrderDAO;
import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.enums.OrderStatus;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.vo.AdminReportVO;
import demo.bigwork.model.vo.CategorySalesVO;
import demo.bigwork.model.vo.FinancialReportVO;
import demo.bigwork.service.AdminReportService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AdminReportServiceImpl implements AdminReportService {

    private final OrderDAO orderDAO;
    private final UserDAO userDAO;

    @Autowired
    public AdminReportServiceImpl(OrderDAO orderDAO, UserDAO userDAO) {
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
    }

    /**
     * 產生一個區間的營運報表（周報 / 季報共用，含 Top 類別）
     */
    @Override
    public AdminReportVO generateReport(LocalDate startDate, LocalDate endDate) {

        Timestamp startTs = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endExclusiveTs = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());

        // ===== 1. 訂單 Summary（一次 SQL 取得全部統計） =====
        List<Object[]> summaryList = orderDAO.findOrderSummary(
                startTs, endExclusiveTs, OrderStatus.COMPLETED.name());

        // 沒有任何訂單時給一組預設值
        Object[] row;
        if (summaryList == null || summaryList.isEmpty()) {
            row = new Object[]{0L, BigDecimal.ZERO, 0L, 0L, 0L};
        } else {
            row = summaryList.get(0);
        }

        long totalOrderCount   = row[0] == null ? 0L : ((Number) row[0]).longValue();
        BigDecimal totalAmount = (BigDecimal) row[1];

        long smallOrderCount   = row[2] == null ? 0L : ((Number) row[2]).longValue();
        long mediumOrderCount  = row[3] == null ? 0L : ((Number) row[3]).longValue();
        long largeOrderCount   = row[4] == null ? 0L : ((Number) row[4]).longValue();

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        BigDecimal averageOrderAmount = BigDecimal.ZERO;
        if (totalOrderCount > 0) {
            averageOrderAmount = totalAmount
                    .divide(BigDecimal.valueOf(totalOrderCount), 2, RoundingMode.HALF_UP);
        }

        // ===== 2. 新增會員數 =====
        long newBuyerCount = userDAO.countNewUsersByRoleBetween(
                startTs, endExclusiveTs, UserRole.BUYER);
        long newSellerCount = userDAO.countNewUsersByRoleBetween(
                startTs, endExclusiveTs, UserRole.SELLER);
        long totalNewUserCount = newBuyerCount + newSellerCount;

        // ===== 3. 熱銷商品類別 Top5（哪個商品類別賣得多） =====
        List<Object[]> catRows = orderDAO.findTopCategoriesByQuantity(
                startTs, endExclusiveTs, OrderStatus.COMPLETED.name());
        List<CategorySalesVO> topCategories = new ArrayList<>();
        if (catRows != null) {
            for (Object[] r : catRows) {
                String catName = (String) r[0];
                long qty = ((Number) r[1]).longValue();
                topCategories.add(new CategorySalesVO(catName, qty));
            }
        }

        // ===== 4. 組成 VO 回傳 =====
        AdminReportVO vo = new AdminReportVO();
        vo.setStartDate(startDate);
        vo.setEndDate(endDate);
        vo.setTotalOrderCount((int) totalOrderCount);
        vo.setTotalOrderAmount(totalAmount);
        vo.setAverageOrderAmount(averageOrderAmount);
        vo.setSmallOrderCount(smallOrderCount);
        vo.setMediumOrderCount(mediumOrderCount);
        vo.setLargeOrderCount(largeOrderCount);
        vo.setNewBuyerCount(newBuyerCount);
        vo.setNewSellerCount(newSellerCount);
        vo.setTotalNewUserCount(totalNewUserCount);
        vo.setTopCategories(topCategories);

        return vo;
    }

    /**
     * 財務對比報表（本期 vs 上期）
     * period = "weekly" 或 "quarterly"
     */
    @Override
    public FinancialReportVO generateFinancialReport(String period) {

        // === 1. 計算「本期」起訖日期 ===
        LocalDate today = LocalDate.now();
        LocalDate currentStartDate;
        LocalDate currentEndDate;

        if ("weekly".equalsIgnoreCase(period)) {
            // 本週：週一 ~ 週日
            currentStartDate = today.with(DayOfWeek.MONDAY);
            currentEndDate   = today.with(DayOfWeek.SUNDAY);
        } else if ("quarterly".equalsIgnoreCase(period)) {
            // 本季：1–3、4–6、7–9、10–12
            int quarterIndex = (today.getMonthValue() - 1) / 3;   // 0,1,2,3
            Month firstMonth = Month.of(quarterIndex * 3 + 1);    // 1,4,7,10

            currentStartDate = LocalDate.of(today.getYear(), firstMonth, 1);
            currentEndDate   = currentStartDate.plusMonths(3).minusDays(1);
        } else {
            throw new IllegalArgumentException("period 必須為 weekly 或 quarterly");
        }

        // === 2. 本期報表（使用你原本的 generateReport） ===
        AdminReportVO current = generateReport(currentStartDate, currentEndDate);

        // === 3. 上期起訖日期 ===
        LocalDate previousStartDate;
        LocalDate previousEndDate;

        if ("weekly".equalsIgnoreCase(period)) {
            // 上一週
            previousStartDate = currentStartDate.minusWeeks(1);
            previousEndDate   = currentEndDate.minusWeeks(1);
        } else {
            // 上一季
            previousStartDate = currentStartDate.minusMonths(3);
            previousEndDate   = currentEndDate.minusMonths(3);
        }

        // === 4. 上期報表 ===
        AdminReportVO previous = generateReport(previousStartDate, previousEndDate);

        // === 5. 組成 FinancialReportVO（完全照你給的 VO 欄位） ===
        FinancialReportVO vo = new FinancialReportVO();

        // 日期
        vo.setCurrentStartDate(currentStartDate);
        vo.setCurrentEndDate(currentEndDate);
        vo.setPreviousStartDate(previousStartDate);
        vo.setPreviousEndDate(previousEndDate);

        // 避免 null
        BigDecimal currentRevenue  = current.getTotalOrderAmount()  != null
                ? current.getTotalOrderAmount()
                : BigDecimal.ZERO;
        BigDecimal previousRevenue = previous.getTotalOrderAmount() != null
                ? previous.getTotalOrderAmount()
                : BigDecimal.ZERO;

        vo.setCurrentRevenue(currentRevenue);
        vo.setPreviousRevenue(previousRevenue);

        // 營收差額
        BigDecimal revenueDiff = currentRevenue.subtract(previousRevenue);
        vo.setRevenueDiff(revenueDiff);

        // 營收成長率 (%)
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal revenueGrowthRate = revenueDiff
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previousRevenue, 2, RoundingMode.HALF_UP);
            vo.setRevenueGrowthRate(revenueGrowthRate);
        } else {
            vo.setRevenueGrowthRate(BigDecimal.ZERO);
        }

        // 訂單數（用 AdminReportVO 的 totalOrderCount）
        long currentOrderCount  = current.getTotalOrderCount();
        long previousOrderCount = previous.getTotalOrderCount();

        vo.setCurrentOrderCount(currentOrderCount);
        vo.setPreviousOrderCount(previousOrderCount);

        long orderCountDiff = currentOrderCount - previousOrderCount;
        vo.setOrderCountDiff(orderCountDiff);

        // 訂單數成長率 (%)
        if (previousOrderCount > 0) {
            BigDecimal orderCountGrowthRate = BigDecimal.valueOf(orderCountDiff)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(previousOrderCount), 2, RoundingMode.HALF_UP);
            vo.setOrderCountGrowthRate(orderCountGrowthRate);
        } else {
            vo.setOrderCountGrowthRate(BigDecimal.ZERO);
        }

        // 你 VO 裡還有 currentOrders / previousOrders 兩個 int 欄位，一併設定
        vo.setCurrentOrders((int) currentOrderCount);
        vo.setPreviousOrders((int) previousOrderCount);

        return vo;
    }

    @Override
    public AdminReportVO generateReportForCurrentWeek() {
        LocalDate today = LocalDate.now();

        // 以「本週一 ~ 本週日」為範圍
        LocalDate start = today.with(DayOfWeek.MONDAY);
        LocalDate end   = today.with(DayOfWeek.SUNDAY);

        return generateReport(start, end);
    }

    @Override
    public AdminReportVO generateReportForCurrentQuarter() {
        LocalDate today = LocalDate.now();

        // 算出本季的第一個月：1、4、7、10
        int quarterIndex = (today.getMonthValue() - 1) / 3; // 0,1,2,3
        Month firstMonthOfQuarter = Month.of(quarterIndex * 3 + 1);

        LocalDate start = LocalDate.of(today.getYear(), firstMonthOfQuarter, 1);
        LocalDate end   = start.plusMonths(3).minusDays(1); // 該季最後一天

        return generateReport(start, end);
    }
    @Override
    public void exportReport(String period, HttpServletResponse response) throws IOException {
        // 1. 先決定要用哪一份報表資料
        AdminReportVO report;
        String fileName;

        if ("weekly".equalsIgnoreCase(period)) {
            report = generateReportForCurrentWeek();
            fileName = "週報表.xlsx";
        } else if ("quarterly".equalsIgnoreCase(period)) {
            report = generateReportForCurrentQuarter();
            fileName = "季報表.xlsx";
        } else {
            // 這裡只丟 IllegalArgumentException（會變成 400），不要丟 AccessDeniedException
            throw new IllegalArgumentException("不支援的 period: " + period);
        }

        // 2. 用 Apache POI 組 Excel
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("營運報表");

            int r = 0;
            Row row;

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("期間");
            row.createCell(1).setCellValue(report.getStartDate() + " ~ " + report.getEndDate());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("訂單總數");
            row.createCell(1).setCellValue(report.getTotalOrderCount());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("訂單總金額");
            row.createCell(1).setCellValue(report.getTotalOrderAmount().doubleValue());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("平均訂單金額");
            row.createCell(1).setCellValue(report.getAverageOrderAmount().doubleValue());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("小額訂單數(≤500)");
            row.createCell(1).setCellValue(report.getSmallOrderCount());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("中額訂單數(500-2000)");
            row.createCell(1).setCellValue(report.getMediumOrderCount());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("大額訂單數(≥2000)");
            row.createCell(1).setCellValue(report.getLargeOrderCount());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("新增買家數");
            row.createCell(1).setCellValue(report.getNewBuyerCount());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("新增賣家數");
            row.createCell(1).setCellValue(report.getNewSellerCount());

         // 3. 設定 Response Header
            response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");

            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedName);
            ServletOutputStream out = response.getOutputStream();

            // 4. 寫出檔案
            wb.write(out);
            out.flush();

           }
    }
}
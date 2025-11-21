package demo.bigwork.service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

        long totalOrderCount   = ((Number) row[0]).longValue();
        BigDecimal totalAmount = (BigDecimal) row[1];
        long smallOrderCount   = ((Number) row[2]).longValue();
        long mediumOrderCount  = ((Number) row[3]).longValue();
        long largeOrderCount   = ((Number) row[4]).longValue();

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
        vo.setTotalOrderCount(totalOrderCount);
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
    public FinancialReportVO generateFinancialReport(LocalDate startDate,
                                                     LocalDate endDate,
                                                     String period) {

        // 本期：直接用上面的 generateReport
        AdminReportVO current = generateReport(startDate, endDate);

        // 上一期：依 weekly / quarterly 回推一段時間
        LocalDate prevStart;
        LocalDate prevEnd;

        if ("weekly".equalsIgnoreCase(period)) {
            prevStart = startDate.minusWeeks(1);
            prevEnd   = endDate.minusWeeks(1);
        } else { // quarterly
            prevStart = startDate.minusMonths(3);
            prevEnd   = endDate.minusMonths(3);
        }

        AdminReportVO previous = generateReport(prevStart, prevEnd);

        BigDecimal curRevenue = current.getTotalOrderAmount();
        BigDecimal prevRevenue = previous.getTotalOrderAmount();
        if (curRevenue == null) curRevenue = BigDecimal.ZERO;
        if (prevRevenue == null) prevRevenue = BigDecimal.ZERO;

        BigDecimal revDiff = curRevenue.subtract(prevRevenue);
        BigDecimal revGrowth = BigDecimal.ZERO;
        if (prevRevenue.compareTo(BigDecimal.ZERO) != 0) {
            revGrowth = revDiff
                    .divide(prevRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        long curCnt = current.getTotalOrderCount();
        long prevCnt = previous.getTotalOrderCount();
        long cntDiff = curCnt - prevCnt;
        BigDecimal cntGrowth = BigDecimal.ZERO;
        if (prevCnt > 0) {
            cntGrowth = BigDecimal.valueOf(cntDiff * 100.0 / prevCnt)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        FinancialReportVO vo = new FinancialReportVO();
        vo.setCurrentStartDate(startDate);
        vo.setCurrentEndDate(endDate);
        vo.setPreviousStartDate(prevStart);
        vo.setPreviousEndDate(prevEnd);

        vo.setCurrentRevenue(curRevenue);
        vo.setPreviousRevenue(prevRevenue);
        vo.setRevenueDiff(revDiff);
        vo.setRevenueGrowthRate(revGrowth);

        vo.setCurrentOrderCount(curCnt);
        vo.setPreviousOrderCount(prevCnt);
        vo.setOrderCountDiff(cntDiff);
        vo.setOrderCountGrowthRate(cntGrowth);

        return vo;
    }
}



package demo.bigwork.dao;

import demo.bigwork.model.po.OrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderDAO extends JpaRepository<OrderPO, Long> {

    // 給前台 / 買家 / 賣家用的原本查詢
    List<OrderPO> findByBuyer_UserId(Long buyerId);

    List<OrderPO> findBySeller_UserId(Long sellerId);

    /**
     * 訂單整體統計 Summary：
     *  [0]=Long total_count        訂單總數
     *  [1]=BigDecimal total_amount 訂單總金額
     *  [2]=Long small_count        小額訂單數 (<=500)
     *  [3]=Long medium_count       中額訂單數 (500~2000)
     *  [4]=Long large_count        大額訂單數 (>2000)
     */
    @Query(value =
            "SELECT " +
            "  COUNT(*) AS total_count, " +
            "  COALESCE(SUM(o.total_price),0) AS total_amount, " +
            "  SUM(CASE WHEN o.total_price <= 500 THEN 1 ELSE 0 END) AS small_count, " +
            "  SUM(CASE WHEN o.total_price > 500 AND o.total_price <= 2000 THEN 1 ELSE 0 END) AS medium_count, " +
            "  SUM(CASE WHEN o.total_price > 2000 THEN 1 ELSE 0 END) AS large_count " +
            "FROM orders o " +
            "WHERE o.created_at >= :start " +
            "  AND o.created_at < :end " +
            "  AND o.status = :status",
            nativeQuery = true)
    List<Object[]> findOrderSummary(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("status") String status);

    /**
     * 熱銷商品類別 Top5（依銷售數量）
     * 每列 Object[]: [0]=String category_name, [1]=Long total_qty
     */
    @Query(value =
            "SELECT c.name AS category_name, SUM(oi.quantity) AS total_qty " +
            "FROM orders o " +
            "JOIN order_items oi ON o.order_id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "JOIN categories c ON p.category_id = c.category_id " +
            "WHERE o.created_at >= :start " +
            "  AND o.created_at < :end " +
            "  AND o.status = :status " +
            "GROUP BY c.category_id, c.name " +
            "ORDER BY total_qty DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> findTopCategoriesByQuantity(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("status") String status);
}


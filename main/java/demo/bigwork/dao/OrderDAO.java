package demo.bigwork.dao;

import demo.bigwork.model.po.OrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDAO extends JpaRepository<OrderPO, Long> {

    /**
     * 查詢某個「買家」的所有訂單
     * "SELECT * FROM orders WHERE buyer_id = ?"
     */
    List<OrderPO> findByBuyer_UserId(Long buyerId);

    /**
     * 查詢某個「賣家」的所有訂單
     * "SELECT * FROM orders WHERE seller_id = ?"
     */
    List<OrderPO> findBySeller_UserId(Long sellerId);
}
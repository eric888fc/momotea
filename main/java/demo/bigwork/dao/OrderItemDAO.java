package demo.bigwork.dao;

import demo.bigwork.model.po.OrderItemPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemDAO extends JpaRepository<OrderItemPO, Long> {
    // (目前不需要客製化查詢)
    // (我們將透過 OrderPO 來存取 OrderItemPO)
}
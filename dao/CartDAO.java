package demo.bigwork.dao;

import demo.bigwork.model.po.CartPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartDAO extends JpaRepository<CartPO, Long> {

    /**
     * (關鍵) 透過「使用者 ID」查詢購物車
     *
     * "SELECT * FROM carts WHERE user_id = ?"
     */
    Optional<CartPO> findByUser_UserId(Long userId);
}
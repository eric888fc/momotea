package demo.bigwork.dao;

import demo.bigwork.model.po.WalletPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletDAO extends JpaRepository<WalletPO, Long> {
    
    // "SELECT * FROM wallets WHERE user_id = ?"
	Optional<WalletPO> findByUser_UserId(Long userId);
}
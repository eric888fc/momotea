package demo.bigwork.dao;

import demo.bigwork.model.po.WalletTransactionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WalletTransactionDAO extends JpaRepository<WalletTransactionPO, Long> {

    // 查詢某個錢包的「所有」交易紀錄 (e.g., "SELECT * FROM ... WHERE wallet_id = ?")
    List<WalletTransactionPO> findByWallet_WalletId(Long walletId);
}
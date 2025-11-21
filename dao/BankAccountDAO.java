package demo.bigwork.dao;

import demo.bigwork.model.po.BankAccountPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository (DAO) for BankAccountPO
 *
 * (關鍵) JpaRepository<BankAccountPO, Long>
 * - BankAccountPO: 這個 DAO 是管 `BankAccountPO` 的
 * - Long:            `BankAccountPO` 的主鍵 (accountId) 型別是 `Long`
 */
@Repository
public interface BankAccountDAO extends JpaRepository<BankAccountPO, Long> {

    /**
     * (關鍵 - 方法名稱查詢)
     * 我們「非常」需要透過「賣家 ID」來查詢帳戶
     *
     * "SELECT * FROM bank_accounts WHERE user_id = ?"
     */
	Optional<BankAccountPO> findByUser_UserId(Long userId);
}
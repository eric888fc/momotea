package demo.bigwork.dao;

import org.springframework.data.jpa.repository.JpaRepository; // 匯入 Spring Data JPA
import org.springframework.stereotype.Repository; // 標記為 Spring Bean

import demo.bigwork.model.po.UserPO;

import java.util.Optional; // 使用 Java 8 的 Optional 來處理 "null"

/**
 * DAO / Repository 介面 (使用 Spring Data JPA)
 *
 * @Repository 告訴 Spring：這是一個資料存取層的 Bean (元件)。
 *
 * (關鍵！) 我們「繼承 JpaRepository<UserPO, Long>」
 * 1. UserPO: 告訴 JPA 這個 Repository 是用來操作「哪個 PO」。
 * 2. Long: 告訴 JPA 這個 PO 的「主鍵 (Primary Key) 是什麼型別」。
 *
 * (效果)
 * 只要你這樣繼承，你就「立即免費獲得」了以下所有方法，
 * Spring Boot 會自動幫你實作：
 *
 * - save(UserPO user) : 新增 (INSERT) 或 更新 (UPDATE) 使用者
 * - findById(Long userId) : 透過 ID 查詢 (SELECT ... WHERE user_id = ?)
 * - deleteById(Long userId) : 透過 ID 刪除 (DELETE ... WHERE user_id = ?)
 * - findAll() : 查詢所有使用者 (SELECT * FROM users)
 * - count() : 計算總筆數 (SELECT COUNT(*) FROM users)
 * - ... 以及更多
 *
 */
@Repository
public interface UserDAO extends JpaRepository<UserPO, Long> {

    // --- 自定義查詢 (Custom Finder Methods) ---

    /**
     * Spring Data JPA 最強大的功能：「方法名稱查詢 (Derived Query Methods)」
     *
     * 你不需要寫任何 SQL！
     * 你只需要「依照 JPA 的命名規範」來定義方法名稱，
     * Spring Boot 就會自動分析方法名，並幫你生成對應的 SQL 查詢。
     *
     * 範例：
     * "findByEmail" -> Spring Boot 會自動生成
     * "SELECT * FROM users WHERE email = ?"
     *
     * @param email 要查詢的 Email
     * @return Java 8 的 Optional 物件，用來安全地處理「可能找到」或「找不到 (null)」的情況
     */
    Optional<UserPO> findByEmail(String email);

    /**
     * (範例) 我們也可以用 "existsBy..." 來檢查資料是否存在
     *
     * "existsByEmail" -> Spring Boot 會自動生成
     * "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM users WHERE email = ?"
     *
     * 這在註冊時用來檢查 Email 是否重複，效能比 findByEmail 更好 (因為它不需要回傳整個 UserPO 物件)
     *
     * @param email 要檢查的 Email
     * @return true 如果存在，false 如果不存在
     */
    boolean existsByEmail(String email);
    
    // 你可以繼續新增，例如：
    // List<UserPO> findByName(String name); // 查詢所有同名同姓的人
    // List<UserPO> findByCreatedAtAfter(Timestamp time); // 查詢某個時間點後註冊的人
}
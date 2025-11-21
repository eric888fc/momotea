package demo.bigwork.dao;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;

@Repository
public interface UserDAO extends JpaRepository<UserPO, Long> {

    Optional<UserPO> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserPO> findByAdminCode(String adminCode);

    /** 指定期間某角色的新會員數 */
    @Query("SELECT COALESCE(COUNT(u), 0) " +
           "FROM UserPO u " +
           "WHERE u.createdAt >= :start " +
           "  AND u.createdAt < :end " +
           "  AND u.role = :role")
    long countNewUsersByRoleBetween(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("role") UserRole role);
}

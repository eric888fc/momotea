package demo.bigwork.dao;

import demo.bigwork.model.po.PasswordResetTokenPO;
import demo.bigwork.model.po.UserPO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenDAO extends JpaRepository<PasswordResetTokenPO, Long> {
    
    // (Spring Data JPA) 自動生成 SQL: 
    // "SELECT * FROM password_reset_tokens WHERE token = ?"
    Optional<PasswordResetTokenPO> findByToken(String token);
    
    @Transactional
    void deleteByUser(UserPO user);
}
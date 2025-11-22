package demo.bigwork.service.Impl;

import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.po.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * (關鍵) 實作 Spring Security 的 UserDetailsService
 * 職責：專門從資料庫讀取使用者資訊 (PO)，
 * 並將其轉換為 Spring Security 認識的 UserDetails 物件
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;

    @Autowired
    public UserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Spring Security 在驗證 Token 時，會呼叫此方法
     * (注意：在我們的設計中，"username" 就是 "email")
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 呼叫我們現有的 DAO，透過 Email 尋找使用者
        UserPO userPO = userDAO.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + email));

        // 2. (關鍵) 將我們的角色 (e.g., "BUYER") 轉換為 Spring Security 的 "權限"
        // (教授提醒) Spring Security 預期角色名稱有一個 "ROLE_" 前綴
        String roleName = "ROLE_" + userPO.getRole().name();
        Collection<GrantedAuthority> authorities = 
                Collections.singletonList(new SimpleGrantedAuthority(roleName));

        // 3. (關鍵) 回傳 Spring Security 的 "User" 物件
        // 它包含了 Email(帳號)、Hashed 密碼、以及權限
        return new User(
            userPO.getEmail(),         // (帳號)
            userPO.getPassword(),      // (密碼)
            authorities                // (權限列表)
        );
    }
}
package demo.bigwork.service.Impl;

import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;

    @Autowired
    public UserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ❗ 這裡的 username 一律當作「Email」
        UserPO user = userDAO.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在: " + username));

        // 建立角色權限：ROLE_ADMIN / ROLE_BUYER / ROLE_SELLER
        String roleName = "ROLE_" + user.getRole().name();  // e.g. ROLE_ADMIN
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

        // ❗ 這裡的 username 也一律放「Email」
        return new User(
                user.getEmail(),        // username
                user.getPassword(),     // hashed password
                authorities
        );
    }
}
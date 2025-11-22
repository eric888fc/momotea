package demo.bigwork.service;

import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 通用登入身分輔助工具：
 * - 取得目前登入的使用者
 * - 檢查是否為 BUYER / SELLER / ADMIN
 */
@Component
public class AuthHelperService {

    private final UserDAO userDAO;

    @Autowired
    public AuthHelperService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 取得「當前已登入」的使用者 (不限角色)
     *
     * @return UserPO
     * @throws AccessDeniedException 如果未登入或 Token 無效
     */
    public UserPO getCurrentAuthenticatedUser() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // (安全) 檢查是否為 null、未驗證、或是「匿名」Token
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("未登入或 Token 無效");
        }

        // 在你的設計中 authentication.getName() = Email
        String username = authentication.getName();

        return userDAO.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Token 有效，但使用者不存在: " + username));
    }

    /**
     * 取得「當前已登入」的賣家 (SELLER)
     */
    public UserPO getCurrentAuthenticatedSeller() throws AccessDeniedException {
        UserPO user = getCurrentAuthenticatedUser();

        if (user.getRole() != UserRole.SELLER) {
            throw new AccessDeniedException("只有賣家 (SELLER) 才能執行此操作");
        }
        return user;
    }

    /**
     * 取得「當前已登入」的買家 (BUYER)
     */
    public UserPO getCurrentAuthenticatedBuyer() throws AccessDeniedException {
        UserPO user = getCurrentAuthenticatedUser();

        if (user.getRole() != UserRole.BUYER) {
            throw new AccessDeniedException("只有買家 (BUYER) 才能執行此操作");
        }
        return user;
    }

    /**
     * ✅ 新增：取得「當前已登入」的系統管理員 (ADMIN)
     */
    public UserPO getCurrentAuthenticatedAdmin() throws AccessDeniedException {
        UserPO user = getCurrentAuthenticatedUser();

        if (user.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("只有系統管理員 (ADMIN) 才能執行此操作");
        }
        return user;
    }
}
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
 * (關鍵) @Component
 * 這是一個通用的輔助工具 Bean，
 * 專門用來處理「從 SecurityContext 取得使用者」的重複任務
 */
@Component
public class AuthHelperService {
    
    private final UserDAO userDAO;

    @Autowired
    public AuthHelperService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * (新) 取得「當前已登入」的使用者 (不限角色)
     *
     * @return UserPO 
     * @throws AccessDeniedException 如果未登入
     */
    public UserPO getCurrentAuthenticatedUser() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // (安全) 
        // 檢查是否為 null、未驗證、或是「匿名」Token
        if (authentication == null || !authentication.isAuthenticated() 
            || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("未登入或 Token 無效");
        }
        
        String username = authentication.getName(); // (這在我們設計中是 Email)
        
        return userDAO.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Token 有效，但使用者不存在: " + username));
    }

    /**
     * (新) 取得「當前已登入」的「賣家」
     * (這個方法會呼叫上面的方法，並多一層角色檢查)
     *
     * @return UserPO (保證是 SELLER)
     * @throws AccessDeniedException 如果未登入或角色不是 SELLER
     */
    public UserPO getCurrentAuthenticatedSeller() throws AccessDeniedException {
        UserPO user = getCurrentAuthenticatedUser();
        
        if (user.getRole() != UserRole.SELLER) {
            throw new AccessDeniedException("只有賣家 (SELLER) 才能執行此操作");
        }
        return user;
    }
    public UserPO getCurrentAuthenticatedBuyer() throws AccessDeniedException {
        UserPO user = getCurrentAuthenticatedUser();
        
        if (user.getRole() != UserRole.BUYER) {
            throw new AccessDeniedException("只有買家 (BUYER) 才能執行此操作");
        }
        return user;
    }
}
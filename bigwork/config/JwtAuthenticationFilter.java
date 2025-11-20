package demo.bigwork.config;

import demo.bigwork.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * (關鍵) @Component
 * 告訴 Spring 這是一個「元件」，Spring 會自動建立它
 *
 * OncePerRequestFilter
 * 確保此 Filter 在「每一個」請求中「只執行一次」
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // (這會自動注入 UserDetailsServiceImpl)

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 這是 Filter 的核心邏輯
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 取得 "Authorization" 標頭 (Header)
        final String authHeader = request.getHeader("Authorization");

        // 2. (檢查) 如果標頭不存在，或不是以 "Bearer " 開頭，
        //    代表這是一個「公開請求」(e.g., 登入)，我們直接放行
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // (放行)
            return;
        }

        // 3. (關鍵) 取得 Token 字串 (去掉 "Bearer ")
        final String token = authHeader.substring(7); // "Bearer " 總共 7 個字
        final String userEmail;

        try {
            // 4. (關鍵) 從 Token 中解析出 "Email"
            userEmail = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            logger.warn("JWT 解析失敗: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("Invalid JWT Token");
            return; // (攔截)
        }

        // 5. (關鍵) 檢查使用者是否「尚未被驗證」
        // (SecurityContextHolder.getContext().getAuthentication() == null)
        // 這是為了避免重複驗證
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. (關鍵) 呼叫 UserDetailsServiceImpl 
            // 從資料庫載入使用者資料
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 7. (關鍵) 驗證 Token 是否有效
            if (jwtUtil.isTokenValid(token, userDetails)) {
                
                // 8. (成功) 建立一個「已驗證」的 Authentication 物件
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // (我們不需要密碼)
                        userDetails.getAuthorities() // (關鍵：塞入權限)
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. (成功) 將這個「已驗證物件」存入 SecurityContext
                // 這代表 Spring Security 認為「此請求已登入」
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 10. (放行) 讓請求繼續前往 Controller
        filterChain.doFilter(request, response);
    }
}
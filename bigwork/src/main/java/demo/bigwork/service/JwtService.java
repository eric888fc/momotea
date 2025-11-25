package demo.bigwork.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import demo.bigwork.model.po.UserPO;
import demo.bigwork.util.JwtUtil;

/**
 * JwtService：對外統一提供 JWT 產生 / 驗證 的服務
 */
@Service
public class JwtService {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /** 從 UserPO 產生 Token（登入成功後用這個） */
    public String generateToken(UserPO user) {
        return jwtUtil.generateToken(user);
    }

    /** 如果有需要，也可以從 UserDetails 產生 Token */
    public String generateToken(UserDetails userDetails) {
        return jwtUtil.generateToken(userDetails);
    }

    /** 從 Token 取出使用者名稱（這裡是 email） */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    /** 驗證 Token 是否有效 */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return jwtUtil.isTokenValid(token, userDetails);
    }
}
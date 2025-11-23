package demo.bigwork.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails; // Spring Security 的使用者
import org.springframework.stereotype.Component;

import demo.bigwork.model.po.UserPO;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * (關鍵) @Component
 * 這是一個「元件」，但它更像 Util。
 * 我們讓 Spring 管理它，這樣它才能讀取 application.properties 中的設定
 */
@Component
public class JwtUtil {

    // (關鍵) 1. 從 application.properties 讀取「密鑰」
    // 這個密鑰是加密 JWT 用的，絕對不能外洩
    @Value("${jwt.secret}")
    private String jwtSecret;

    // (關鍵) 2. 從 application.properties 讀取「過期時間」(毫秒)
    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    /**
     * 從 Token 中解析出「使用者名稱 (Email)」
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 驗證 Token 是否有效
     * (檢查 使用者名稱是否相符 & 是否過期)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
 // --- (以下是我們的新方法) ---

    /**
     * (最重要) 重載：從 UserPO 物件生成 Token
     * (我們將在 AuthController 的 login 方法中呼叫它)
     *
     * @param userPO 登入成功的使用者物件
     * @return 一長串的 JWT 字串
     */
    public String generateToken(UserPO userPO) {
        // (關鍵) 
        // 我們可以在 Token 中塞入額外資訊 (稱為 Claims)
        // 這裡我們塞入 "role"，這樣未來 Security 驗證時
        // 就可以直接從 Token 知道他是 BUYER 還是 SELLER
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userPO.getRole().name()); // e.g., "BUYER"
        claims.put("userId", userPO.getUserId()); // (選填，但很方便)

        // "Subject" (主題) 通常是使用者的唯一識別，我們用 Email
        String subject = userPO.getEmail();
        
        return createToken(claims, subject);
    }

    /**
     * (最重要) 生成 Token
     * (我們將在 UserServiceImpl 的 login 方法中呼叫它)
     */
    public String generateToken(UserDetails userDetails) {
        // (教授提醒) 我們可以在 Token 中塞入額外資訊 (e.g., 角色)
        // 這會讓未來「授權 (Authorization)」更方便
        Map<String, Object> claims = new HashMap<>();
        // (例如: claims.put("role", ...))
        
        return createToken(claims, userDetails.getUsername());
    }

    // --- 以下為輔助方法 ---

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject) // (關鍵) Subject 通常是使用者的唯一識別 (e.g., Email)
                .issuedAt(new Date(System.currentTimeMillis())) // 發行時間
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // (關鍵) 過期時間
                .signWith(getSigningKey(), Jwts.SIG.HS256) // (關鍵) 簽名演算法
                .compact();
    }

    // 取得簽名用的 SecretKey
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
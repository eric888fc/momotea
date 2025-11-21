package demo.bigwork.config;

import demo.bigwork.service.Impl.UserDetailsServiceImpl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider; // (匯入 2)
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // (匯入 3)
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // (匯入 4)
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // (匯入 5)
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // (匯入 6)
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * (關鍵) 這是 SecurityConfig 的「大改版」
 *
 * @EnableWebSecurity 啟用 Web 安全性
 * @EnableMethodSecurity (教授建議) 啟用「方法級別」的安全註解。 這讓我們未來可以直接在 Controller
 *                       的方法上寫 @PreAuthorize("hasRole('SELLER')") 來保護 API，這比在
 *                       http.authorizeHttpRequests 中管理所有 URL 更乾淨。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter; // (新) 我們的 JWT 過濾器
	private final UserDetailsService userDetailsService; // (新) 我們的 UserDetailsServiceImpl

	// (關鍵) 透過建構子注入
	@Autowired
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsServiceImpl userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * (保留) 這個 Bean (密碼加密器) 仍然是必需的
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration configuration = new CorsConfiguration();
	    // (關鍵) 
	    // 我們在允許的列表中，
	    // 加入 "http://localhost:5500" (Live Server 的預設位址)
	    configuration.setAllowedOrigins(Arrays.asList(
	        "http://localhost:5173", // (Vite，先留著)
	        "http://localhost:3000", // (create-react-app，先留著)
	        "http://127.0.0.1:5500", // (Live Server)
	        "http://localhost:5500"  // (Live Server)
	    ));
	    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
	    configuration.setAllowCredentials(true);
	    
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    return source;
	}

	/**
	 * (新) 認證提供者 (Authentication Provider) 職責：告訴 Spring Security 如何去「取得使用者資料」
	 * (UserDetailsService) 以及「如何檢查密碼」(PasswordEncoder)
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		// (關鍵) 告訴 Provider 去使用我們的 UserDetailsServiceImpl
		authProvider.setUserDetailsService(userDetailsService);
		// (關鍵) 告訴 Provider 去使用我們的 BCrypt 加密器
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

	/**
	 * (關鍵) 這就是「保全系統」的規則鏈 (Filter Chain)
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// (關鍵 - 第 2 步)
				// 在「所有規則之前」套用我們的 CORS 設定
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// 1. (不變) 關閉 CSRF (因為我們是 REST API)
				.csrf(csrf -> csrf.disable())

				// 2. (關鍵) 設定「授權 (Authorization)」規則
				.authorizeHttpRequests(authz -> authz
						// (重要) 放行所有 /api/auth/** 的請求 (註冊, 登入, 忘記密碼...)
						.requestMatchers("/api/auth/**").permitAll()
						// 告訴 Spring Security，「所有」 /uploads/ 路徑下的請求 (圖片)
						// 都是「公開的」(permitAll)，不需要登入
						.requestMatchers("/uploads/**").permitAll()
						// 允許「任何人」訪問所有 /api/public/** 的請求
						// (e.g., /api/public/products)
						.requestMatchers("/api/public/**").permitAll().requestMatchers("/api/cart/**").hasRole("BUYER")

						// 所有 /api/orders/** 路徑下的請求
						// (例如 /api/orders/checkout, /api/orders/me)
						// 都「必須」具備 "BUYER" 角色
						.requestMatchers("/api/orders/**").hasRole("BUYER")
						// 「建立」評價，必須是「買家」
						.requestMatchers(HttpMethod.POST, "/api/ratings").hasRole("BUYER")

						// 「查詢」評價，是「公開」的
						// (我們把它放在 /api/public/.. 路徑下，這條規則其實已包含在上面)
						// 為了更明確，我們可以單獨為商品評價設定
						.requestMatchers(HttpMethod.GET, "/api/public/products/*/ratings").permitAll()
						// 「查詢我的評價」，必須是「買家」
						.requestMatchers(HttpMethod.GET, "/api/ratings/me").hasRole("BUYER")
						// 「更新」評價 (e.g., /api/ratings/1)，必須是「買家」
						// (使用 * 萬用字元來匹配 ID)
						.requestMatchers(HttpMethod.PUT, "/api/ratings/*").hasRole("BUYER")
						// 「刪除」評價 (e.g., /api/ratings/1)，必須是「買家」
						.requestMatchers(HttpMethod.DELETE, "/api/ratings/*").hasRole("BUYER")

						// (規則 1)
						// 賣家 API：所有 /api/products/** 的請求
						// 都「必須」具備 "SELLER" 角色
						.requestMatchers("/api/products/**").hasRole("SELLER")
						// 「查詢賣家收到的評價」，必須是「賣家」
						.requestMatchers(HttpMethod.GET, "/api/seller/ratings/me").hasRole("SELLER")
						// 「查詢賣家收到的訂單」，必須是「賣家」
						.requestMatchers(HttpMethod.GET, "/api/seller/orders/**").hasRole("SELLER")
						// 錢包 API
						// 只要「已登入」 (BUYER 或 SELLER) 就可以訪問
						.requestMatchers("/api/wallet/**").authenticated()

						// 所有 /api/seller/** 路徑下的請求
						// (例如 /api/seller/account)
						// 都「必須」具備 "SELLER" 角色
						.requestMatchers("/api/seller/**").hasRole("SELLER")
						// (新規則)
						// 所有 /api/profile/** 路徑下的請求
						// (例如 /api/profile/me)
						// 只要「已登入」 (BUYER 或 SELLER) 就可以訪問
						.requestMatchers("/api/profile/**").authenticated()

						// (規則 2)
						// 買家 API (未來)：
						// .requestMatchers("/api/cart/**").hasRole("BUYER")

						// (重要) 除了上面放行的，其他「任何請求 (anyRequest)」都必須「已驗證 (authenticated)」
						.anyRequest().authenticated())

				// 3. (關鍵) 設定 Session 管理策略 =「無狀態 (STATELESS)」
				// 這是 REST API 的核心：我們不再使用 Cookie/Session
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 4. (關鍵) 告訴 Spring Security 使用我們定義的 Provider
				.authenticationProvider(authenticationProvider())

				// 5. (關鍵)
				// 告訴 Spring Security：
				// 在「執行」標準的 UsernamePasswordAuthenticationFilter 之前，
				// 「先執行」我們的 jwtAuthFilter
				// 這就是「攔截 Token」的步驟
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}	
	  
}
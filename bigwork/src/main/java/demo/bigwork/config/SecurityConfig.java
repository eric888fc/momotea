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
				"http://localhost:5500" // (Live Server)
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        // 認證相關 (登入 / 註冊 / 發驗證碼...) -> 全開
                        .requestMatchers("/api/auth/**").permitAll()

                        // 靜態檔案
                        .requestMatchers("/uploads/**").permitAll()

                        // 公開 API
                        .requestMatchers("/api/public/**").permitAll()
                        
                        // ===== 管理員 API：所有 /api/admin/** 都要 ADMIN =====
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ===== 買家 API =====
                        .requestMatchers("/api/cart/**").hasRole("BUYER")
                        .requestMatchers("/api/orders/**").hasRole("BUYER")
                        .requestMatchers(HttpMethod.POST, "/api/ratings").hasRole("BUYER")
                        .requestMatchers(HttpMethod.GET, "/api/ratings/me").hasRole("BUYER")
                        .requestMatchers(HttpMethod.PUT, "/api/ratings/*").hasRole("BUYER")
                        .requestMatchers(HttpMethod.DELETE, "/api/ratings/*").hasRole("BUYER")
                        // 開放給綠界伺服器呼叫 (無 Token)
                        .requestMatchers("/notify").permitAll()
                        
                        // 建立訂單 API (必須登入，才能抓取購物車)
                        .requestMatchers("/createOrder").authenticated()

                        // ===== 賣家 API =====
                        .requestMatchers("/api/products/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/seller/ratings/me").hasRole("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/seller/orders/**").hasRole("SELLER")
                        .requestMatchers("/api/seller/**").hasRole("SELLER")

                        // ===== 共用 (只要登入就能用) =====
                        .requestMatchers("/api/wallet/**").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()

                        // 其他沒列到的，只要登入即可
                        .anyRequest().authenticated()
                );

        return http.build();
	}

}
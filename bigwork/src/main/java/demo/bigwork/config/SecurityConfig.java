package demo.bigwork.config;

import demo.bigwork.service.Impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // ===== 密碼編碼器 =====
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===== CORS 設定 =====
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ===== AuthenticationProvider / AuthenticationManager =====
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ===== SecurityFilterChain =====
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


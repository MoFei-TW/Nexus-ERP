package com.mofei.erp.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mofei.erp.common.R;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_EMPLOYEE");
        return hierarchy;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 靜態資源與公版頁面視圖允許所有人檢視 (Plan G: 未登入狀態可檢視前端UI與按鈕效果)
                .requestMatchers(
                    "/", "/login", "/logout", "/dashboard",
                    "/employee", "/product", "/customer", "/order",
                    "/static/**", "/css/**", "/js/**", "/images/**", "/logo/**", "/favicon.ico"
                ).permitAll()
                // 儀表板唯讀數據允許訪客預覽
                .requestMatchers(HttpMethod.GET, "/api/dashboard/**").permitAll()
                // 認證 API
                .requestMatchers("/api/auth/**").permitAll()
                // 後端 CRUD API 嚴格要求登入驗證
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                // 當未登入訪問 API 時回傳 401 JSON，而非重導向
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(
                            R.fail(401, "未登入或登入憑證已過期，請先登入")
                        ));
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(
                            R.fail(403, "權限不足，禁止存取")
                        ));
                    } else {
                        response.sendRedirect("/dashboard");
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(R.ok("登出成功", null)));
                })
                .deleteCookies("JSESSIONID", "MOFEI_SESSION")
                .invalidateHttpSession(true)
            );

        return http.build();
    }
}

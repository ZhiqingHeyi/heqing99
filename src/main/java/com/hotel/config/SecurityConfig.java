package com.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig 类是 Spring Security 的核心配置类。
 * 它负责配置应用程序的安全性，包括身份验证、授权、跨域支持等。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 自动注入 JwtAuthenticationFilter，用于处理 JWT 认证。
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置 AuthenticationManager，用于处理身份验证。
     * @param authConfig AuthenticationConfiguration 对象
     * @return AuthenticationManager 实例
     * @throws Exception 如果配置失败
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * 配置 PasswordEncoder，用于密码加密和验证。
     * 这里使用 BCryptPasswordEncoder，它是一种强哈希算法，适合存储密码。
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 配置 HttpFirewall，允许 URL 中包含编码的斜杠。
     * 这对于某些 REST API 路径可能有用。
     * @return HttpFirewall 实例
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
    
    /**
     * 配置 SecurityFilterChain，这是 Spring Security 的核心过滤器链。
     * 它定义了请求的安全规则，包括哪些路径需要认证、哪些路径允许匿名访问等。
     * @param http HttpSecurity 对象
     * @return SecurityFilterChain 实例
     * @throws Exception 如果配置失败
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("配置Security (JWT): 设置权限和请求规则");
        http
            // 启用跨域支持
            .cors().and()
            // 禁用 CSRF 保护，因为我们在使用 JWT
            .csrf().disable()
            // 设置会话管理策略为无状态，因为我们使用 JWT
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求授权规则
            .authorizeRequests()
                // 静态资源和前端页面允许匿名访问
                .antMatchers("/*.html", "/*.js", "/*.css", "/*.ico", "/assets/**").permitAll()
                // 前端 SPA 路由允许匿名访问
                .antMatchers("/", "/about", "/rooms", "/room/**", "/contact", "/booking").permitAll()
                // 用户注册和登录相关路径允许匿名访问
                .antMatchers("/register", "/login", "/user/**").permitAll()
                // 后台管理相关路由允许匿名访问
                .antMatchers("/admin", "/admin/login", "/admin/dashboard/**",
                    "/admin/users/**", "/admin/staff/**", 
                    "/admin/reception/**", "/admin/cleaning/**").permitAll()
                // 后端 API 登录接口允许匿名访问
                .antMatchers(HttpMethod.POST, "/admin/login").permitAll()
                .antMatchers(HttpMethod.GET, "/admin/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // 刷新 Token 接口允许匿名访问
                .antMatchers(HttpMethod.POST, "/refresh-token").permitAll()
                // 用户相关 API 注册和登录接口允许匿名访问
                .antMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                // 检查用户名和手机号是否可用的接口允许匿名访问
                .antMatchers(HttpMethod.GET, "/api/users/check-username").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/check-phone").permitAll()
                // 新增：允许匿名访问邀请码验证接口
                .antMatchers(HttpMethod.GET, "/api/invitation-codes/validate/**").permitAll()
                // 新增：允许匿名访问员工注册接口
                .antMatchers(HttpMethod.POST, "/api/users/register/staff").permitAll()
                // 新增：允许匿名访问获取房型列表接口
                .antMatchers(HttpMethod.GET, "/api/rooms/types").permitAll()
                // 用户个人信息接口需要认证
                .antMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                // 修改用户信息接口需要认证
                .antMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                // 修改用户密码接口需要认证
                .antMatchers(HttpMethod.PUT, "/api/users/{id}/password").authenticated()
                // ADD: Allow ADMIN or RECEPTIONIST to access specific stats endpoints
                .antMatchers(HttpMethod.GET, "/api/admin/dashboard/stats", "/api/admin/checkin/today-stats").hasAnyRole("ADMIN", "RECEPTIONIST")
                // ADD: Allow ADMIN or RECEPTIONIST to access rooms list for status view
                .antMatchers(HttpMethod.GET, "/api/admin/rooms").hasAnyRole("ADMIN", "RECEPTIONIST")
                // ADD: Explicit rules for admin access to non-admin prefixed APIs
                .antMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/reservations").hasAnyRole("ADMIN", "RECEPTIONIST")
                // ADD: Allow ADMIN or RECEPTIONIST to access rooms list for status view
                .antMatchers(HttpMethod.GET, "/api/admin/rooms").hasAnyRole("ADMIN", "RECEPTIONIST")
                // ADD: Explicit rules for admin access to non-admin prefixed APIs
                .antMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/reservations").hasAnyRole("ADMIN", "RECEPTIONIST")
                // ADD: Allow ADMIN or RECEPTIONIST to access rooms list for status view
                .antMatchers(HttpMethod.GET, "/api/admin/rooms").hasAnyRole("ADMIN", "RECEPTIONIST")
                // ADD: Explicit rules for admin access to non-admin prefixed APIs
                .antMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/reservations").hasAnyRole("ADMIN", "RECEPTIONIST")
                
                // --- 新增规则：允许 RECEPTIONIST 办理入住 --- 
                .antMatchers(HttpMethod.POST, "/api/admin/checkin").hasAnyRole("ADMIN", "RECEPTIONIST")
                // --- 新增结束 --- 
                
                // --- 新增规则：允许 RECEPTIONIST 访问退房管理 (修正角色名) ---
                .antMatchers(HttpMethod.GET, "/api/admin/checkout/stats", "/api/admin/checkout/records").hasAnyRole("ADMIN", "RECEPTIONIST")
                .antMatchers(HttpMethod.GET, "/api/admin/rooms/occupied").hasAnyRole("ADMIN", "RECEPTIONIST")
                .antMatchers(HttpMethod.POST, "/api/admin/checkout/confirm/**", "/api/admin/checkout/create").hasAnyRole("ADMIN", "RECEPTIONIST")
                .antMatchers(HttpMethod.POST, "/api/admin/checkout/card-checkout").hasAnyRole("ADMIN", "RECEPTIONIST")
                .antMatchers(HttpMethod.GET, "/api/admin/checkout/{id}/bill").hasAnyRole("ADMIN", "RECEPTIONIST")
                .antMatchers(HttpMethod.GET, "/api/admin/rooms/{roomNumber}/current-guest").hasAnyRole("ADMIN", "RECEPTIONIST")
                // 添加对 random-occupied 的授权
                .antMatchers(HttpMethod.GET, "/api/admin/rooms/random-occupied").hasAnyRole("ADMIN", "RECEPTIONIST")
                // --- 新增结束 ---

                // 管理员相关 API 需要管理员角色 (Keep this general rule after specific ones)
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")
                // 添加 OPTIONS 请求支持，用于 CORS 预检
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            .and()
            // 在 UsernamePasswordAuthenticationFilter 之前添加 JwtAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("Security配置 (JWT) 完成");
        
        return http.build();
    }

    /**
     * 配置 CORS（跨域资源共享），允许前端应用访问后端 API。
     * @return CorsConfigurationSource 实例
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("配置CORS: 允许跨域请求");
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的源，这里是本地开发环境的前端地址
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "Origin", "Accept", "Authorization"));
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        // 允许携带凭证（如 Cookie）
        configuration.setAllowCredentials(true);
        // 设置预检请求的有效期（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 将 CORS 配置应用到所有路径
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("CORS配置完成");
        return source;
    }
}
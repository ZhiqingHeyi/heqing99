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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("配置Security (JWT): 设置权限和请求规则");
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 静态资源和前端页面
                .antMatchers("/*.html", "/*.js", "/*.css", "/*.ico", "/assets/**").permitAll()
                // 前端SPA路由
                .antMatchers("/", "/about", "/rooms", "/room/**", "/contact", "/booking").permitAll()
                .antMatchers("/register", "/login", "/user/**").permitAll()
                // 后台管理相关路由，用于SPA前端路由
                .antMatchers("/admin", "/admin/login", "/admin/dashboard/**",
                    "/admin/users/**", "/admin/staff/**", 
                    "/admin/reception/**", "/admin/cleaning/**").permitAll()
                // 后端API
                .antMatchers(HttpMethod.POST, "/admin/login").permitAll()
                .antMatchers(HttpMethod.GET, "/admin/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // 刷新Token接口
                .antMatchers(HttpMethod.POST, "/refresh-token").permitAll()
                // 用户相关API
                .antMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/check-username").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/check-phone").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/users/{id}/password").authenticated()
                // 需要管理员权限的 API
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")
                // 添加OPTIONS请求支持，用于CORS预检
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("Security配置 (JWT) 完成");
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("配置CORS: 允许跨域请求");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "Origin", "Accept", "Authorization"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("CORS配置完成");
        return source;
    }
}
package com.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
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
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("配置Security: 设置权限和请求规则");
        http
            .cors().and()
            .csrf().disable()
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
                // 用户相关API
                .antMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/check-username").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                .antMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                // 添加OPTIONS请求支持，用于CORS预检
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login/process")
                .defaultSuccessUrl("/admin/dashboard")
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login")
                .permitAll();
                
        System.out.println("Security配置完成");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("配置CORS: 允许跨域请求");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "Origin", "Accept"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("CORS配置完成");
        return source;
    }
}
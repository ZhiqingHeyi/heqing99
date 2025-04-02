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
        http
            .csrf().disable()
            .authorizeRequests()
                // 静态资源和前端页面
                .antMatchers("/*.html", "/*.js", "/*.css", "/*.ico", "/assets/**").permitAll()
                // 前端SPA路由
                .antMatchers("/", "/about", "/rooms", "/room/**", "/contact", "/booking").permitAll()
                // 后台管理相关路由，用于SPA前端路由
                .antMatchers("/admin", "/admin/login", "/admin/dashboard/**",
                    "/admin/users/**", "/admin/staff/**", 
                    "/admin/reception/**", "/admin/cleaning/**").permitAll()
                // 后端API
                .antMatchers(HttpMethod.POST, "/admin/login").permitAll()
                .antMatchers(HttpMethod.GET, "/admin/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
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
    }
}
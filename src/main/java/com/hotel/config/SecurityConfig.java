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
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                // 静态资源和前端页面
                .antMatchers("/", "/index.html", "/favicon.ico", "/assets/**", "/static/**", "/dist/**").permitAll()
                // 前端路由
                .antMatchers("/about", "/rooms", "/room/**", "/contact", "/booking").permitAll()
                // 后端API
                .antMatchers(HttpMethod.POST, "/admin/login").permitAll()
                .antMatchers(HttpMethod.GET, "/admin/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
                .antMatchers("/admin/dashboard/**").hasAnyRole("ADMIN", "admin")
                .antMatchers("/admin/reception/**").hasAnyRole("RECEPTIONIST", "receptionist")
                .antMatchers("/admin/cleaning/**").hasAnyRole("CLEANER", "cleaner")
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
package com.hotel.config;

import com.hotel.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JwtAuthenticationFilter 是一个 Spring Security 的过滤器，
 * 用于在每个请求到达时验证 JWT（JSON Web Token）的有效性。
 * 如果 JWT 有效，则将用户信息加载到 Spring Security 的上下文中。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider; // 提供 JWT 相关操作的工具类

    @Autowired
    private UserDetailsService userDetailsService; // 用于加载用户详细信息的服务

    /**
     * 每个请求都会经过这个方法进行处理。
     * 主要功能包括：
     * 1. 从请求头中提取 JWT。
     * 2. 验证 JWT 是否有效。
     * 3. 如果有效，加载用户信息并设置到 Spring Security 的上下文中。
     * 4. 如果无效，记录日志但不会阻止请求继续执行。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中提取 JWT
            String jwt = getJwtFromRequest(request);

            // 检查 JWT 是否存在且有效
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 从 JWT 中提取用户名
                String username = tokenProvider.getUsernameFromToken(jwt);

                // 加载用户详细信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息设置到 Spring Security 的上下文中
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("用户 '{}' 认证成功, 设置 SecurityContext", username);
            } else {
                // 如果 JWT 无效或不存在，记录日志
                if (StringUtils.hasText(jwt)) {
                    logger.warn("JWT Token 无效: {}", jwt);
                } else {
                    // 对于不需要 Token 的路径（如登录、注册），这里不打印日志
                }
            }
        } catch (Exception ex) {
            // 如果发生异常，记录错误日志
            logger.error("无法在安全上下文中设置用户认证", ex);
        }

        // 继续执行过滤链中的下一个过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT。
     * JWT 通常以 "Bearer <token>" 的形式出现在 Authorization 头中。
     * 
     * @param request HTTP 请求对象
     * @return 提取的 JWT 字符串，如果不存在则返回 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 去掉 "Bearer " 前缀
        }
        return null;
    }
}
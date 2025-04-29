package com.hotel.util;

import com.hotel.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    
    private static TokenBlacklistService tokenBlacklistService;

    private static String secret = "heqinghotel-secret-key";
    private static long expiration = 3600000; // 默认1小时
    private static long refreshExpiration = 604800000; // 默认7天
    
    @Autowired
    public void setTokenBlacklistService(TokenBlacklistService tokenBlacklistService) {
        JwtUtil.tokenBlacklistService = tokenBlacklistService;
    }

    @Value("${jwt.secret:heqinghotel-secret-key}")
    public void setSecret(String secret) {
        JwtUtil.secret = secret;
    }

    @Value("${jwt.expiration:3600000}")
    public void setExpiration(long expiration) {
        JwtUtil.expiration = expiration;
    }
    
    @Value("${jwt.refresh-expiration:604800000}")
    public void setRefreshExpiration(long refreshExpiration) {
        JwtUtil.refreshExpiration = refreshExpiration;
    }

    /**
     * 生成访问令牌
     * 
     * @param username 用户名
     * @param role 用户角色
     * @return 生成的JWT令牌
     */
    public static String generateToken(String username, String role) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            return Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace();
            // 返回一个特殊token，表示生成失败
            return "token-error-" + System.currentTimeMillis();
        }
    }
    
    /**
     * 生成刷新令牌
     * 
     * @param username 用户名
     * @param role 用户角色
     * @return 生成的刷新令牌
     */
    public static String generateRefreshToken(String username, String role) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshExpiration);
            
            // 生成唯一标识符
            String tokenId = UUID.randomUUID().toString();

            return Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .claim("type", "refresh")
                    .setId(tokenId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace();
            return "refresh-token-error-" + System.currentTimeMillis();
        }
    }

    /**
     * 从令牌中获取声明
     * 
     * @param token JWT令牌
     * @return Claims对象
     */
    public static Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户角色
     * 
     * @param token JWT令牌
     * @return 用户角色
     */
    public static String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 获取令牌过期日期
     * 
     * @param token JWT令牌
     * @return 过期日期
     */
    public static Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 检查令牌是否过期
     * 
     * @param token JWT令牌
     * @return 如果令牌已过期返回true，否则返回false
     */
    public static boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证访问令牌
     * 
     * @param token JWT令牌
     * @return 如果令牌有效返回true，否则返回false
     */
    public static boolean validateToken(String token) {
        try {
            // 检查token是否在黑名单中
            if (tokenBlacklistService != null && tokenBlacklistService.isBlacklisted(token)) {
                return false;
            }
            
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证刷新令牌
     * 
     * @param token 刷新令牌
     * @return 如果刷新令牌有效返回true，否则返回false
     */
    public static boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            
            // 检查令牌类型
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                return false;
            }
            
            // 检查是否过期
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取访问令牌过期时间（秒）
     * 
     * @return 过期时间（秒）
     */
    public static long getExpirationDuration() {
        return expiration / 1000;
    }
    
    /**
     * 获取刷新令牌过期时间（秒）
     * 
     * @return 过期时间（秒）
     */
    public static long getRefreshExpirationDuration() {
        return refreshExpiration / 1000;
    }
}
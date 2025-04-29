package com.hotel.service;

/**
 * Token黑名单服务接口
 * 用于管理已失效的Token
 */
public interface TokenBlacklistService {

    /**
     * 将token添加到黑名单
     * 
     * @param token 需要加入黑名单的token
     * @param expirySeconds token的剩余有效期（秒）
     */
    void addToBlacklist(String token, long expirySeconds);

    /**
     * 检查token是否在黑名单中
     * 
     * @param token 需要检查的token
     * @return 如果token在黑名单中返回true，否则返回false
     */
    boolean isBlacklisted(String token);
} 
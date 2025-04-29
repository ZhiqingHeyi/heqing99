package com.hotel.service.impl;

import com.hotel.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务的内存实现
 * 使用ConcurrentHashMap存储黑名单中的token
 */
@Service
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 将token添加到黑名单
     *
     * @param token 需要加入黑名单的token
     * @param expirySeconds token的剩余有效期（秒）
     */
    @Override
    public void addToBlacklist(String token, long expirySeconds) {
        // 计算过期时间戳
        long expiryTimestamp = System.currentTimeMillis() + (expirySeconds * 1000);
        blacklist.put(token, expiryTimestamp);

        // 安排自动清理任务
        scheduler.schedule(() -> blacklist.remove(token), expirySeconds, TimeUnit.SECONDS);
    }

    /**
     * 检查token是否在黑名单中
     *
     * @param token 需要检查的token
     * @return 如果token在黑名单中返回true，否则返回false
     */
    @Override
    public boolean isBlacklisted(String token) {
        Long expiryTimestamp = blacklist.get(token);
        
        if (expiryTimestamp == null) {
            return false;
        }
        
        // 检查是否已过期
        if (System.currentTimeMillis() > expiryTimestamp) {
            blacklist.remove(token); // 清理过期条目
            return false;
        }
        
        return true;
    }
    
    /**
     * 清理过期的token
     * 此方法可以定期调用以清理过期的token
     */
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
} 
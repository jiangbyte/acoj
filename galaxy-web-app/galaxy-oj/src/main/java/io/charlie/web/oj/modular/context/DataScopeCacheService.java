package io.charlie.web.oj.modular.context;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeCacheService {
    
    private final CacheManager cacheManager;
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // 用于存储缓存时间戳，便于手动清理
    private final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    /**
     * 初始化定时清理任务
     */
//    @PostConstruct
//    public void init() {
//        // 每30分钟执行一次清理任务
//        scheduler.scheduleAtFixedRate(this::cleanExpiredCache, 10, 10, TimeUnit.MINUTES);
//        log.info("数据权限缓存自动清理任务已启动，每30分钟执行一次");
//    }

    /**
     * 销毁时关闭定时任务
     */
//    @PreDestroy
//    public void destroy() {
//        if (!scheduler.isShutdown()) {
//            scheduler.shutdown();
//            try {
//                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
//                    scheduler.shutdownNow();
//                }
//            } catch (InterruptedException e) {
//                scheduler.shutdownNow();
//                Thread.currentThread().interrupt();
//            }
//            log.info("数据权限缓存自动清理任务已停止");
//        }
//    }
    
    public void put(String userId, DataScopeContext context) {
        String cacheKey = getCacheKey(userId);
        Cache cache = cacheManager.getCache(CacheConfig.DATA_SCOPE_CACHE);
        if (cache != null) {
            cache.put(cacheKey, context);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            log.info("数据权限上下文已缓存: userId={}", userId);
        }
    }
    
    public DataScopeContext get(String userId) {
        String cacheKey = getCacheKey(userId);
        Cache cache = cacheManager.getCache(CacheConfig.DATA_SCOPE_CACHE);
        if (cache != null) {
            DataScopeContext context = cache.get(cacheKey, DataScopeContext.class);
            if (context != null) {
                log.info("从缓存获取数据权限上下文: userId={}", userId);
            }
            return context;
        }
        return null;
    }
    
    public void evict(String userId) {
        String cacheKey = getCacheKey(userId);
        Cache cache = cacheManager.getCache(CacheConfig.DATA_SCOPE_CACHE);
        if (cache != null) {
            cache.evict(cacheKey);
            cacheTimestamps.remove(cacheKey);
            log.info("数据权限上下文缓存已清除: userId={}", userId);
        }
    }
    
    public void evictAll() {
        Cache cache = cacheManager.getCache(CacheConfig.DATA_SCOPE_CACHE);
        if (cache != null) {
            cache.clear();
            cacheTimestamps.clear();
            log.info("所有数据权限上下文缓存已清除");
        }
    }
    
    private String getCacheKey(String userId) {
        return "dataScope:" + userId;
    }
    
//    /**
//     * 清理过期缓存
//     */
//    public void cleanExpiredCache() {
//        long currentTime = System.currentTimeMillis();
//        long expireTime = TimeUnit.MINUTES.toMillis(CacheConfig.DEFAULT_CACHE_TIMEOUT);
//
//        cacheTimestamps.entrySet().removeIf(entry -> {
//            if (currentTime - entry.getValue() > expireTime) {
//                String cacheKey = entry.getKey();
//                Cache cache = cacheManager.getCache(CacheConfig.DATA_SCOPE_CACHE);
//                if (cache != null) {
//                    cache.evict(cacheKey);
//                }
//                log.info("清理过期缓存: {}", cacheKey);
//                return true;
//            }
//            return false;
//        });
//    }

    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        try {
            long currentTime = System.currentTimeMillis();
            long expireTime = TimeUnit.MINUTES.toMillis(CacheConfig.DEFAULT_CACHE_TIMEOUT);

            int expiredCount = 0;
            int totalCount = cacheTimestamps.size();

            for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
                if (currentTime - entry.getValue() > expireTime) {
                    String cacheKey = entry.getKey();
                    Cache cache = cacheManager.getCache(CacheConfig.DATA_SCOPE_CACHE);
                    if (cache != null) {
                        cache.evict(cacheKey);
                    }
                    cacheTimestamps.remove(cacheKey);
                    expiredCount++;
                    log.info("清理过期缓存: {}", cacheKey);
                }
            }

            if (expiredCount > 0) {
                log.info("自动清理过期缓存完成: 清理了 {}/{} 个缓存项", expiredCount, totalCount);
            } else {
                log.info("自动清理过期缓存完成: 无过期缓存项，当前缓存总数: {}", totalCount);
            }

        } catch (Exception e) {
            log.error("自动清理过期缓存时发生异常", e);
        }
    }


    /**
     * 手动触发立即清理（用于测试或特殊情况）
     */
    public void cleanExpiredCacheNow() {
        log.info("手动触发立即清理过期缓存");
        cleanExpiredCache();
    }

}
package io.charlie.web.oj.context;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    public static final String DATA_SCOPE_CACHE = "dataScopeCache";
    public static final long DEFAULT_CACHE_TIMEOUT = 30; // 30分钟
    
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(DATA_SCOPE_CACHE);
    }
}
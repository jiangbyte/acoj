package io.charlie.galaxy.cache;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class MybatisPlusRedisCache implements Cache {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final String id;
    private RedisTemplate<String, Object> redisTemplate;

    // 添加缓存时间配置（单位：分钟）
    private final long expireTime = 30; // 默认30分钟

    public MybatisPlusRedisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
//        log.warn("缓存ID {}", id);
//        System.out.println("缓存ID " + id);
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
//        log.warn("缓存保存 {}", key);
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        if (value != null) {
            redisTemplate.opsForHash().put(id, key.toString(), value);
            // 为整个缓存空间设置过期时间
            redisTemplate.expire(id, expireTime, TimeUnit.MINUTES);
        }
    }

    @Override
    public Object getObject(Object key) {
//        log.warn("缓存获取 {}", key);
//        System.out.println("缓存获取 " + key);
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        try {
            if (key != null) {
                return redisTemplate.opsForHash().get(id, key.toString());
            }
        } catch (Exception e) {
            log.error("缓存获取出错 {}", e.getMessage());
            // 发生异常时删除可能有问题的缓存
            redisTemplate.delete(key.toString());
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
//        log.warn("删除缓存 {}", key);
//        System.out.println("删除缓存 " + key);
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        if (key != null) {
            redisTemplate.delete(key.toString());
        }
        return null;
    }

    @Override
    public void clear() {
//        log.warn("清空缓存 {}", this.id);
//        System.out.println("清空缓存 " + this.id);
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        Set<Object> hashKeys = redisTemplate.opsForHash().keys(this.id);
        if (!CollectionUtils.isEmpty(hashKeys)) {
//            log.warn("删除Hash字段: {}", hashKeys);
//            System.out.println("删除Hash字段 " + hashKeys);
            Long deletedCount = redisTemplate.opsForHash().delete(this.id, hashKeys.toArray());
//            log.warn("删除字段数量: {}", deletedCount);
//            System.out.println("删除字段数量 " + deletedCount);
        }
    }

    @Override
    public int getSize() {
        log.warn("获取缓存大小");
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }
        Long size = redisTemplate.opsForHash().size(id);
        return size.intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }
}
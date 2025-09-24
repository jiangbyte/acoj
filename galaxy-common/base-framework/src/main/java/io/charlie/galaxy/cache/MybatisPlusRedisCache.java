package io.charlie.galaxy.cache;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.galaxy.pojo.CommonEntity;
import io.lettuce.core.dynamic.annotation.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.dromara.core.trans.vo.TransPojo;
import org.dromara.core.trans.vo.VO;
import org.dromara.trans.service.impl.TransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class MybatisPlusRedisCache implements Cache {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final String id;
    private RedisTemplate<String, Object> redisTemplate;

    public MybatisPlusRedisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        log.info("获取缓存ID {}", this.id);
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        log.info("缓存 {} {}", key, value);
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        Class<?> clazz = value.getClass();
        System.out.println("类名: " + clazz.getName());
        System.out.println("简单类名: " + clazz.getSimpleName());
        if (value instanceof ArrayList<?> arrayList) {
            System.out.println("列表元素类型: " + arrayList.getFirst().getClass().getName());
        }

        if (value != null) {
            redisTemplate.opsForValue().set(key.toString(), value, 1, TimeUnit.HOURS);
        }
    }

    @Override
    public Object getObject(Object key) {
        log.info("获取缓存 {}", key);
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        try {
            if (key != null) {
                return redisTemplate.opsForValue().get(key.toString());
            }
        } catch (Exception e) {
            log.error("缓存获取出错 {}", e.getMessage());
            redisTemplate.delete(key.toString());
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
        log.info("删除缓存 {}", key);
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
        log.info("清空缓存");
        if (redisTemplate == null) {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        }

        Set<String> keys = redisTemplate.keys("*:" + this.id + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public int getSize() {
        Set<String> keys = redisTemplate.keys("*:" + this.id + "*");
        return keys != null ? keys.size() : 0;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }
}
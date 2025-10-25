package io.charlie.galaxy.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description Redis配置
 */
@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 使用 GenericJackson2JsonRedisSerializer
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

//    @Bean
//    @Primary
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//
//        // 先完全初始化ObjectMapper
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
//
//        // 获取验证器后再激活类型信息
//        PolymorphicTypeValidator validator = objectMapper.getPolymorphicTypeValidator();
//        objectMapper.activateDefaultTyping(
//                validator,
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY);
//
//        // 使用新API创建序列化器
//        GenericJackson2JsonRedisSerializer serializer =
//                new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        // 设置序列化方式
//        template.setKeySerializer(RedisSerializer.string());
//        template.setValueSerializer(serializer);
//        template.setHashKeySerializer(RedisSerializer.string());
//        template.setHashValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//        // 创建相同的ObjectMapper配置
//        ObjectMapper cacheObjectMapper = new ObjectMapper();
//        cacheObjectMapper.registerModule(new JavaTimeModule());
//        cacheObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        PolymorphicTypeValidator validator = cacheObjectMapper.getPolymorphicTypeValidator();
//        cacheObjectMapper.activateDefaultTyping(
//                validator,
//                ObjectMapper.DefaultTyping.NON_FINAL);
//
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
//                        RedisSerializer.string()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
//                        new GenericJackson2JsonRedisSerializer(cacheObjectMapper)))
//                .entryTtl(Duration.ofHours(1));
//
//        return RedisCacheManager.builder(redisConnectionFactory)
//                .cacheDefaults(config)
//                .build();
//    }

//    @Bean
//    @Primary
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        // 使用自定义配置的 Jackson 序列化器
//        template.setValueSerializer(jackson2JsonRedisSerializer());
//        template.setHashValueSerializer(jackson2JsonRedisSerializer());
//
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    @Bean
//    public GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer() {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        // 允许序列化 transient 字段
//        objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, false);
//
//        // 忽略未知属性（反序列化时）
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        // 忽略空bean转json错误
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//
//        // 处理循环引用
//        objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
//
//        // 日期格式处理
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        objectMapper.registerModule(new JavaTimeModule());
//
//        // 包含 null 值，确保所有字段都被序列化
//        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
//        return new GenericJackson2JsonRedisSerializer(objectMapper);
//    }
}

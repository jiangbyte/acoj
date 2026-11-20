package io.charlie.cores.config.timestamp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class TimestampJacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 对齐 Spring Boot 默认：忽略请求体中未知字段（如 edit 提交的 createTime 等）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();
        // 注册序列化器和反序列化器
        module.addSerializer(Date.class, new DateToTimestampSerializer());
        module.addDeserializer(Date.class, new TimestampToDateDeserializer());

        objectMapper.registerModule(module);
        return objectMapper;
    }
}
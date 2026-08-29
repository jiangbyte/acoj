package io.charlie.web.modular.sys.file.config;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.charlie.web.modular.sys.file.service.StorageService;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 将库中对象名序列化为可访问 URL（公开路径 / 预签名）。
 */
public class StorageAccessUrlSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (!StringUtils.hasText(value)) {
            gen.writeNull();
            return;
        }
        try {
            StorageServiceFactory factory = SpringUtil.getBean(StorageServiceFactory.class);
            StorageService storageService = factory.getStorageService();
            gen.writeString(storageService.toAccessUrl(value));
        } catch (Exception e) {
            gen.writeString(value);
        }
    }
}

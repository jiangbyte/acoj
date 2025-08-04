package io.charlie.app.core.modular.file.config;

import io.charlie.app.core.modular.file.config.properties.FileStorageProperties;
import io.charlie.app.core.modular.file.service.FileStorageService;
import io.charlie.app.core.modular.file.service.impl.LocalFileStorageService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "app.file")
    public FileStorageProperties fileStorageProperties() {
        return new FileStorageProperties();
    }
    
    @Bean
    public FileStorageService fileStorageService(FileStorageProperties properties) {
        // 这里可以根据配置决定使用本地存储还是OSS
        // 可以使用策略模式或工厂模式来切换存储方式
        return new LocalFileStorageService(properties);
    }
}
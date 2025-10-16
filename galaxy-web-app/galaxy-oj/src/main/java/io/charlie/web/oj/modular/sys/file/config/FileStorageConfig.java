package io.charlie.web.oj.modular.sys.file.config;

import io.charlie.web.oj.modular.sys.file.config.properties.FileStorageProperties;
import io.charlie.web.oj.modular.sys.file.service.FileStorageService;
import io.charlie.web.oj.modular.sys.file.service.impl.LocalFileStorageService;
import io.charlie.web.oj.modular.sys.file.service.impl.MinIoFileStorageService;
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
//        if (properties.isUseMinIo()) {
            return new MinIoFileStorageService(properties);
//        } else {
//            return new LocalFileStorageService(properties);
//        }
    }
}
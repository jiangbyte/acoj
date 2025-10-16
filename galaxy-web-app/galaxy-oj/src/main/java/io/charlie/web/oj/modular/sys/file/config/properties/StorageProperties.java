package io.charlie.web.oj.modular.sys.file.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {

    private StorageType type;

    private LocalProperties local;
    private MinioProperties minio;
    private OSSProperties oss;

    private String maxFileSize;
    private String maxRequestSize;

    private List<String> allowedExtensions;
    private List<String> previewableExtensions;

    @Data
    public static class LocalProperties {
        private String basePath;
        private String accessUrl;
    }

    @Data
    public static class MinioProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String region;
    }

    @Data
    public static class OSSProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String region;
    }

    public enum StorageType {
        LOCAL, MINIO, OSS
    }
}
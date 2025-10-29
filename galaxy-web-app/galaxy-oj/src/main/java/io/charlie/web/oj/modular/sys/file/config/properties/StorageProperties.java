package io.charlie.web.oj.modular.sys.file.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "oj.storage")
@Data
public class StorageProperties {

    private StorageType type;

    private LocalProperties local;
    private MinioProperties minio;
    private OSSProperties oss;

    private String maxFileSize;
    private String maxRequestSize;

    private List<String> allowedExtensions;
    private List<String> previewExtensions;

    @Data
    public static class LocalProperties {
        private String basePath; // 存储文件的基础路径
        private String accessUrl; // 返回的文件访问URL
    }

    @Data
    public static class MinioProperties {
        private String endpoint; // MinIO服务端地址
        private String accessKey; // MinIO访问密钥
        private String secretKey; // MinIO密钥
        private String bucketName; // 存储桶名称
        private String region; // 存储桶区域
    }

    @Data
    public static class OSSProperties {
        private String endpoint; // OSS服务端地址
        private String accessKey; // OSS访问密钥
        private String secretKey; // OSS密钥
        private String bucketName; // 存储桶名称
        private String region; // 存储桶区域
    }

    public enum StorageType {
        LOCAL, MINIO, OSS
    }
}
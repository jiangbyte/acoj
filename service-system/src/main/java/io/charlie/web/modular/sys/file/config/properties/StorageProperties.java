package io.charlie.web.modular.sys.file.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "system.storage")
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
        /** SDK 连接地址（容器内可写 silo:9000 / minio:9000） */
        private String endpoint;
        /**
         * 浏览器可访问的对外基址（不含桶名）；为空则回退 endpoint。
         * Compose 场景通常为 http://localhost:9000，避免把内网主机名返回给前端。
         */
        private String accessUrl;
        private String accessKey; // MinIO访问密钥
        private String secretKey; // MinIO密钥
        private String bucketName; // 存储桶名称
        private String region; // 存储桶区域
        /** 私有桶预签名 URL 有效期（秒），默认 7 天 */
        private Integer presignedExpirySeconds = 7 * 24 * 3600;
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

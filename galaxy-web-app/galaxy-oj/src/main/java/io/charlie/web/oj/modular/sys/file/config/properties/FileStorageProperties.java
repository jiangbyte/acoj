package io.charlie.web.oj.modular.sys.file.config.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileStorageProperties {
    private boolean useMinIo;  // 新增字段
    private String uploadDir;
    private String baseUrl;
    private String maxFileSize;
    private String maxRequestSize;
    private List<String> allowedExtensions;
    private List<String> previewableExtensions;

    private MinioProperties minio;  // 新增MinIO配置

    @Getter
    @Setter
    public static class MinioProperties {
        private Boolean isPublicRead;
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private boolean secure;
        private String region;
    }
}
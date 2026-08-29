package io.charlie.web.modular.sys.file.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.charlie.cores.file.FileInfo;
import io.charlie.web.modular.sys.file.config.properties.StorageProperties;
import io.charlie.web.modular.sys.file.service.StorageService;
import io.charlie.web.modular.sys.file.util.StorageObjectNames;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 16/10/2025
 * @description MinIO存储服务实现
 */
@Service
@ConditionalOnProperty(name = "oj.storage.type", havingValue = "minio")
@Slf4j
public class MinioStorageService implements StorageService {

    @Autowired
    private StorageProperties storageProperties;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DEFAULT_PRESIGNED_EXPIRY_SECONDS = 7 * 24 * 3600;

    private MinioClient minioClient;
    private String bucketName;
    /** 桶是否允许匿名读；null 表示尚未检测 */
    private volatile Boolean bucketPublic;

    @Autowired
    public MinioStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        initMinioClient();
    }

    private void initMinioClient() {
        try {
            StorageProperties.MinioProperties minioProps = storageProperties.getMinio();
            this.bucketName = minioProps.getBucketName();

            this.minioClient = MinioClient.builder()
                    .endpoint(minioProps.getEndpoint())
                    .credentials(minioProps.getAccessKey(), minioProps.getSecretKey())
                    .region(minioProps.getRegion())
                    .build();

            // 检查并创建存储桶
            createBucketIfNotExists();
            this.bucketPublic = detectBucketPublic();
            log.info("MinIO客户端初始化成功, Bucket: {}, 公开访问: {}", bucketName, bucketPublic);

        } catch (Exception e) {
            log.error("MinIO客户端初始化失败", e);
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .region(storageProperties.getMinio().getRegion())
                    .build());
            log.info("创建MinIO存储桶: {}", bucketName);
        }
    }

    @Override
    public FileInfo upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("上传文件为空");
        }

        // 验证文件扩展名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);

        if (!isAllowedExtension(fileExtension)) {
            throw new IOException("不支持的文件类型: " + fileExtension);
        }

        // 生成唯一文件名
        String filename = generateUniqueFilename(originalFilename);

        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 构建文件信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalFilename(originalFilename);
            fileInfo.setFileType(file.getContentType());
            fileInfo.setSize(file.getSize());
            fileInfo.setExtension(fileExtension);
            fileInfo.setPreview(canPreview(fileExtension));
            fileInfo.setUrl(getUrl(filename));
            fileInfo.setStorageTime(System.currentTimeMillis());

            log.info("文件上传到MinIO成功: {}", filename);
            return fileInfo;

        } catch (Exception e) {
            log.error("文件上传到MinIO失败: {}", filename, e);
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource download(String filename) throws FileNotFoundException {
        try {
            // 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            // 获取文件流
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            log.info("从MinIO下载文件: {}", filename);
            return new InputStreamResource(stream) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

        } catch (ErrorResponseException e) {
            log.error("文件不存在: {}", filename, e);
            throw new FileNotFoundException("文件不存在: " + filename);
        } catch (Exception e) {
            log.error("从MinIO下载文件失败: {}", filename, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean previewed(String filename) {
        try {
            String extension = getFileExtension(filename);
            return canPreview(extension);
        } catch (Exception e) {
            log.error("检查文件预览支持失败: {}", filename, e);
            return false;
        }
    }

    @Override
    public boolean delete(String filename) throws IOException {
        try {
            // 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            // 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            log.info("从MinIO删除文件成功: {}", filename);
            return true;

        } catch (ErrorResponseException e) {
            log.error("文件不存在: {}", filename, e);
            throw new FileNotFoundException("文件不存在: " + filename);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败: {}", filename, e);
            throw new IOException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUrl(String objectName) {
        try {
            if (!StringUtils.hasText(objectName)) {
                return "";
            }
            String cleaned = StorageObjectNames.stripQuery(objectName);
            // 非本桶且不像本地上传文件的外链原样返回
            if (isExternalUrl(cleaned) && !isOurMinioUrl(cleaned) && !StorageObjectNames.looksLikeStoredFile(cleaned)) {
                return cleaned;
            }
            String filename = toObjectName(objectName);
            if (!StringUtils.hasText(filename) || isExternalUrl(filename)) {
                return filename;
            }
            if (isBucketPublic()) {
                return buildPublicUrl(filename);
            }
            return getPresignedUrl(filename, getPresignedExpirySeconds());
        } catch (Exception e) {
            log.error("生成文件URL失败: {}", objectName, e);
            return "";
        }
    }

    @Override
    public String toAccessUrl(String stored) {
        if (!StringUtils.hasText(stored)) {
            return stored;
        }
        String cleaned = StorageObjectNames.stripQuery(stored);
        // 纯文本配置（如 APP_NAME）不转换
        if (!isOurMinioUrl(cleaned) && !StorageObjectNames.looksLikeStoredFile(cleaned)) {
            return stored;
        }
        return getUrl(toObjectName(stored));
    }

    @Override
    public String toObjectName(String storedOrUrl) {
        if (!StringUtils.hasText(storedOrUrl)) {
            return storedOrUrl;
        }
        String cleaned = StorageObjectNames.stripQuery(storedOrUrl);
        if (isOurMinioUrl(cleaned)) {
            String prefix = buildPublicUrlPrefix();
            if (cleaned.startsWith(prefix)
                    || cleaned.startsWith(prefix.replace("https://", "http://"))
                    || cleaned.startsWith(prefix.replace("http://", "https://"))) {
                String after = cleaned;
                if (cleaned.startsWith(prefix)) {
                    after = cleaned.substring(prefix.length());
                } else if (cleaned.startsWith(prefix.replace("https://", "http://"))) {
                    after = cleaned.substring(prefix.replace("https://", "http://").length());
                } else {
                    after = cleaned.substring(prefix.replace("http://", "https://").length());
                }
                return after.contains("/") ? StorageObjectNames.extractFileName(after) : after;
            }
            return StorageObjectNames.extractFileName(cleaned);
        }
        // 历史错误相对路径 /content/xxx.jpg、完整网关 URL 等：抽文件名入库
        if (StorageObjectNames.looksLikeStoredFile(cleaned)) {
            return StorageObjectNames.extractFileName(cleaned);
        }
        // 外链（picsum 等）原样保留
        return cleaned;
    }

    private String buildPublicUrl(String objectName) {
        return buildPublicUrlPrefix() + objectName;
    }

    private String buildPublicUrlPrefix() {
        String endpoint = storageProperties.getMinio().getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + bucketName + "/";
    }

    private boolean isOurMinioUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String prefix = buildPublicUrlPrefix();
        return value.startsWith(prefix)
                || value.startsWith(prefix.replace("https://", "http://"))
                || value.startsWith(prefix.replace("http://", "https://"));
    }

    private boolean isExternalUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private boolean isBucketPublic() {
        Boolean cached = this.bucketPublic;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (this.bucketPublic == null) {
                this.bucketPublic = detectBucketPublic();
            }
            return this.bucketPublic;
        }
    }

    /**
     * 根据桶策略判断是否允许匿名 GetObject（公开读）
     */
    private boolean detectBucketPublic() {
        try {
            String policy = minioClient.getBucketPolicy(
                    GetBucketPolicyArgs.builder().bucket(bucketName).build());
            if (!StringUtils.hasText(policy)) {
                return false;
            }
            return policyAllowsAnonymousGetObject(policy);
        } catch (ErrorResponseException e) {
            String code = e.errorResponse() != null ? e.errorResponse().code() : null;
            if ("NoSuchBucketPolicy".equals(code)) {
                return false;
            }
            log.warn("获取桶策略失败，按私有桶处理: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("检测桶公开性失败，按私有桶处理: {}", e.getMessage());
            return false;
        }
    }

    private boolean policyAllowsAnonymousGetObject(String policyJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(policyJson);
            JsonNode statements = root.get("Statement");
            if (statements == null || statements.isNull()) {
                return false;
            }
            if (!statements.isArray()) {
                return statementAllowsAnonymousGetObject(statements);
            }
            for (JsonNode statement : statements) {
                if (statementAllowsAnonymousGetObject(statement)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("解析桶策略失败，按私有桶处理: {}", e.getMessage());
            return false;
        }
    }

    private boolean statementAllowsAnonymousGetObject(JsonNode statement) {
        if (statement == null || statement.isNull()) {
            return false;
        }
        JsonNode effect = statement.get("Effect");
        if (effect == null || !"Allow".equalsIgnoreCase(effect.asText())) {
            return false;
        }
        if (!principalIsAnonymous(statement.get("Principal"))) {
            return false;
        }
        return actionAllowsGetObject(statement.get("Action"));
    }

    private boolean principalIsAnonymous(JsonNode principal) {
        if (principal == null || principal.isNull()) {
            return false;
        }
        if (principal.isTextual()) {
            return "*".equals(principal.asText());
        }
        if (principal.isObject()) {
            JsonNode aws = principal.get("AWS");
            if (aws == null) {
                return false;
            }
            if (aws.isTextual()) {
                return "*".equals(aws.asText());
            }
            if (aws.isArray()) {
                for (JsonNode item : aws) {
                    if ("*".equals(item.asText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean actionAllowsGetObject(JsonNode action) {
        if (action == null || action.isNull()) {
            return false;
        }
        if (action.isTextual()) {
            return isGetObjectAction(action.asText());
        }
        if (action.isArray()) {
            for (JsonNode item : action) {
                if (isGetObjectAction(item.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGetObjectAction(String action) {
        return "s3:GetObject".equals(action) || "s3:*".equals(action) || "*".equals(action);
    }

    private int getPresignedExpirySeconds() {
        Integer configured = storageProperties.getMinio().getPresignedExpirySeconds();
        if (configured == null || configured < 1) {
            return DEFAULT_PRESIGNED_EXPIRY_SECONDS;
        }
        return configured;
    }

    @Override
    public StorageProperties.StorageType getType() {
        return StorageProperties.StorageType.MINIO;
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String filename) throws IOException {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalFilename(filename);
            fileInfo.setFileType(stat.contentType());
            fileInfo.setSize(stat.size());
            fileInfo.setExtension(getFileExtension(filename));
            fileInfo.setPreview(canPreview(getFileExtension(filename)));
            fileInfo.setUrl(getUrl(filename));
            fileInfo.setStorageTime(stat.lastModified().toInstant().toEpochMilli());

            return fileInfo;

        } catch (ErrorResponseException e) {
            throw new FileNotFoundException("文件不存在: " + filename);
        } catch (Exception e) {
            log.error("获取MinIO文件信息失败: {}", filename, e);
            throw new IOException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String filename) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取预签名URL（用于临时访问）
     *
     * @param filename       对象名
     * @param expirySeconds  有效期（秒）
     */
    public String getPresignedUrl(String filename, int expirySeconds) throws IOException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filename)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", filename, e);
            throw new IOException("生成访问链接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制文件
     */
    public boolean copy(String sourceFilename, String targetFilename) throws IOException {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetFilename)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(sourceFilename)
                                            .build()
                            )
                            .build()
            );
            log.info("文件复制成功: {} -> {}", sourceFilename, targetFilename);
            return true;
        } catch (Exception e) {
            log.error("文件复制失败: {} -> {}", sourceFilename, targetFilename, e);
            throw new IOException("文件复制失败: " + e.getMessage(), e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (StringUtils.hasText(extension) ? "." + extension : "");
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        return storageProperties.getAllowedExtensions() == null ||
                storageProperties.getAllowedExtensions().isEmpty() ||
                storageProperties.getAllowedExtensions().contains(extension.toLowerCase());
    }

    private boolean canPreview(String extension) {
        return storageProperties.getPreviewExtensions() != null &&
                storageProperties.getPreviewExtensions().contains(extension.toLowerCase());
    }
}
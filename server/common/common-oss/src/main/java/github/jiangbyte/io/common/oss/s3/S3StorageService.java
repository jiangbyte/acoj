package github.jiangbyte.io.common.oss.s3;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.oss.StorageService;
import github.jiangbyte.io.common.oss.config.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 基于 S3 兼容 API 的对象存储实现。
 *
 * Author: Charlie
 */
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final OssProperties properties;

    /** 上传对象到 S3。 */
    @Override
    public String put(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            return publicUrl(objectKey);
        } catch (RuntimeException exception) {
            String detail = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            throw new BizException(500, "Failed to store object to S3: " + detail);
        }
    }

    /** 删除 S3 对象。S3 删除为幂等操作：对象不存在（NoSuchKey/404）视为已删除。 */
    @Override
    public void delete(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(objectKey)
                    .build());
        } catch (NoSuchKeyException exception) {
            // 对象已不存在：幂等删除成功
        } catch (S3Exception exception) {
            if (isNotFound(exception)) {
                return;
            }
            throw new BizException(500, "Failed to delete S3 object: " + describe(exception));
        } catch (RuntimeException exception) {
            String detail = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            throw new BizException(500, "Failed to delete S3 object: " + detail);
        }
    }

    /** 从 S3 加载对象。 */
    @Override
    public Resource load(String objectKey) {
        try {
            InputStream stream = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(objectKey)
                    .build());
            return new InputStreamResource(stream);
        } catch (RuntimeException exception) {
            throw new BizException(404, "File not found");
        }
    }

    /**
     * 返回可访问 URL：公开桶→永久直链（优先自定义 Base URL）；非公开→预签名（可选 Base URL host 改写）。
     */
    @Override
    public String publicUrl(String objectKey) {
        String key = normalizeKey(objectKey);
        if (properties.getS3().isBucketPublic()) {
            return buildPublicDirectUrl(key);
        }
        int expireSeconds = properties.getS3().getPresignExpireSeconds();
        if (expireSeconds <= 0) {
            expireSeconds = 3600;
        }
        return presignedUrl(key, Duration.ofSeconds(expireSeconds));
    }

    private String buildPublicDirectUrl(String key) {
        String publicBaseUrl = properties.getS3().getPublicBaseUrl();
        if (StringUtils.hasText(publicBaseUrl)) {
            return joinBaseAndKey(publicBaseUrl, key);
        }
        return buildEndpointObjectUrl(key);
    }

    private String buildEndpointObjectUrl(String key) {
        String bucket = properties.getS3().getBucket();
        String endpoint = properties.getS3().getEndpoint();
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(endpoint)) {
            throw new BizException(500, "S3 bucket/endpoint is required for public URL");
        }
        String encodedKey = quoteObjectKey(key);
        String origin = normalizeEndpointOrigin(endpoint);
        if (properties.getS3().isPathStyleAccess()) {
            return origin + "/" + bucket + "/" + encodedKey;
        }
        // virtual-host: https://bucket.endpoint/key
        URI uri = URI.create(origin);
        String host = uri.getRawAuthority();
        if (!StringUtils.hasText(host)) {
            return origin + "/" + bucket + "/" + encodedKey;
        }
        String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
        return scheme + "://" + bucket + "." + host + "/" + encodedKey;
    }

    private static String normalizeEndpointOrigin(String endpoint) {
        String value = endpoint.trim();
        if (!value.contains("://")) {
            value = "https://" + value;
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String joinBaseAndKey(String baseUrl, String key) {
        String base = baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + quoteObjectKey(key);
    }

    private static String quoteObjectKey(String objectKey) {
        return Arrays.stream(objectKey.replace('\\', '/').split("/"))
                .filter(StringUtils::hasText)
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

    private static boolean isNotFound(S3Exception exception) {
        int status = exception.statusCode();
        if (status == 404) {
            return true;
        }
        String code = exception.awsErrorDetails() == null
                ? null
                : exception.awsErrorDetails().errorCode();
        return code != null && (code.equalsIgnoreCase("NoSuchKey")
                || code.equalsIgnoreCase("NoSuchBucket")
                || code.equalsIgnoreCase("NotFound")
                || code.equalsIgnoreCase("NoSuchObject"));
    }

    private static String describe(S3Exception exception) {
        String code = exception.awsErrorDetails() == null
                ? null
                : exception.awsErrorDetails().errorCode();
        String message = exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
        return code == null ? message : code + " (" + statusCodeLabel(exception.statusCode()) + "): " + message;
    }

    private static String statusCodeLabel(int status) {
        return status == 0 ? "unknown" : String.valueOf(status);
    }

    private static String normalizeKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return "";
        }
        return objectKey.replace('\\', '/').replaceAll("^/+", "");
    }

    /** 生成 S3 预签名 URL。 */
    @Override
    public String presignedUrl(String objectKey, Duration ttl) {
        try {
            Duration expire = ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofMinutes(15) : ttl;
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(normalizeKey(objectKey))
                    .build();
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                    .signatureDuration(expire)
                    .getObjectRequest(getObjectRequest)
                    .build());
            String signed = presigned.url().toExternalForm();
            return rewritePublicHost(signed);
        } catch (RuntimeException exception) {
            throw new BizException(500, "Failed to create S3 presigned URL");
        }
    }

    /**
     * 保留 SigV4 查询参数；可选地将 scheme/host 改写为 CDN / 公网 base。
     */
    private String rewritePublicHost(String signedUrl) {
        // 私有桶不做 host 改写，避免 SigV4 SignatureDoesNotMatch
        if (!properties.getS3().isBucketPublic()) {
            return signedUrl;
        }
        String publicBaseUrl = properties.getS3().getPublicBaseUrl();
        if (!StringUtils.hasText(publicBaseUrl)) {
            return signedUrl;
        }
        try {
            URI signed = URI.create(signedUrl);
            URI pub = URI.create(publicBaseUrl.endsWith("/")
                    ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                    : publicBaseUrl);
            StringBuilder rebuilt = new StringBuilder();
            rebuilt.append(pub.getScheme() != null ? pub.getScheme() : signed.getScheme()).append("://");
            rebuilt.append(pub.getRawAuthority() != null ? pub.getRawAuthority() : signed.getRawAuthority());
            if (StringUtils.hasText(pub.getRawPath()) && !"/".equals(pub.getRawPath())) {
                String basePath = pub.getRawPath().endsWith("/")
                        ? pub.getRawPath().substring(0, pub.getRawPath().length() - 1)
                        : pub.getRawPath();
                String objectPath = signed.getRawPath() == null ? "" : signed.getRawPath();
                rebuilt.append(basePath).append(objectPath);
            } else {
                rebuilt.append(signed.getRawPath() == null ? "" : signed.getRawPath());
            }
            if (StringUtils.hasText(signed.getRawQuery())) {
                rebuilt.append('?').append(signed.getRawQuery());
            }
            return rebuilt.toString();
        } catch (IllegalArgumentException ignored) {
            return signedUrl;
        }
    }
}

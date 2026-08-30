package github.jiangbyte.io.common.oss.s3;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.oss.StorageService;
import github.jiangbyte.io.common.oss.config.OssProperties;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * S3 客户端工厂：按 OssProperties 构建 AWS SDK / 兼容端点客户端。
 *
 * Author: Charlie
 */
public final class S3ClientFactory {

    private S3ClientFactory() {
    }

    /** 按配置创建 S3 客户端。 */
    public static ManagedS3Storage create(OssProperties properties) {
        if (properties == null || properties.getS3() == null) {
            throw new BizException(500, "S3 storage properties required");
        }
        OssProperties.S3 s3 = properties.getS3();
        if (!StringUtils.hasText(s3.getBucket())) {
            throw new BizException("存储引擎未配置: bucket / Storage engine missing: bucket");
        }
        if (!StringUtils.hasText(s3.getAccessKey()) || !StringUtils.hasText(s3.getSecretKey())) {
            throw new BizException("存储引擎未配置: ACCESS_KEY/SECRET_KEY / Storage engine missing ACCESS_KEY/SECRET_KEY");
        }
        String region = StringUtils.hasText(s3.getRegion()) ? s3.getRegion().trim() : "us-east-1";
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3.getAccessKey().trim(), s3.getSecretKey().trim()));

        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials);
        if (StringUtils.hasText(s3.getEndpoint())) {
            clientBuilder.endpointOverride(normalizeEndpoint(s3.getEndpoint()));
        }
        if (s3.isPathStyleAccess()) {
            clientBuilder.forcePathStyle(true);
        }
        S3Client client = clientBuilder.build();

        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials);
        if (StringUtils.hasText(s3.getEndpoint())) {
            presignerBuilder.endpointOverride(normalizeEndpoint(s3.getEndpoint()));
        }
        if (s3.isPathStyleAccess()) {
            presignerBuilder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        S3Presigner presigner = presignerBuilder.build();
        StorageService service = new S3StorageService(client, presigner, properties);
        return new ManagedS3Storage(service, client, presigner);
    }

    private static URI normalizeEndpoint(String endpoint) {
        String value = endpoint.trim();
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "https://" + value;
        }
        return URI.create(value);
    }

    public static final class ManagedS3Storage implements StorageService, AutoCloseable {
        private final StorageService delegate;
        private final S3Client client;
        private final S3Presigner presigner;

        ManagedS3Storage(StorageService delegate, S3Client client, S3Presigner presigner) {
            this.delegate = delegate;
            this.client = client;
            this.presigner = presigner;
        }

        @Override
        public String put(String objectKey, java.io.InputStream inputStream, long contentLength, String contentType) {
            return delegate.put(objectKey, inputStream, contentLength, contentType);
        }

        @Override
        public void delete(String objectKey) {
            delegate.delete(objectKey);
        }

        @Override
        public org.springframework.core.io.Resource load(String objectKey) {
            return delegate.load(objectKey);
        }

        @Override
        public String publicUrl(String objectKey) {
            return delegate.publicUrl(objectKey);
        }

        @Override
        public String presignedUrl(String objectKey, java.time.Duration ttl) {
            return delegate.presignedUrl(objectKey, ttl);
        }

        @Override
        public void close() {
            try {
                presigner.close();
            } catch (Exception ignored) {
                // 忽略
            }
            try {
                client.close();
            } catch (Exception ignored) {
                // 忽略
            }
        }
    }
}

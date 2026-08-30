package github.jiangbyte.io.sys.modules.storage;

import github.jiangbyte.io.common.oss.config.OssProperties;
import lombok.Builder;
import lombok.Value;

/**
 * 由 sys_config / RuntimeSettings 构建的运行时存储快照。
 *
 * Author: Charlie
 */
@Value
@Builder
public class ResolvedStorageConfig {

    String id;
    String engine;
    String provider;
    String bucket;
    String endpoint;
    String accessKey;
    String secretKey;
    String region;
    boolean useSsl;
    boolean pathStyleAccess;
    String baseUrl;
    boolean bucketPublic;
    long uploadMaxBytes;
    int presignExpireSeconds;

    /** 是否 S3 兼容存储。 */
    public boolean isS3Compatible() {
        return FileEngines.isS3Compatible(provider);
    }

    /** 转为 OssProperties。 */
    public OssProperties toOssProperties() {
        OssProperties props = new OssProperties();
        props.setType(FileEngines.toOssType(provider));
        props.getS3().setBucket(bucket);
        props.getS3().setEndpoint(endpoint);
        props.getS3().setAccessKey(accessKey);
        props.getS3().setSecretKey(secretKey);
        props.getS3().setRegion(region == null || region.isBlank() ? "us-east-1" : region);
        props.getS3().setPublicBaseUrl(baseUrl);
        props.getS3().setPathStyleAccess(pathStyleAccess);
        props.getS3().setBucketPublic(bucketPublic);
        props.getS3().setPresignExpireSeconds(presignExpireSeconds > 0 ? presignExpireSeconds : 3600);
        props.getUpload().setMaxSizeBytes(uploadMaxBytes);
        return props;
    }
}

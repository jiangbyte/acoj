package github.jiangbyte.io.common.oss;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.time.Duration;

/**
 * 对象存储抽象：上传、下载、删除与预签名 URL 等统一能力。
 *
 * Author: Charlie
 */
public interface StorageService {

    /** 上传对象并返回存储键或访问标识。 */
    String put(String objectKey, InputStream inputStream, long contentLength, String contentType);

    /** 删除指定对象。 */
    void delete(String objectKey);

    /** 加载对象为 Resource。 */
    Resource load(String objectKey);

    /** 返回对象的公开访问 URL（公开直连或预签名）。 */
    String publicUrl(String objectKey);

    /** 生成带过期时间的预签名访问 URL。 */
    default String presignedUrl(String objectKey, Duration ttl) {
        return publicUrl(objectKey);
    }
}

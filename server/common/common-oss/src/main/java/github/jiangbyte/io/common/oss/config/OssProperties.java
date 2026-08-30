package github.jiangbyte.io.common.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象存储配置属性：S3 兼容端点、凭证与访问策略。
 *
 * Author: Charlie
 */
@Data
@ConfigurationProperties(prefix = "hei.storage")
public class OssProperties {

    private String type = "s3";
    private S3 s3 = new S3();
    private Upload upload = new Upload();

    @Data
    public static class S3 {
        private String bucket;
        private String region = "us-east-1";
        private String endpoint;
        /**
         * 自定义访问基础 URL（CDN / 自定义域名）。
         * 公开桶：永久直链前缀；非公开：预签名后可选 host 改写。
         */
        private String publicBaseUrl;
        private String accessKey;
        private String secretKey;
        /** MinIO / 自定义 endpoint 的 path-style 访问。 */
        private boolean pathStyleAccess = false;
        /** 桶是否公开可读。公开→直连；非公开→预签名。 */
        private boolean bucketPublic = false;
        /** 非公开预签名 TTL 秒数（默认 3600）。 */
        private int presignExpireSeconds = 3600;
    }

    @Data
    public static class Upload {
        /** 最大上传字节数（默认 20 MiB）。 */
        private long maxSizeBytes = 20L * 1024 * 1024;
        private List<String> allowedExtensions = defaultExtensions();
        private List<String> allowedContentTypes = defaultContentTypes();

        private static List<String> defaultExtensions() {
            List<String> list = new ArrayList<>();
            list.add(".png");
            list.add(".jpg");
            list.add(".jpeg");
            list.add(".gif");
            list.add(".webp");
            list.add(".pdf");
            list.add(".txt");
            list.add(".md");
            list.add(".doc");
            list.add(".docx");
            list.add(".xls");
            list.add(".xlsx");
            list.add(".ppt");
            list.add(".pptx");
            list.add(".zip");
            list.add(".mp3");
            list.add(".mp4");
            list.add(".wav");
            return list;
        }

        private static List<String> defaultContentTypes() {
            List<String> list = new ArrayList<>();
            list.add("image/png");
            list.add("image/jpeg");
            list.add("image/gif");
            list.add("image/webp");
            list.add("application/pdf");
            list.add("text/plain");
            list.add("text/markdown");
            list.add("application/msword");
            list.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            list.add("application/vnd.ms-excel");
            list.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            list.add("application/vnd.ms-powerpoint");
            list.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            list.add("application/zip");
            list.add("audio/mpeg");
            list.add("audio/wav");
            list.add("video/mp4");
            list.add("application/octet-stream");
            return list;
        }
    }
}

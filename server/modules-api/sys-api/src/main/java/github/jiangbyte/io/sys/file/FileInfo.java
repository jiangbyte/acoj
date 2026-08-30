package github.jiangbyte.io.sys.file;

import lombok.Data;

/**
 * 跨模块文件快照：存储键、原始文件名、类型、大小与已解析访问 URL。
 * 非 HTTP 结果，亦非持久化实体。
 *
 * Author: Charlie
 */
@Data
public class FileInfo {
    private String id;
    private String objectName;
    private String originalName;
    private String contentType;
    private Long size;
    private String url;
}

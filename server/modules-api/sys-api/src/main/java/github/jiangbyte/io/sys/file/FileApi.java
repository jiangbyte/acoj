package github.jiangbyte.io.sys.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 跨模块文件门面：上传/删除、object_name 规范化与可访问 URL 解析。
 * HTTP 类型留在 {@code module/sys}；实现为 {@code FileApiProvider}。
 * URL 解析对齐 hei-fastapi {@code resolve_file_url}。
 *
 * Author: Charlie
 */
public interface FileApi {

    /** 上传文件并返回元数据快照。 */
    FileInfo upload(MultipartFile file, String storageProvider);

    /** 按 object_name 删除存储对象及元数据。 */
    void deleteByObjectName(String objectName);

    /**
     * 将存储值规范化为 object_name（外部 URL 则原样返回）。
     * 对齐 hei-fastapi {@code normalize_object_name}。
     */
    String normalizeObjectName(String value);

    /**
     * 将 object_name / 公网路径 / 外部 URL 解析为浏览器可访问 URL。
     * 对齐 hei-fastapi {@code resolve_file_url}。
     */
    String resolveUrl(String objectNameOrUrl);

    /** 批量解析 URL；缺失或空白的键会被省略。 */
    Map<String, String> resolveUrls(Collection<String> objectNameOrUrls);

    /** 按 object_name 查询文件元数据；url 字段已解析。 */
    List<FileInfo> listByObjectNames(Collection<String> objectNames);
}

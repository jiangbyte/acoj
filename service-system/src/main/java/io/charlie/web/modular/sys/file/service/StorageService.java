package io.charlie.web.modular.sys.file.service;

import io.charlie.web.modular.sys.file.config.properties.StorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import io.charlie.cores.file.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface StorageService {
    /**
     * 文件上传
     */
    FileInfo upload(MultipartFile file) throws IOException;

    /**
     * 文件下载
     */
    Resource download(String path) throws FileNotFoundException;

    /**
     * 判断文件是否可预览
     */
    boolean previewed(String path);

    /**
     * 删除文件
     */
    boolean delete(String path) throws IOException;

    /**
     * 根据对象名生成可访问 URL（公开路径或预签名）
     */
    String getUrl(String objectName);

    /**
     * 将库中存储值转为可访问 URL。
     * 库中一般为对象名；兼容历史完整 URL / 外链。
     */
    default String toAccessUrl(String stored) {
        return getUrl(stored);
    }

    /**
     * 将上传回传的 URL 或对象名规范为库中应存储的对象名。
     * 外链原样返回。
     */
    default String toObjectName(String storedOrUrl) {
        return storedOrUrl;
    }

    /**
     * 获取服务类型
     */
    StorageProperties.StorageType getType();
}
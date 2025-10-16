package io.charlie.web.oj.modular.sys.file.service;

import io.charlie.web.oj.modular.sys.file.config.properties.StorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import io.charlie.galaxy.file.FileInfo;

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
     * 获取文件访问URL
     */
    String getUrl(String path);

    /**
     * 获取服务类型
     */
    StorageProperties.StorageType getType();
}
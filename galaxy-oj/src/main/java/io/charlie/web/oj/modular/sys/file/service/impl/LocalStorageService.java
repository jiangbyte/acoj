package io.charlie.web.oj.modular.sys.file.service.impl;

import io.charlie.galaxy.file.FileInfo;
import io.charlie.web.oj.modular.sys.file.config.properties.StorageProperties;
import io.charlie.web.oj.modular.sys.file.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 16/10/2025
 * @description 本地存储服务实现
 */
@Service
@ConditionalOnProperty(name = "oj.storage.type", havingValue = "local")
@Slf4j
public class LocalStorageService implements StorageService {

    @Autowired
    private StorageProperties storageProperties;

    private final Path basePath;

    public LocalStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.basePath = Paths.get(storageProperties.getLocal().getBasePath()).toAbsolutePath().normalize();
        initStorageDirectory();
    }

    private void initStorageDirectory() {
        try {
            Files.createDirectories(basePath);
            log.info("本地存储目录初始化完成: {}", basePath);
        } catch (IOException e) {
            log.error("创建本地存储目录失败: {}", basePath, e);
            throw new RuntimeException("无法创建存储目录", e);
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
        Path targetLocation = basePath.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

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

        log.info("文件上传成功: {}", filename);
        return fileInfo;
    }

    @Override
    public Resource download(String filename) throws FileNotFoundException {
        try {
            Path filePath = basePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("文件下载: {}", filename);
                return resource;
            } else {
                throw new FileNotFoundException("文件不存在或不可读: " + filename);
            }
        } catch (IOException e) {
            log.error("文件下载失败: {}", filename, e);
            throw new FileNotFoundException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public boolean previewed(String filename) {
        Path filePath = basePath.resolve(filename).normalize();
        File file = filePath.toFile();

        if (file.exists() && file.isFile()) {
            String extension = getFileExtension(filename);
            return canPreview(extension);
        }
        return false;
    }

    @Override
    public boolean delete(String filename) throws IOException {
        Path filePath = basePath.resolve(filename).normalize();

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + filename);
        }

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件删除成功: {}", filename);
            }
            return deleted;
        } catch (IOException e) {
            log.error("文件删除失败: {}", filename, e);
            throw new IOException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUrl(String filename) {
        String accessUrl = storageProperties.getLocal().getAccessUrl();
        if (!accessUrl.endsWith("/")) {
            accessUrl += "/";
        }
        return accessUrl + filename;
    }

    @Override
    public StorageProperties.StorageType getType() {
        return StorageProperties.StorageType.LOCAL;
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String filename) throws IOException {
        Path filePath = basePath.resolve(filename).normalize();
        File file = filePath.toFile();

        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("文件不存在: " + filename);
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(filename);
        fileInfo.setOriginalFilename(filename); // 本地存储中文件名就是原始文件名
        fileInfo.setFileType(Files.probeContentType(filePath));
        fileInfo.setSize(file.length());
        fileInfo.setExtension(getFileExtension(filename));
        fileInfo.setPreview(canPreview(getFileExtension(filename)));
        fileInfo.setUrl(getUrl(filename));
        fileInfo.setStorageTime(file.lastModified());

        return fileInfo;
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String filename) {
        Path filePath = basePath.resolve(filename).normalize();
        return Files.exists(filePath) && Files.isRegularFile(filePath);
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
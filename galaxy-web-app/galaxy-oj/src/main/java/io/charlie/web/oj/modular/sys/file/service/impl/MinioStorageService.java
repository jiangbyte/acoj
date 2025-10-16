package io.charlie.web.oj.modular.sys.file.service.impl;

import io.charlie.galaxy.file.FileInfo;
import io.charlie.web.oj.modular.sys.file.config.properties.StorageProperties;
import io.charlie.web.oj.modular.sys.file.service.StorageService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.UUID;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 16/10/2025
 * @description MinIO存储服务实现
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
@Slf4j
public class MinioStorageService implements StorageService {

    @Autowired
    private StorageProperties storageProperties;

    private MinioClient minioClient;
    private String bucketName;

    @Autowired
    public MinioStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        initMinioClient();
    }

    private void initMinioClient() {
        try {
            StorageProperties.MinioProperties minioProps = storageProperties.getMinio();
            this.bucketName = minioProps.getBucketName();

            this.minioClient = MinioClient.builder()
                    .endpoint(minioProps.getEndpoint())
                    .credentials(minioProps.getAccessKey(), minioProps.getSecretKey())
                    .region(minioProps.getRegion())
                    .build();

            // 检查并创建存储桶
            createBucketIfNotExists();
            log.info("MinIO客户端初始化成功, Bucket: {}", bucketName);

        } catch (Exception e) {
            log.error("MinIO客户端初始化失败", e);
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .region(storageProperties.getMinio().getRegion())
                    .build());
            log.info("创建MinIO存储桶: {}", bucketName);
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

        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

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

            log.info("文件上传到MinIO成功: {}", filename);
            return fileInfo;

        } catch (Exception e) {
            log.error("文件上传到MinIO失败: {}", filename, e);
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }

/* ============================================================ 压缩 ============================================================ */

/* ============================================================ 压缩 ============================================================ */



    @Override
    public Resource download(String filename) throws FileNotFoundException {
        try {
            // 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            // 获取文件流
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            log.info("从MinIO下载文件: {}", filename);
            return new InputStreamResource(stream) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

        } catch (ErrorResponseException e) {
            log.error("文件不存在: {}", filename, e);
            throw new FileNotFoundException("文件不存在: " + filename);
        } catch (Exception e) {
            log.error("从MinIO下载文件失败: {}", filename, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean previewed(String filename) {
        try {
            String extension = getFileExtension(filename);
            return canPreview(extension);
        } catch (Exception e) {
            log.error("检查文件预览支持失败: {}", filename, e);
            return false;
        }
    }

    @Override
    public boolean delete(String filename) throws IOException {
        try {
            // 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            // 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            log.info("从MinIO删除文件成功: {}", filename);
            return true;

        } catch (ErrorResponseException e) {
            log.error("文件不存在: {}", filename, e);
            throw new FileNotFoundException("文件不存在: " + filename);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败: {}", filename, e);
            throw new IOException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUrl(String filename) {
        try {
            // 生成永久访问URL（需要确保存储桶策略允许公开访问或使用预签名URL）
            // 这里返回对象在MinIO中的访问路径
            return String.format("%s/%s/%s",
                    storageProperties.getMinio().getEndpoint(),
                    bucketName,
                    filename);
        } catch (Exception e) {
            log.error("生成文件URL失败: {}", filename, e);
            return "";
        }
    }

    @Override
    public StorageProperties.StorageType getType() {
        return StorageProperties.StorageType.MINIO;
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String filename) throws IOException {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalFilename(filename);
            fileInfo.setFileType(stat.contentType());
            fileInfo.setSize(stat.size());
            fileInfo.setExtension(getFileExtension(filename));
            fileInfo.setPreview(canPreview(getFileExtension(filename)));
            fileInfo.setUrl(getUrl(filename));
            fileInfo.setStorageTime(stat.lastModified().toInstant().toEpochMilli());

            return fileInfo;

        } catch (ErrorResponseException e) {
            throw new FileNotFoundException("文件不存在: " + filename);
        } catch (Exception e) {
            log.error("获取MinIO文件信息失败: {}", filename, e);
            throw new IOException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String filename) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取预签名URL（用于临时访问）
     */
    public String getPresignedUrl(String filename, int expiryMinutes) throws IOException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filename)
                            .expiry(expiryMinutes * 60)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", filename, e);
            throw new IOException("生成访问链接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制文件
     */
    public boolean copy(String sourceFilename, String targetFilename) throws IOException {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetFilename)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(sourceFilename)
                                            .build()
                            )
                            .build()
            );
            log.info("文件复制成功: {} -> {}", sourceFilename, targetFilename);
            return true;
        } catch (Exception e) {
            log.error("文件复制失败: {} -> {}", sourceFilename, targetFilename, e);
            throw new IOException("文件复制失败: " + e.getMessage(), e);
        }
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
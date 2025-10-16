package io.charlie.web.oj.modular.sys.file.service.impl;

import io.charlie.galaxy.file.FileInfo;
import io.charlie.web.oj.modular.sys.file.config.properties.FileStorageProperties;
import io.charlie.web.oj.modular.sys.file.service.FileStorageService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinIoFileStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final FileStorageProperties properties;
    private final String bucketName;

    public MinIoFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        FileStorageProperties.MinioProperties minioProps = properties.getMinio();

        this.minioClient = MinioClient.builder()
                .endpoint(minioProps.getEndpoint())
                .credentials(minioProps.getAccessKey(), minioProps.getSecretKey())
                .build();
        this.bucketName = minioProps.getBucketName();
        init();
    }

    private void init() {
        try {
            // 检查存储桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());

                // 对于长期存储，建议不设置公开读取策略，而是通过权限控制
                // 可以根据需要决定是否设置公开策略
                if (properties.getMinio().getIsPublicRead()) {
                    setPublicReadPolicy();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize MinIO storage", e);
        }
    }

    /**
     * 设置存储桶为公开读取策略
     */
    private void setPublicReadPolicy() throws Exception {
        String policy = """
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": "*",
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                    }
                ]
            }
            """.formatted(bucketName);

        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policy)
                .build());
    }

    @Override
    public FileInfo store(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = FilenameUtils.getExtension(originalFilename);

        // 验证文件扩展名
        if (!properties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new RuntimeException("File type not allowed: " + extension);
        }

        // 生成唯一文件名，包含时间戳便于管理
        String filename = generateLongTermFilename(originalFilename);

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }

            try (InputStream inputStream = file.getInputStream()) {
                // 上传文件到MinIO，长期存储不需要设置过期时间
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalFilename(originalFilename);
            fileInfo.setFileType(file.getContentType());
            fileInfo.setSize(file.getSize());
            fileInfo.setExtension(extension);
            fileInfo.setPreviewable(isPreviewable(filename));
            fileInfo.setUrl(getFileUrl(filename));
            // 添加存储时间戳
            fileInfo.setStorageTime(System.currentTimeMillis());

            return fileInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    /**
     * 生成长效存储的文件名
     * 格式: 年份/月份/UUID.扩展名
     */
    private String generateLongTermFilename(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();

        // 按年月分目录，便于管理和维护
        java.time.YearMonth currentYearMonth = java.time.YearMonth.now();
        String directory = currentYearMonth.getYear() + "/" +
                String.format("%02d", currentYearMonth.getMonthValue());

        return directory + "/" + uuid + "." + extension;
    }

    @Override
    public Resource loadAsResource(String filename) throws FileNotFoundException {
        try {
            GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());

            return new InputStreamResource(object) {
                @Override
                public String getFilename() {
                    return filename;
                }

                @Override
                public long contentLength() throws IOException {
                    try {
                        StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filename)
                                .build());
                        return stat.size();
                    } catch (Exception e) {
                        throw new IOException("Failed to get file size", e);
                    }
                }
            };
        } catch (Exception e) {
            throw new FileNotFoundException("Could not read file: " + filename);
        }
    }

    @Override
    public boolean isPreviewable(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return properties.getPreviewableExtensions().contains(extension);
    }

    @Override
    public void delete(String filename) throws IOException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            throw new IOException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public String getFileUrl(String filename) {
        // 长期存储的URL生成策略
        FileStorageProperties.MinioProperties minioProps = properties.getMinio();

        if (minioProps.getIsPublicRead()) {
            // 如果存储桶是公开的，直接返回永久URL
            return minioProps.getEndpoint() + "/" + bucketName + "/" + filename;
        } else {
            // 如果是私有存储桶，可以根据需要生成长期有效的预签名URL
            // 或者通过应用程序代理访问
            try {
                // 生成长期有效的预签名URL（例如30天）
                return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(filename)
                        .expiry(30, TimeUnit.DAYS) // 延长有效期
                        .build());
            } catch (Exception e) {
                // 如果生成预签名URL失败，返回基础URL
                return properties.getBaseUrl() + "/" + filename;
            }
        }
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String filename) throws IOException {
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setFileType(stat.contentType());
            fileInfo.setSize(stat.size());
            fileInfo.setExtension(FilenameUtils.getExtension(filename));
            fileInfo.setPreviewable(isPreviewable(filename));
            fileInfo.setUrl(getFileUrl(filename));
            fileInfo.setStorageTime(stat.lastModified().toInstant().toEpochMilli());

            return fileInfo;
        } catch (Exception e) {
            throw new IOException("Failed to get file info: " + filename, e);
        }
    }

    /**
     * 批量删除文件（长期存储可能需要批量清理）
     */
    public void batchDelete(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build());
            } catch (Exception e) {
                throw new IOException("Failed to delete file: " + filename, e);
            }
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String filename) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
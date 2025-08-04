package io.charlie.app.core.modular.file.service.impl;

import io.charlie.app.core.modular.file.config.properties.FileStorageProperties;
import io.charlie.app.core.modular.file.service.FileStorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import io.charlie.galaxy.file.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Primary
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;
    private final FileStorageProperties properties;

    public LocalFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        this.rootLocation = Paths.get(properties.getUploadDir());
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public FileInfo store(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = FilenameUtils.getExtension(originalFilename);

        // 验证文件扩展名
        if (!properties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new RuntimeException("File type not allowed");
        }

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + "." + extension;

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                throw new RuntimeException("Cannot store file with relative path outside current directory " + filename);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalFilename(originalFilename);
            fileInfo.setFileType(file.getContentType());
            fileInfo.setSize(file.getSize());
            fileInfo.setExtension(extension);
            fileInfo.setPreviewable(isPreviewable(filename));
            fileInfo.setUrl(getFileUrl(filename));

            return fileInfo;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) throws FileNotFoundException {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
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
        Path file = rootLocation.resolve(filename);
        Files.deleteIfExists(file);
    }

    @Override
    public String getFileUrl(String filename) {
        return properties.getBaseUrl() + "/" + filename;
    }
}
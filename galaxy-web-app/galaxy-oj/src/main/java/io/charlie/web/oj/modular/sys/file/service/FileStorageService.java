package io.charlie.web.oj.modular.sys.file.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import io.charlie.galaxy.file.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileStorageService {
    FileInfo store(MultipartFile file) throws IOException;

    Resource loadAsResource(String filename) throws FileNotFoundException;

    boolean isPreviewable(String filename);

    void delete(String filename) throws IOException;

    String getFileUrl(String filename);
}
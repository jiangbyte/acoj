package io.charlie.web.oj.modular.sys.file.config.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileStorageProperties {
    private String uploadDir;
    private String baseUrl;
    private String maxFileSize;
    private String maxRequestSize;
    private List<String> allowedExtensions;
    private List<String> previewableExtensions;
}
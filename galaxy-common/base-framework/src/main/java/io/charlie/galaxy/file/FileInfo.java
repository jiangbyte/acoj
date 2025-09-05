package io.charlie.galaxy.file;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 29/05/2025
 * @description 基础文件信息类
 */
@Data
public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String filename;
    private String originalFilename;
    private String fileType;
    private long size;
    private String extension;
    private boolean previewable;
    private String url;
}
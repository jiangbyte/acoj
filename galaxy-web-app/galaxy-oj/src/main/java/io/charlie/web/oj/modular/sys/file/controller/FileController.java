package io.charlie.web.oj.modular.sys.file.controller;

import io.charlie.web.oj.annotation.log.Log;
import io.charlie.web.oj.annotation.log.LogCategory;
import io.charlie.web.oj.annotation.log.LogModule;
import io.charlie.web.oj.modular.sys.file.config.StorageServiceFactory;
import io.charlie.web.oj.modular.sys.file.service.StorageService;
import io.charlie.web.oj.modular.sys.file.service.impl.LocalStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.file.FileInfo;
import io.charlie.galaxy.result.Result;

@Tag(name = "文件前端控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class FileController {
    private final StorageServiceFactory storageServiceFactory;

    @Log(category = LogCategory.OPERATION, module = LogModule.SYS)
    @Operation(summary = "文件上传")
    @PostMapping("/sys/file/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            FileInfo fileInfo = storageService.upload(file);
            return Result.success(fileInfo);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败" + e.getMessage());
        }
    }

    @Operation(summary = "文件下载")
    @GetMapping("/sys/file/download/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename,
                                             HttpServletRequest request) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            Resource resource = storageService.download(filename);

            // 尝试确定内容类型
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("文件下载失败: {}", filename, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文件预览")
    @GetMapping("/sys/file/preview/{filename:.+}")
    public ResponseEntity<Resource> preview(@PathVariable String filename,
                                            HttpServletRequest request) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();

            // 检查是否支持预览
            if (!storageService.previewed(filename)) {
                throw new BusinessException("该文件类型不支持预览");
            }

            Resource resource = storageService.download(filename);

            // 尝试确定内容类型
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (Exception e) {
            log.error("文件预览失败: {}", filename, e);
            throw new BusinessException("文件预览失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/sys/file/delete/{filename:.+}")
    public Result<Boolean> delete(@PathVariable String filename) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            boolean result = storageService.delete(filename);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件删除失败: {}", filename, e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/sys/file/info/{filename:.+}")
    public Result<FileInfo> getFileInfo(@PathVariable String filename) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();

            // 如果是本地存储服务，使用扩展的方法获取文件信息
            if (storageService instanceof LocalStorageService) {
                LocalStorageService localStorageService = (LocalStorageService) storageService;
                FileInfo fileInfo = localStorageService.getFileInfo(filename);
                return Result.success(fileInfo);
            } else {
                // 对于其他存储服务，可以返回基础信息
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilename(filename);
                fileInfo.setUrl(storageService.getUrl(filename));
                fileInfo.setPreview(storageService.previewed(filename));
                return Result.success(fileInfo);
            }
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", filename, e);
            throw new BusinessException("获取文件信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查文件是否存在")
    @GetMapping("/sys/file/exists/{filename:.+}")
    public Result<Boolean> exists(@PathVariable String filename) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();

            if (storageService instanceof LocalStorageService localStorageService) {
                boolean exists = localStorageService.exists(filename);
                return Result.success(exists);
            } else {
                // 对于其他存储服务，尝试下载来检查是否存在
                try {
                    storageService.download(filename);
                    return Result.success(true);
                } catch (Exception e) {
                    return Result.success(false);
                }
            }
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", filename, e);
            throw new BusinessException("检查文件存在性失败: " + e.getMessage());
        }
    }
}
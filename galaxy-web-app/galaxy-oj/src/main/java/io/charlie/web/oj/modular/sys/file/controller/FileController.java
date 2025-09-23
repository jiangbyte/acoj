package io.charlie.web.oj.modular.sys.file.controller;

import io.charlie.web.oj.modular.sys.file.service.FileStorageService;
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

import java.io.FileNotFoundException;
import java.io.IOException;

@Tag(name = "文件前端控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class FileController {
    private final FileStorageService storageService;

    @Operation(summary = "文件上传")
    @PostMapping("/sys/file/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileInfo fileInfo = storageService.store(file);
            return Result.success(fileInfo);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败" + e.getMessage());
        }
    }

    @Operation(summary = "文件预览或下载")
    @GetMapping("/sys/file/{filename:.+}")
    public ResponseEntity<?> serveFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            Resource file = storageService.loadAsResource(filename);

            // 如果是可预览的文件类型，直接返回文件内容
            if (storageService.isPreviewable(filename)) {
                String contentType = request.getServletContext().getMimeType(file.getFilename());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            }
            // 否则作为附件下载
            else {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            }
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/sys/file/info/{filename:.+}")
    public Result<?> getFileInfo(@PathVariable String filename) {
        try {
            Resource file = storageService.loadAsResource(filename);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(filename);
            fileInfo.setOriginalFilename(filename); // 这里可以改进，实际项目中应该保存原始文件名
            fileInfo.setSize(file.contentLength());
            fileInfo.setPreviewable(storageService.isPreviewable(filename));
            fileInfo.setUrl(storageService.getFileUrl(filename));

            return Result.success(fileInfo);
        } catch (IOException e) {
            throw new BusinessException("文件未找到" + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/sys/file/delete/{filename:.+}")
    public Result<?> deleteFile(@PathVariable String filename) {
        try {
            storageService.delete(filename);
            return Result.success();
        } catch (IOException e) {
            throw new BusinessException("文件删除失败" + e.getMessage());
        }
    }
}
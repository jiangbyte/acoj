package github.jiangbyte.io.sys.modules.file.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import github.jiangbyte.io.sys.modules.file.param.SysFileObjectNameParam;
import github.jiangbyte.io.sys.modules.file.result.SysFileUrlResult;
import github.jiangbyte.io.sys.modules.file.service.FileService;
import github.jiangbyte.io.sys.modules.file.support.ContentDispositions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 门户端文件 API：上传与访问 URL（仅本人上传的文件）。
 *
 * Author: Charlie
 */
@Tag(name = "门户端文件 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalFileController {

    private final FileService fileService;

    /** 上传文件。 */
    @Operation(summary = "上传文件。")
    @PostMapping("/v1/portal/sys/file/upload")
    public ApiResponse<SysFile> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storage_provider", required = false) String storageProvider) {
        return ApiResponse.ok(fileService.upload(file, storageProvider));
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/portal/sys/file/detail")
    public ApiResponse<SysFile> detail(@Valid @ModelAttribute IdParam param) {
        SysFile file = fileService.detail(param.getId());
        fileService.assertOwnedByCurrent(file);
        return ApiResponse.ok(file);
    }

    /** 按 ID 列表查询（仅返回本人文件）。 */
    @Operation(summary = "按 ID 列表查询（仅返回本人文件）。")
    @PostMapping("/v1/portal/sys/file/list_by_ids")
    public ApiResponse<List<SysFile>> listByIds(@Valid @RequestBody IdsParam param) {
        List<SysFile> files = fileService.listByIds(param.getIds());
        files.forEach(fileService::assertOwnedByCurrent);
        return ApiResponse.ok(files);
    }

    /** 下载文件。 */
    @Operation(summary = "下载文件。")
    @GetMapping("/v1/portal/sys/file/download")
    public ResponseEntity<Resource> download(@Valid @ModelAttribute IdParam param) {
        SysFile meta = fileService.detail(param.getId());
        fileService.assertOwnedByCurrent(meta);
        Resource resource = fileService.download(param.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDispositions.attachment(meta.getOriginalName()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /** 获取文件访问 URL。 */
    @Operation(summary = "获取文件访问 URL。")
    @PostMapping("/v1/portal/sys/file/url")
    public ApiResponse<SysFileUrlResult> url(@Valid @RequestBody SysFileObjectNameParam param) {
        SysFile file = fileService.listByObjectNames(List.of(param.getObjectName())).stream()
                .findFirst()
                .orElse(null);
        fileService.assertOwnedByCurrent(file);
        return ApiResponse.ok(fileService.url(param.getObjectName()));
    }

    /** 获取预签名 URL。 */
    @Operation(summary = "获取预签名 URL。")
    @PostMapping("/v1/portal/sys/file/presigned_url")
    public ApiResponse<SysFileUrlResult> presignedUrl(@Valid @RequestBody SysFileObjectNameParam param) {
        SysFile file = fileService.listByObjectNames(List.of(param.getObjectName())).stream()
                .findFirst()
                .orElse(null);
        fileService.assertOwnedByCurrent(file);
        return ApiResponse.ok(fileService.presignedUrl(param.getObjectName()));
    }
}

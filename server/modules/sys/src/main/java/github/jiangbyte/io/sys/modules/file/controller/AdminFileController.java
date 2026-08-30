package github.jiangbyte.io.sys.modules.file.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import github.jiangbyte.io.sys.modules.file.param.SysFileEditParam;
import github.jiangbyte.io.sys.modules.file.param.SysFileObjectNameParam;
import github.jiangbyte.io.sys.modules.file.param.SysFilePageParam;
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
 * 管理端文件 API：上传、分页、编辑与访问 URL。
 *
 * Author: Charlie
 */
@Tag(name = "管理端文件 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileService fileService;

    /** 上传文件。 */
    @Operation(summary = "上传文件。")
    @PostMapping("/v1/admin/sys/file/upload")
    @SaCheckPermission(value = "sys:file:upload", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysFile> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storage_provider", required = false) String storageProvider) {
        return ApiResponse.ok(fileService.upload(file, storageProvider));
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/file/delete")
    @SaCheckPermission(value = "sys:file:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_file", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        fileService.delete(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/file/update")
    @SaCheckPermission(value = "sys:file:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_file", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysFileEditParam param) {
        fileService.update(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/file/detail")
    @SaCheckPermission(value = "sys:file:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysFile> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(fileService.detail(param.getId()));
    }

    /** 按 ID 列表查询。 */
    @Operation(summary = "按 ID 列表查询。")
    @PostMapping("/v1/admin/sys/file/list_by_ids")
    @SaCheckPermission(value = "sys:file:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysFile>> listByIds(@Valid @RequestBody IdsParam param) {
        return ApiResponse.ok(fileService.listByIds(param.getIds()));
    }

    /** 下载生成代码。 */
    @Operation(summary = "下载生成代码。")
    @GetMapping("/v1/admin/sys/file/download")
    @SaCheckPermission(value = "sys:file:url", type = StpKit.TYPE_ADMIN)
    public ResponseEntity<Resource> download(@Valid @ModelAttribute IdParam param) {
        SysFile meta = fileService.detail(param.getId());
        Resource resource = fileService.download(param.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDispositions.attachment(meta.getOriginalName()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /** 获取文件访问 URL。 */
    @Operation(summary = "获取文件访问 URL。")
    @PostMapping("/v1/admin/sys/file/url")
    @SaCheckPermission(value = "sys:file:url", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysFileUrlResult> url(@Valid @RequestBody SysFileObjectNameParam param) {
        return ApiResponse.ok(fileService.url(param.getObjectName()));
    }

    /** 获取预签名 URL。 */
    @Operation(summary = "获取预签名 URL。")
    @PostMapping("/v1/admin/sys/file/presigned_url")
    @SaCheckPermission(value = "sys:file:presignedurl", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysFileUrlResult> presignedUrl(@Valid @RequestBody SysFileObjectNameParam param) {
        return ApiResponse.ok(fileService.presignedUrl(param.getObjectName()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/file/page")
    @SaCheckPermission(value = "sys:file:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysFile>> page(@Valid @ModelAttribute SysFilePageParam param) {
        return ApiResponse.ok(fileService.page(param));
    }
}

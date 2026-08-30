package github.jiangbyte.io.sys.modules.codegen.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenField;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenPlan;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenFieldsUpdateBatchParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenPlanPageParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenPlanSaveParam;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenDatabaseColumnResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenDatabaseTableResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenPreviewResult;
import github.jiangbyte.io.sys.modules.codegen.service.CodegenService;
import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 管理端代码生成 API：方案、字段、预览与下载。
 *
 * Author: Charlie
 */
@Tag(name = "管理端代码生成 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "hei.codegen", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AdminCodegenController {

    private final CodegenService codegenService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/sys/codegen/create")
    @SaCheckPermission(value = "sys:codegen:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_codegen", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysCodegenPlanSaveParam param) {
        codegenService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/codegen/update")
    @SaCheckPermission(value = "sys:codegen:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_codegen", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysCodegenPlanSaveParam param) {
        codegenService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/codegen/delete")
    @SaCheckPermission(value = "sys:codegen:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_codegen", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        codegenService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/codegen/detail")
    @SaCheckPermission(value = "sys:codegen:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysCodegenPlan> detail(@RequestParam String id) {
        return ApiResponse.ok(codegenService.detail(id));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/codegen/page")
    @SaCheckPermission(value = "sys:codegen:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysCodegenPlan>> page(@Valid @ModelAttribute SysCodegenPlanPageParam param) {
        return ApiResponse.ok(codegenService.page(param));
    }

    /** 查询数据库表。 */
    @Operation(summary = "查询数据库表。")
    @GetMapping("/v1/admin/sys/codegen/tables")
    @SaCheckPermission(value = "sys:codegen:tables", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysCodegenDatabaseTableResult>> tables() {
        return ApiResponse.ok(codegenService.tables());
    }

    /** 查询表列元数据。 */
    @Operation(summary = "查询表列元数据。")
    @GetMapping("/v1/admin/sys/codegen/table-columns")
    @SaCheckPermission(value = "sys:codegen:tables", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysCodegenDatabaseColumnResult>> tableColumns(@RequestParam("table_name") String tableName) {
        return ApiResponse.ok(codegenService.tableColumns(tableName));
    }

    /** 查询字段配置。 */
    @Operation(summary = "查询字段配置。")
    @GetMapping("/v1/admin/sys/codegen/fields")
    @SaCheckPermission(value = "sys:codegen:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysCodegenField>> fields(
            @RequestParam("plan_id") String planId,
            @RequestParam(value = "table_role", required = false) String tableRole) {
        return ApiResponse.ok(codegenService.fields(planId, tableRole));
    }

    /** 批量更新字段配置。 */
    @Operation(summary = "批量更新字段配置。")
    @PostMapping("/v1/admin/sys/codegen/fields/update-batch")
    @SaCheckPermission(value = "sys:codegen:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_codegen", action = "update")
    public ApiResponse<Void> updateFieldsBatch(@Valid @RequestBody SysCodegenFieldsUpdateBatchParam param) {
        codegenService.updateFieldsBatch(param);
        return ApiResponse.ok();
    }

    /** 查询可选父级资源树。 */
    @Operation(summary = "查询可选父级资源树。")
    @GetMapping("/v1/admin/sys/codegen/parent-resources")
    @SaCheckPermission(value = "sys:codegen:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<Tree<String>>> parentResources(
            @RequestParam(value = "module_id", required = false) String moduleId) {
        return ApiResponse.ok(codegenService.parentResources(moduleId));
    }

    /** 预览生成代码。 */
    @Operation(summary = "预览生成代码。")
    @GetMapping("/v1/admin/sys/codegen/preview")
    @SaCheckPermission(value = "sys:codegen:preview", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysCodegenPreviewResult> preview(@RequestParam String id) {
        return ApiResponse.ok(codegenService.preview(id));
    }

    /** 下载生成代码。 */
    @Operation(summary = "下载生成代码。")
    @GetMapping("/v1/admin/sys/codegen/download")
    @SaCheckPermission(value = "sys:codegen:download", type = StpKit.TYPE_ADMIN)
    public ResponseEntity<byte[]> download(@RequestParam String id) {
        byte[] content = codegenService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codegen-" + id + ".zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(content);
    }
}

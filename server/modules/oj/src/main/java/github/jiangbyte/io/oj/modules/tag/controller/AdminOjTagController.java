package github.jiangbyte.io.oj.modules.tag.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.tag.entity.OjTag;
import github.jiangbyte.io.oj.modules.tag.param.OjTagAddParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagEditParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagPageParam;
import github.jiangbyte.io.oj.modules.tag.service.OjTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 OJ 标签 API：CRUD。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 标签 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjTagController {

    private final OjTagService ojTagService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/oj/tags/create")
    @SaCheckPermission(value = "oj:tag:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_tag", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody OjTagAddParam param) {
        ojTagService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/oj/tags/update")
    @SaCheckPermission(value = "oj:tag:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_tag", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody OjTagEditParam param) {
        ojTagService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/oj/tags/delete")
    @SaCheckPermission(value = "oj:tag:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_tag", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        ojTagService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/oj/tags/detail")
    @SaCheckPermission(value = "oj:tag:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjTag> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(ojTagService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/tags/page")
    @SaCheckPermission(value = "oj:tag:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjTag>> page(@Valid @ModelAttribute OjTagPageParam param) {
        return ApiResponse.ok(ojTagService.page(param));
    }

    /** 启用中的标签选项（题目打标用）。 */
    @Operation(summary = "启用中的标签选项。")
    @GetMapping("/v1/admin/oj/tags/options")
    @SaCheckPermission(value = "oj:problem:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<OjTag>> options() {
        return ApiResponse.ok(ojTagService.listEnabled());
    }
}

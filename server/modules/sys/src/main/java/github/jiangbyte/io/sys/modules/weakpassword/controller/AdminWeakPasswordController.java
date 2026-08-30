package github.jiangbyte.io.sys.modules.weakpassword.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.weakpassword.entity.SysWeakPassword;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordAddParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordEditParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordPageParam;
import github.jiangbyte.io.sys.modules.weakpassword.service.WeakPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端弱密码库 API：CRUD。
 *
 * Author: Charlie
 */
@Tag(name = "管理端弱密码库 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminWeakPasswordController {

    private final WeakPasswordService weakPasswordService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/sys/weak-password/create")
    @SaCheckPermission(value = "sys:weakpassword:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_weakpassword", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysWeakPasswordAddParam param) {
        weakPasswordService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/weak-password/update")
    @SaCheckPermission(value = "sys:weakpassword:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_weakpassword", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysWeakPasswordEditParam param) {
        weakPasswordService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/weak-password/delete")
    @SaCheckPermission(value = "sys:weakpassword:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_weakpassword", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        weakPasswordService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/weak-password/detail")
    @SaCheckPermission(value = "sys:weakpassword:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysWeakPassword> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(weakPasswordService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/weak-password/page")
    @SaCheckPermission(value = "sys:weakpassword:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysWeakPassword>> page(@Valid @ModelAttribute SysWeakPasswordPageParam param) {
        return ApiResponse.ok(weakPasswordService.page(param));
    }

    /** 列表查询。 */
    @Operation(summary = "列表查询。")
    @GetMapping("/v1/admin/sys/weak-password/list")
    @SaCheckPermission(value = "sys:weakpassword:list", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysWeakPassword>> list(
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(weakPasswordService.list(password, keyword));
    }
}

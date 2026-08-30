package github.jiangbyte.io.iam.modules.dept.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.satoken.StpKit;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptAddParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptEditParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptPageParam;
import github.jiangbyte.io.iam.modules.dept.service.DeptService;
import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * 管理端部门 API：部门 CRUD、分页与组织树。
 *
 * Author: Charlie
 */
@Tag(name = "管理端部门 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminDeptController {

    private final DeptService deptService;

    /** 创建部门。 */
    @Operation(summary = "创建部门。")
    @PostMapping("/v1/admin/sys/depts/create")
    @SaCheckPermission(value = "iam:dept:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_dept", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysDeptAddParam param) {
        deptService.create(param);
        return ApiResponse.ok();
    }

    /** 更新部门。 */
    @Operation(summary = "更新部门。")
    @PostMapping("/v1/admin/sys/depts/update")
    @SaCheckPermission(value = "iam:dept:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_dept", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysDeptEditParam param) {
        deptService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除部门。 */
    @Operation(summary = "批量删除部门。")
    @PostMapping("/v1/admin/sys/depts/delete")
    @SaCheckPermission(value = "iam:dept:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "iam_dept", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        deptService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询部门详情。 */
    @Operation(summary = "查询部门详情。")
    @GetMapping("/v1/admin/sys/depts/detail")
    @SaCheckPermission(value = "iam:dept:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysDept> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(deptService.detail(param.getId()));
    }

    /** 分页查询部门。 */
    @Operation(summary = "分页查询部门。")
    @GetMapping("/v1/admin/sys/depts/page")
    @SaCheckPermission(value = "iam:dept:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysDept>> page(@Valid @ModelAttribute SysDeptPageParam param) {
        return ApiResponse.ok(deptService.page(param));
    }

    /** 部门组织树。 */
    @Operation(summary = "部门组织树。")
    @GetMapping("/v1/admin/sys/depts/tree")
    @SaCheckPermission(value = "iam:dept:tree", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<Tree<String>>> tree() {
        return ApiResponse.ok(deptService.tree());
    }
}

package github.jiangbyte.io.sys.modules.dict.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.dict.entity.SysDict;
import github.jiangbyte.io.sys.modules.dict.param.SysDictAddParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictEditParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictPageParam;
import github.jiangbyte.io.sys.modules.dict.service.DictService;
import cn.hutool.core.lang.tree.Tree;
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
 * 管理端数据字典 API：CRUD 与树/列表查询。
 *
 * Author: Charlie
 */
@Tag(name = "管理端数据字典 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminDictController {

    private final DictService dictService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/sys/dicts/create")
    @SaCheckPermission(value = "sys:dict:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_dict", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysDictAddParam param) {
        dictService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/dicts/update")
    @SaCheckPermission(value = "sys:dict:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_dict", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysDictEditParam param) {
        dictService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/dicts/delete")
    @SaCheckPermission(value = "sys:dict:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_dict", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        dictService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/dicts/detail")
    @SaCheckPermission(value = "sys:dict:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysDict> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(dictService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/dicts/page")
    @SaCheckPermission(value = "sys:dict:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysDict>> page(@Valid @ModelAttribute SysDictPageParam param) {
        return ApiResponse.ok(dictService.page(param));
    }

    /** 树形查询。 */
    @Operation(summary = "树形查询。")
    @GetMapping("/v1/admin/sys/dicts/tree")
    public ApiResponse<List<Tree<String>>> tree(@RequestParam(required = false) String category) {
        return ApiResponse.ok(dictService.tree(category));
    }
}

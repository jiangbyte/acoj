package github.jiangbyte.io.sys.modules.banner.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerAddParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerEditParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerPageParam;
import github.jiangbyte.io.sys.modules.banner.service.BannerService;
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
 * 管理端 Banner API：CRUD 与状态维护。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 Banner API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    /** 列表查询。 */
    @Operation(summary = "列表查询。")
    @GetMapping("/v1/admin/sys/banners/list")
    public ApiResponse<List<SysBanner>> list(
            @RequestParam String position,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type) {
        return ApiResponse.ok(bannerService.adminList(position, category, type));
    }

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/sys/banners/create")
    @SaCheckPermission(value = "sys:banner:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_banner", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysBannerAddParam param) {
        bannerService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/banners/update")
    @SaCheckPermission(value = "sys:banner:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_banner", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysBannerEditParam param) {
        bannerService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/banners/delete")
    @SaCheckPermission(value = "sys:banner:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_banner", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        bannerService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/banners/detail")
    @SaCheckPermission(value = "sys:banner:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysBanner> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(bannerService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/banners/page")
    @SaCheckPermission(value = "sys:banner:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysBanner>> page(@Valid @ModelAttribute SysBannerPageParam param) {
        return ApiResponse.ok(bannerService.page(param));
    }
}

package io.charlie.web.oj.modular.sys.menu.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.menu.param.SysMenuPageParam;
import io.charlie.web.oj.modular.sys.menu.param.SysMenuAddParam;
import io.charlie.web.oj.modular.sys.menu.param.SysMenuEditParam;
import io.charlie.web.oj.modular.sys.menu.param.SysMenuIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.menu.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-24
* @description 菜单表 控制器
*/
@Tag(name = "菜单表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysMenuController {
    private final SysMenuService sysMenuService;

    @Operation(summary = "获取菜单分页")
    //@SaCheckPermission("/sys/menu/page")
    @GetMapping("/sys/menu/page")
    public Result<?> page(@ParameterObject SysMenuPageParam sysMenuPageParam) {
        return Result.success(sysMenuService.page(sysMenuPageParam));
    }

    @Operation(summary = "添加菜单")
    //@SaCheckPermission("/sys/menu/add")
    @PostMapping("/sys/menu/add")
    public Result<?> add(@RequestBody @Valid SysMenuAddParam sysMenuAddParam) {
        sysMenuService.add(sysMenuAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑菜单")
    //@SaCheckPermission("/sys/menu/edit")
    @PostMapping("/sys/menu/edit")
    public Result<?> edit(@RequestBody @Valid SysMenuEditParam sysMenuEditParam) {
        sysMenuService.edit(sysMenuEditParam);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    //@SaCheckPermission("/sys/menu/delete")
    @PostMapping("/sys/menu/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysMenuIdParam> sysMenuIdParam) {
        sysMenuService.delete(sysMenuIdParam);
        return Result.success();
    }

    @Operation(summary = "获取菜单详情")
    //@SaCheckPermission("/sys/menu/detail")
    @GetMapping("/sys/menu/detail")
    public Result<?> detail(@ParameterObject @Valid SysMenuIdParam sysMenuIdParam) {
        return Result.success(sysMenuService.detail(sysMenuIdParam));
    }

    @Operation(summary = "获取菜单详情")
    //@SaCheckPermission("/sys/menu/detail")
    @GetMapping("/sys/menu/tree/list")
    public Result<?> treeList() {
        return Result.success(sysMenuService.menuTree());
    }

    @Operation(summary = "获取认证菜单")
    //@SaCheckPermission("/sys/menu/detail")
    @GetMapping("/sys/menu/auth/list")
    public Result<?> authMenu() {
        return Result.success(sysMenuService.authMenu());
    }
}
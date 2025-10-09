package io.charlie.web.oj.modular.sys.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.relation.service.SysRoleMenuService;
import io.charlie.web.oj.modular.sys.role.param.SysMenuAssignParam;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.role.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.role.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 角色表 控制器
*/
@Tag(name = "角色表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysRoleController {
    private final SysRoleService sysRoleService;
    private final SysUserRoleService  sysUserRoleService;
    private final SysRoleMenuService sysRoleMenuService;

    @Operation(summary = "获取角色分页")
    @SaCheckPermission("/sys/role/page")
    @GetMapping("/sys/role/page")
    public Result<?> page(@ParameterObject SysRolePageParam sysRolePageParam) {
        return Result.success(sysRoleService.page(sysRolePageParam));
    }

    @Operation(summary = "添加角色")
    @SaCheckPermission("/sys/role/add")
    @PostMapping("/sys/role/add")
    public Result<?> add(@RequestBody @Valid SysRoleAddParam sysRoleAddParam) {
        sysRoleService.add(sysRoleAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑角色")
    @SaCheckPermission("/sys/role/edit")
    @PostMapping("/sys/role/edit")
    public Result<?> edit(@RequestBody @Valid SysRoleEditParam sysRoleEditParam) {
        sysRoleService.edit(sysRoleEditParam);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("/sys/role/delete")
    @PostMapping("/sys/role/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysRoleIdParam> sysRoleIdParam) {
        sysRoleService.delete(sysRoleIdParam);
        return Result.success();
    }

    @Operation(summary = "获取角色详情")
    @SaCheckPermission("/sys/role/detail")
    @GetMapping("/sys/role/detail")
    public Result<?> detail(@ParameterObject @Valid SysRoleIdParam sysRoleIdParam) {
        return Result.success(sysRoleService.detail(sysRoleIdParam));
    }

    @Operation(summary = "获取认证角色列表")
    @GetMapping("/sys/role/auth/list")
    public Result<?> authRoles() {
        return Result.success(sysRoleService.authRoles());
    }

    @Operation(summary = "获取认证角色列表")
    @GetMapping("/sys/role/auth/list1")
    public Result<?> authRoles1() {
        return Result.success(sysRoleService.authRoles1());
    }

    @Operation(summary = "授权角色")
    @PostMapping("/sys/role/assign")
    public Result<?> assign(@RequestBody @Valid SysRoleAssignParam sysRoleAssignParam) {
        sysUserRoleService.assignRoles(sysRoleAssignParam.getUserId(), sysRoleAssignParam.getRoleIds());
        return Result.success();
    }

    @Operation(summary = "获取认证菜单")
    @PostMapping("/sys/role/menu/assign")
    public Result<?> assignMenu(@RequestBody @Valid SysMenuAssignParam sysMenuPermissionParam) {
        sysRoleMenuService.assignMenus(sysMenuPermissionParam.getRoleId(), sysMenuPermissionParam.getMenuIds());
        return Result.success();
    }
}
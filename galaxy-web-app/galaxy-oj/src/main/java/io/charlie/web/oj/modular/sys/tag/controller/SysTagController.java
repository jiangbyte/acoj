package io.charlie.web.oj.modular.sys.tag.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.tag.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.tag.service.SysTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 标签表 控制器
*/
@Tag(name = "标签表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysTagController {
    private final SysTagService sysTagService;

    @Operation(summary = "获取标签分页")
    @SaCheckPermission("/sys/tag/page")
    @GetMapping("/sys/tag/page")
    public Result<?> page(@ParameterObject SysTagPageParam sysTagPageParam) {
        return Result.success(sysTagService.page(sysTagPageParam));
    }

    @Operation(summary = "添加标签")
    @SaCheckPermission("/sys/tag/add")
    @PostMapping("/sys/tag/add")
    public Result<?> add(@RequestBody @Valid SysTagAddParam sysTagAddParam) {
        sysTagService.add(sysTagAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑标签")
    @SaCheckPermission("/sys/tag/edit")
    @PostMapping("/sys/tag/edit")
    public Result<?> edit(@RequestBody @Valid SysTagEditParam sysTagEditParam) {
        sysTagService.edit(sysTagEditParam);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @SaCheckPermission("/sys/tag/delete")
    @PostMapping("/sys/tag/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysTagIdParam> sysTagIdParam) {
        sysTagService.delete(sysTagIdParam);
        return Result.success();
    }

    @Operation(summary = "获取标签详情")
    @SaCheckPermission("/sys/tag/detail")
    @GetMapping("/sys/tag/detail")
    public Result<?> detail(@ParameterObject @Valid SysTagIdParam sysTagIdParam) {
        return Result.success(sysTagService.detail(sysTagIdParam));
    }

    @Operation(summary = "标签选项列表")
    @GetMapping("/sys/tag/options")
    public Result<?> options(@ParameterObject SysTagOptionParam sysTagOptionParam) {
        return Result.success(sysTagService.options(sysTagOptionParam));
    }
}
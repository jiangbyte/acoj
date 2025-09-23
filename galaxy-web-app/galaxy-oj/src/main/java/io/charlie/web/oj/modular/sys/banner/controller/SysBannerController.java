package io.charlie.web.oj.modular.sys.banner.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.banner.param.SysBannerPageParam;
import io.charlie.web.oj.modular.sys.banner.param.SysBannerAddParam;
import io.charlie.web.oj.modular.sys.banner.param.SysBannerEditParam;
import io.charlie.web.oj.modular.sys.banner.param.SysBannerIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.banner.service.SysBannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 横幅表 控制器
*/
@Tag(name = "横幅表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysBannerController {
    private final SysBannerService sysBannerService;

    @Operation(summary = "获取横幅分页")
    //@SaCheckPermission("/sys/banner/page")
    @GetMapping("/sys/banner/page")
    public Result<?> page(@ParameterObject SysBannerPageParam sysBannerPageParam) {
        return Result.success(sysBannerService.page(sysBannerPageParam));
    }

    @Operation(summary = "添加横幅")
    //@SaCheckPermission("/sys/banner/add")
    @PostMapping("/sys/banner/add")
    public Result<?> add(@RequestBody @Valid SysBannerAddParam sysBannerAddParam) {
        sysBannerService.add(sysBannerAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑横幅")
    //@SaCheckPermission("/sys/banner/edit")
    @PostMapping("/sys/banner/edit")
    public Result<?> edit(@RequestBody @Valid SysBannerEditParam sysBannerEditParam) {
        sysBannerService.edit(sysBannerEditParam);
        return Result.success();
    }

    @Operation(summary = "删除横幅")
    //@SaCheckPermission("/sys/banner/delete")
    @PostMapping("/sys/banner/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysBannerIdParam> sysBannerIdParam) {
        sysBannerService.delete(sysBannerIdParam);
        return Result.success();
    }

    @Operation(summary = "获取横幅详情")
    //@SaCheckPermission("/sys/banner/detail")
    @GetMapping("/sys/banner/detail")
    public Result<?> detail(@ParameterObject @Valid SysBannerIdParam sysBannerIdParam) {
        return Result.success(sysBannerService.detail(sysBannerIdParam));
    }

    @Operation(summary = "获取最新横幅列表")
    @GetMapping("/sys/banner/latest")
    public Result<?> latest() {
        return Result.success(sysBannerService.latestN(10));
    }

}
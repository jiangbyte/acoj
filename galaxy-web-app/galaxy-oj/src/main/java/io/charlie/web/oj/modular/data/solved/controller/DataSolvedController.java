package io.charlie.web.oj.modular.data.solved.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedPageParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedAddParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedEditParam;
import io.charlie.web.oj.modular.data.solved.param.DataSolvedIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.solved.service.DataSolvedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 用户解决表 控制器
*/
@Tag(name = "用户解决表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataSolvedController {
    private final DataSolvedService dataSolvedService;

    @Operation(summary = "获取用户解决分页")
    @SaCheckPermission("/data/solved/page")
    @GetMapping("/data/solved/page")
    public Result<?> page(@ParameterObject DataSolvedPageParam dataSolvedPageParam) {
        return Result.success(dataSolvedService.page(dataSolvedPageParam));
    }

    @Operation(summary = "添加用户解决")
    @SaCheckPermission("/data/solved/add")
    @PostMapping("/data/solved/add")
    public Result<?> add(@RequestBody @Valid DataSolvedAddParam dataSolvedAddParam) {
        dataSolvedService.add(dataSolvedAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑用户解决")
    @SaCheckPermission("/data/solved/edit")
    @PostMapping("/data/solved/edit")
    public Result<?> edit(@RequestBody @Valid DataSolvedEditParam dataSolvedEditParam) {
        dataSolvedService.edit(dataSolvedEditParam);
        return Result.success();
    }

    @Operation(summary = "删除用户解决")
    @SaCheckPermission("/data/solved/delete")
    @PostMapping("/data/solved/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataSolvedIdParam> dataSolvedIdParam) {
        dataSolvedService.delete(dataSolvedIdParam);
        return Result.success();
    }

    @Operation(summary = "获取用户解决详情")
    @SaCheckPermission("/data/solved/detail")
    @GetMapping("/data/solved/detail")
    public Result<?> detail(@ParameterObject @Valid DataSolvedIdParam dataSolvedIdParam) {
        return Result.success(dataSolvedService.detail(dataSolvedIdParam));
    }
}
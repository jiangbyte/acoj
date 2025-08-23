package io.charlie.app.core.modular.problem.submit.controller;

import io.charlie.app.core.modular.problem.submit.param.*;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.app.core.modular.problem.submit.service.ProSubmitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 提交表 控制器
 */
@Tag(name = "提交表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSubmitController {
    private final ProSubmitService proSubmitService;

    @Operation(summary = "获取提交分页")
    //@SaCheckPermission("/pro/submit/page")
    @GetMapping("/pro/submit/page")
    public Result<?> page(@ParameterObject ProSubmitPageParam proSubmitPageParam) {
        return Result.success(proSubmitService.page(proSubmitPageParam));
    }

    @Operation(summary = "添加提交")
    //@SaCheckPermission("/pro/submit/add")
    @PostMapping("/pro/submit/add")
    public Result<?> add(@RequestBody @Valid ProSubmitAddParam proSubmitAddParam) {
        proSubmitService.add(proSubmitAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑提交")
    //@SaCheckPermission("/pro/submit/edit")
    @PostMapping("/pro/submit/edit")
    public Result<?> edit(@RequestBody @Valid ProSubmitEditParam proSubmitEditParam) {
        proSubmitService.edit(proSubmitEditParam);
        return Result.success();
    }

    @Operation(summary = "删除提交")
    //@SaCheckPermission("/pro/submit/delete")
    @PostMapping("/pro/submit/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSubmitIdParam> proSubmitIdParam) {
        proSubmitService.delete(proSubmitIdParam);
        return Result.success();
    }

    @Operation(summary = "获取提交详情")
    //@SaCheckPermission("/pro/submit/detail")
    @GetMapping("/pro/submit/detail")
    public Result<?> detail(@ParameterObject @Valid ProSubmitIdParam proSubmitIdParam) {
        return Result.success(proSubmitService.detail(proSubmitIdParam));
    }

    @Operation(summary = "执行提交")
    //@SaCheckPermission("/pro/submit/execute")
    @PostMapping("/pro/submit/execute")
    public Result<?> execute(@RequestBody @Valid ProSubmitExecuteParam proSubmitExecuteParam) {
        return Result.success(proSubmitService.execute(proSubmitExecuteParam));
    }

    @Operation(summary = "获取用户提交分页")
    //@SaCheckPermission("/pro/submit/page")
    @GetMapping("/pro/submit/page/user")
    public Result<?> problemUserSubmitPage(@ParameterObject ProUserSubmitPageParam proUserSubmitPageParam) {
        return Result.success(proSubmitService.problemUserSubmitPage(proUserSubmitPageParam));
    }

    @Operation(summary = "获取状态统计")
    //@SaCheckPermission("/pro/submit/page")
    @GetMapping("/pro/submit/status/count")
    public Result<?> problemUserSubmitStatusCount() {
        return Result.success(proSubmitService.countStatusStatistics());
    }
}
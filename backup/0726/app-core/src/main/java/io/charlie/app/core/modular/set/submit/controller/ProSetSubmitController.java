package io.charlie.app.core.modular.set.submit.controller;

import io.charlie.app.core.modular.set.submit.param.*;
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
import io.charlie.app.core.modular.set.submit.service.ProSetSubmitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题单提交表 控制器
*/
@Tag(name = "题单提交表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetSubmitController {
    private final ProSetSubmitService proSetSubmitService;

    @Operation(summary = "获取题单提交分页")
    //@SaCheckPermission("/pro/set/submit/page")
    @GetMapping("/pro/set/submit/page")
    public Result<?> page(@ParameterObject ProSetSubmitPageParam proSetSubmitPageParam) {
        return Result.success(proSetSubmitService.page(proSetSubmitPageParam));
    }

    @Operation(summary = "添加题单提交")
    //@SaCheckPermission("/pro/set/submit/add")
    @PostMapping("/pro/set/submit/add")
    public Result<?> add(@RequestBody @Valid ProSetSubmitAddParam proSetSubmitAddParam) {
        proSetSubmitService.add(proSetSubmitAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题单提交")
    //@SaCheckPermission("/pro/set/submit/edit")
    @PostMapping("/pro/set/submit/edit")
    public Result<?> edit(@RequestBody @Valid ProSetSubmitEditParam proSetSubmitEditParam) {
        proSetSubmitService.edit(proSetSubmitEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题单提交")
    //@SaCheckPermission("/pro/set/submit/delete")
    @PostMapping("/pro/set/submit/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<ProSetSubmitIdParam> proSetSubmitIdParam) {
        proSetSubmitService.delete(proSetSubmitIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题单提交详情")
    //@SaCheckPermission("/pro/set/submit/detail")
    @GetMapping("/pro/set/submit/detail")
    public Result<?> detail(@ParameterObject @Valid ProSetSubmitIdParam proSetSubmitIdParam) {
        return Result.success(proSetSubmitService.detail(proSetSubmitIdParam));
    }
    
    @Operation(summary = "执行题单提交")
    //@SaCheckPermission("/pro/set/submit/execute")
    @PostMapping("/pro/set/submit/execute")
    public Result<?> execute(@RequestBody @Valid ProSetSubmitExecuteParam proSetSubmitExecuteParam) {
        return Result.success(proSetSubmitService.execute(proSetSubmitExecuteParam));
    }
}
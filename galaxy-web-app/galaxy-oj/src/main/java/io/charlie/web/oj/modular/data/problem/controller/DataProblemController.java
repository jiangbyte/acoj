package io.charlie.web.oj.modular.data.problem.controller;

import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.problem.param.DataProblemPageParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemAddParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemEditParam;
import io.charlie.web.oj.modular.data.problem.param.DataProblemIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.problem.service.DataProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题目表 控制器
*/
@Tag(name = "题目表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataProblemController {
    private final DataProblemService dataProblemService;

    @Operation(summary = "获取题目分页")
    //@SaCheckPermission("/data/problem/page")
    @GetMapping("/data/problem/page")
    public Result<?> page(@ParameterObject DataProblemPageParam dataProblemPageParam) {
        return Result.success(dataProblemService.page(dataProblemPageParam));
    }

    @Operation(summary = "添加题目")
    //@SaCheckPermission("/data/problem/add")
    @PostMapping("/data/problem/add")
    public Result<?> add(@RequestBody @Valid DataProblemAddParam dataProblemAddParam) {
        dataProblemService.add(dataProblemAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑题目")
    //@SaCheckPermission("/data/problem/edit")
    @PostMapping("/data/problem/edit")
    public Result<?> edit(@RequestBody @Valid DataProblemEditParam dataProblemEditParam) {
        dataProblemService.edit(dataProblemEditParam);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    //@SaCheckPermission("/data/problem/delete")
    @PostMapping("/data/problem/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataProblemIdParam> dataProblemIdParam) {
        dataProblemService.delete(dataProblemIdParam);
        return Result.success();
    }

    @Operation(summary = "获取题目详情")
    //@SaCheckPermission("/data/problem/detail")
    @GetMapping("/data/problem/detail")
    public Result<?> detail(@ParameterObject @Valid DataProblemIdParam dataProblemIdParam) {
        return Result.success(dataProblemService.detail(dataProblemIdParam));
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/data/problem/client/detail")
    public Result<?> clientDetail(@ParameterObject @Valid DataProblemIdParam dataProblemIdParam) {
        return Result.success(dataProblemService.detail(dataProblemIdParam));
    }

    @Operation(summary = "C端-获取最新10道题目")
    @GetMapping("/data/problem/latest")
    public Result<?> latest10() {
        return Result.success(dataProblemService.latestN(4));
    }

    @Operation(summary = "获取题目统计")
    @GetMapping("/data/problem/count")
    public Result<?> getProblemCount() {
        return Result.success(dataProblemService.getProblemCount());
    }

    @Operation(summary = "C端-获取最热10道题目")
    @GetMapping("/data/problem/hot")
    public Result<?> getHot() {
        return Result.success(dataProblemService.getHotN(10));
    }

    @Operation(summary = "C端-难度分布")
    @GetMapping("/data/problem/difficulty/distribution")
    public Result<?> difficultyDistribution() {
        return Result.success(dataProblemService.difficultyDistribution());
    }

    @Operation(summary = "导入题目")
    @PostMapping("/data/problem/import")
    public Result<?> importProblems(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.failure("请选择要上传的文件");
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
                return Result.failure("请选择有效的ZIP格式的压缩包");
            }
            dataProblemService.importProblems(file);
            return Result.success("问题导入成功");
        } catch (Exception e) {
            log.error("导入失败", e);
            return Result.failure("导入失败: " + e.getMessage());
        }
    }
}
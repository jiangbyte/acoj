package io.charlie.web.oj.modular.data.contest.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.contest.param.DataContestPageParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.contest.service.DataContestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛表 控制器
*/
@Tag(name = "竞赛表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataContestController {
    private final DataContestService dataContestService;

    @Operation(summary = "获取竞赛分页")
    @SaCheckPermission("/data/contest/page")
    @GetMapping("/data/contest/page")
    public Result<?> page(@ParameterObject DataContestPageParam dataContestPageParam) {
        return Result.success(dataContestService.page(dataContestPageParam));
    }

    @Operation(summary = "获取竞赛分页")
    @GetMapping("/data/contest/page/client")
    public Result<?> pageClient(@ParameterObject DataContestPageParam dataContestPageParam) {
        return Result.success(dataContestService.page(dataContestPageParam));
    }

    @Operation(summary = "添加竞赛")
    @SaCheckPermission("/data/contest/add")
    @PostMapping("/data/contest/add")
    public Result<?> add(@RequestBody @Valid DataContestAddParam dataContestAddParam) {
        dataContestService.add(dataContestAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑竞赛")
    @SaCheckPermission("/data/contest/edit")
    @PostMapping("/data/contest/edit")
    public Result<?> edit(@RequestBody @Valid DataContestEditParam dataContestEditParam) {
        dataContestService.edit(dataContestEditParam);
        return Result.success();
    }

    @Operation(summary = "删除竞赛")
    @SaCheckPermission("/data/contest/delete")
    @PostMapping("/data/contest/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataContestIdParam> dataContestIdParam) {
        dataContestService.delete(dataContestIdParam);
        return Result.success();
    }

    @Operation(summary = "获取竞赛详情")
    @SaCheckPermission("/data/contest/detail")
    @GetMapping("/data/contest/detail")
    public Result<?> detail(@ParameterObject @Valid DataContestIdParam dataContestIdParam) {
        return Result.success(dataContestService.detail(dataContestIdParam));
    }

    @Operation(summary = "C端-获取最热10个题集")
    @GetMapping("/data/contest/hot")
    public Result<?> getHot() {
        return Result.success(dataContestService.getHotN(5));
    }
}
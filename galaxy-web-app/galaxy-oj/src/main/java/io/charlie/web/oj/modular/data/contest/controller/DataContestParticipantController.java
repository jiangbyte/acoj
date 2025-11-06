package io.charlie.web.oj.modular.data.contest.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantPageParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.contest.service.DataContestParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛参与表 控制器
*/
@Tag(name = "竞赛参与表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataContestParticipantController {
    private final DataContestParticipantService dataContestParticipantService;

    @Operation(summary = "获取竞赛参与分页")
    @SaCheckPermission("/data/contest/participant/page")
    @GetMapping("/data/contest/participant/page")
    public Result<?> page(@ParameterObject DataContestParticipantPageParam dataContestParticipantPageParam) {
        return Result.success(dataContestParticipantService.page(dataContestParticipantPageParam));
    }

    @Operation(summary = "添加竞赛参与")
    @SaCheckPermission("/data/contest/participant/add")
    @PostMapping("/data/contest/participant/add")
    public Result<?> add(@RequestBody @Valid DataContestParticipantAddParam dataContestParticipantAddParam) {
        dataContestParticipantService.add(dataContestParticipantAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑竞赛参与")
    @SaCheckPermission("/data/contest/participant/edit")
    @PostMapping("/data/contest/participant/edit")
    public Result<?> edit(@RequestBody @Valid DataContestParticipantEditParam dataContestParticipantEditParam) {
        dataContestParticipantService.edit(dataContestParticipantEditParam);
        return Result.success();
    }

    @Operation(summary = "删除竞赛参与")
    @SaCheckPermission("/data/contest/participant/delete")
    @PostMapping("/data/contest/participant/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataContestParticipantIdParam> dataContestParticipantIdParam) {
        dataContestParticipantService.delete(dataContestParticipantIdParam);
        return Result.success();
    }

    @Operation(summary = "获取竞赛参与详情")
    @SaCheckPermission("/data/contest/participant/detail")
    @GetMapping("/data/contest/participant/detail")
    public Result<?> detail(@ParameterObject @Valid DataContestParticipantIdParam dataContestParticipantIdParam) {
        return Result.success(dataContestParticipantService.detail(dataContestParticipantIdParam));
    }
}
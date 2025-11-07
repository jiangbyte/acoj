package io.charlie.web.oj.modular.data.contest.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.data.contest.param.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.data.contest.service.DataContestAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛认证表 控制器
*/
@Tag(name = "竞赛认证表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class DataContestAuthController {
    private final DataContestAuthService dataContestAuthService;

    @Operation(summary = "获取竞赛认证分页")
    @SaCheckPermission("/data/contest/auth/page")
    @GetMapping("/data/contest/auth/page")
    public Result<?> page(@ParameterObject DataContestAuthPageParam dataContestAuthPageParam) {
        return Result.success(dataContestAuthService.page(dataContestAuthPageParam));
    }

    @Operation(summary = "添加竞赛认证")
    @SaCheckPermission("/data/contest/auth/add")
    @PostMapping("/data/contest/auth/add")
    public Result<?> add(@RequestBody @Valid DataContestAuthAddParam dataContestAuthAddParam) {
        dataContestAuthService.add(dataContestAuthAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑竞赛认证")
    @SaCheckPermission("/data/contest/auth/edit")
    @PostMapping("/data/contest/auth/edit")
    public Result<?> edit(@RequestBody @Valid DataContestAuthEditParam dataContestAuthEditParam) {
        dataContestAuthService.edit(dataContestAuthEditParam);
        return Result.success();
    }

    @Operation(summary = "删除竞赛认证")
    @SaCheckPermission("/data/contest/auth/delete")
    @PostMapping("/data/contest/auth/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<DataContestAuthIdParam> dataContestAuthIdParam) {
        dataContestAuthService.delete(dataContestAuthIdParam);
        return Result.success();
    }

    @Operation(summary = "获取竞赛认证详情")
    @SaCheckPermission("/data/contest/auth/detail")
    @GetMapping("/data/contest/auth/detail")
    public Result<?> detail(@ParameterObject @Valid DataContestAuthIdParam dataContestAuthIdParam) {
        return Result.success(dataContestAuthService.detail(dataContestAuthIdParam));
    }

    @Operation(summary = "竞赛认证")
    @PostMapping("/data/contest/auth/user")
    public Result<?> auth(@RequestBody @Valid DataContestAuthParam dataContestAuthParam) {
        return Result.success(dataContestAuthService.auth(dataContestAuthParam));
    }
}
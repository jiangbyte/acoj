package io.charlie.web.oj.modular.sys.conversation.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.galaxy.result.Result;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationPageParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationAddParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationEditParam;
import io.charlie.web.oj.modular.sys.conversation.param.SysConversationIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.charlie.web.oj.modular.sys.conversation.service.SysConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-10-20
 * @description 大模型对话表 控制器
 */
@Tag(name = "大模型对话表控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class SysConversationController {
    private final SysConversationService sysConversationService;

    @Operation(summary = "获取大模型对话分页")
    @SaCheckPermission("/sys/conversation/page")
    @GetMapping("/sys/conversation/page")
    public Result<?> page(@ParameterObject SysConversationPageParam sysConversationPageParam) {
        return Result.success(sysConversationService.page(sysConversationPageParam));
    }

    @Operation(summary = "添加大模型对话")
    @SaCheckPermission("/sys/conversation/add")
    @PostMapping("/sys/conversation/add")
    public Result<?> add(@RequestBody @Valid SysConversationAddParam sysConversationAddParam) {
        sysConversationService.add(sysConversationAddParam);
        return Result.success();
    }

    @Operation(summary = "编辑大模型对话")
    @SaCheckPermission("/sys/conversation/edit")
    @PostMapping("/sys/conversation/edit")
    public Result<?> edit(@RequestBody @Valid SysConversationEditParam sysConversationEditParam) {
        sysConversationService.edit(sysConversationEditParam);
        return Result.success();
    }

    @Operation(summary = "删除大模型对话")
    @SaCheckPermission("/sys/conversation/delete")
    @PostMapping("/sys/conversation/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<SysConversationIdParam> sysConversationIdParam) {
        sysConversationService.delete(sysConversationIdParam);
        return Result.success();
    }

    @Operation(summary = "获取大模型对话详情")
    @SaCheckPermission("/sys/conversation/detail")
    @GetMapping("/sys/conversation/detail")
    public Result<?> detail(@ParameterObject @Valid SysConversationIdParam sysConversationIdParam) {
        return Result.success(sysConversationService.detail(sysConversationIdParam));
    }

    @Operation(summary = "获取用户历史对话")
    @GetMapping("/sys/conversation/userHistory")
    public Result<?> userHistory(@ParameterObject @Valid SysConversationPageParam sysConversationPageParam) {
        return Result.success(sysConversationService.userHistorypage(sysConversationPageParam));
    }

    @Operation(summary = "获取对话ID历史")
    @GetMapping("/sys/conversation/history/{conversationId}")
    public Result<?> history(@PathVariable @NotBlank(message = "对话ID不能为空") String conversationId) {
        return Result.success(sysConversationService.historyByConversationId(conversationId));
    }
}
package io.charlie.web.modular.task.trace.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.charlie.cores.result.Result;
import io.charlie.web.modular.task.trace.param.WorkflowTraceQueryParam;
import io.charlie.web.modular.task.trace.service.WorkflowTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跨工作流追溯控制器。
 */
@Tag(name = "跨工作流追溯")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class WorkflowTraceController {

    private final WorkflowTraceService workflowTraceService;

    @Operation(summary = "按题目追溯提交/Token/克隆/会话")
    @SaCheckPermission("/task/reports/page")
    @GetMapping("/task/trace/by-problem")
    public Result<?> byProblem(@ParameterObject @Valid WorkflowTraceQueryParam param) {
        return Result.success(workflowTraceService.byProblem(param));
    }

    @Operation(summary = "按用户追溯提交/Token/克隆/会话")
    @SaCheckPermission("/task/reports/page")
    @GetMapping("/task/trace/by-user")
    public Result<?> byUser(@ParameterObject @Valid WorkflowTraceQueryParam param) {
        return Result.success(workflowTraceService.byUser(param));
    }
}

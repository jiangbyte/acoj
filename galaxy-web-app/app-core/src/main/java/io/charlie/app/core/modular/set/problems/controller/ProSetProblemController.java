package io.charlie.app.core.modular.set.problems.controller;

import io.charlie.app.core.modular.problem.problem.param.SetProblemPageParam;
import io.charlie.app.core.modular.set.problems.service.ProSetProblemService;
import io.charlie.app.core.modular.set.set.param.ProSetPageParam;
import io.charlie.app.core.modular.set.set.service.ProSetService;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description TODO
 */
@Tag(name = "题集控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ProSetProblemController {
    private final ProSetProblemService proSetProblemService;

    @Operation(summary = "获取题集题目分页")
    //@SaCheckPermission("/pro/set/problems/page")
    @GetMapping("/pro/set/problems/page")
    public Result<?> page(@ParameterObject SetProblemPageParam setProblemPageParam) {
        return Result.success(proSetProblemService.setProblemPage(setProblemPageParam));
    }
}

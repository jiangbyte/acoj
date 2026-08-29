package io.charlie.web.modular.sys.analyse.controller;

import io.charlie.cores.result.Result;
import io.charlie.web.modular.sys.analyse.service.TodayTotalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 17/10/2025
 */
@Tag(name = "今日统计控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class TodayTotalController {
    private final TodayTotalService todayTotalService;

    @GetMapping("/analyse/today/total")
    public Result<?> todayTotal() {
        return Result.success(todayTotalService.getTodayTotal());
    }
}

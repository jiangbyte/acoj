package io.charlie.app.core.modular.monitor.controller;

import io.charlie.app.core.modular.monitor.service.HealthService;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 22/05/2025
 * @description 服务状态控制器
 */
@Slf4j
@Tag(name = "服务状态控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HealthController {
    private final HealthService healthService;

    @Operation(summary = "获取服务健康状态")
    @GetMapping("/sys/monitor/health/status")
    public Result<?> getServicesHealth() {
        return Result.success(healthService.getServicesHealth());
    }
}

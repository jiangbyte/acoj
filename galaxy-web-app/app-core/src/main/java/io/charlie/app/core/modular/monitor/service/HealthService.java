package io.charlie.app.core.modular.monitor.service;

import io.charlie.app.core.modular.monitor.result.ServiceDetailStatus;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 29/05/2025
 * @description 服务健康检查
 */
public interface HealthService {
    List<ServiceDetailStatus> getServicesHealth();
}

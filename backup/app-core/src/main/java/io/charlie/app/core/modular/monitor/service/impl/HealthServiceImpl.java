package io.charlie.app.core.modular.monitor.service.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import io.charlie.app.core.modular.monitor.result.ServiceDetailStatus;
import io.charlie.app.core.modular.monitor.service.HealthService;
import io.charlie.galaxy.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 24/07/2025
 * @description 监控服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {
    private final NamingService namingService;

    // 缓存最新的健康状态
    private final AtomicReference<List<ServiceDetailStatus>> cachedHealthStatus =
            new AtomicReference<>(new ArrayList<>());

    // 健康检查配置
    private static final long HEALTH_CHECK_TIMEOUT = 5000;
    private static final long HEARTBEAT_TIMEOUT = 30000;
    private static final long CACHE_REFRESH_INTERVAL = 10000; // 10秒刷新一次

    /**
     * 启动时初始化定时任务
     */
    @PostConstruct
    public void init() {
        // 立即加载一次数据
        refreshHealthStatus();
        // 定时刷新
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                this::refreshHealthStatus,
                0,
                CACHE_REFRESH_INTERVAL,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 对外接口：直接返回缓存数据
     */
    @Override
    public List<ServiceDetailStatus> getServicesHealth() {
        return cachedHealthStatus.get();
    }

    /**
     * 定时刷新健康状态
     */
    private void refreshHealthStatus() {
        log.info("开始执行定时健康检测...");
        long startTime = System.currentTimeMillis();
        try {
            List<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE).getData();
            log.debug("从Nacos获取到 {} 个服务", services.size());

            List<ServiceDetailStatus> latestStatus = services.stream()
                    .parallel()
                    .map(this::checkServiceHealth)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            cachedHealthStatus.set(latestStatus);
            log.info("定时检测完成，更新缓存状态。服务数: {}, 耗时: {}ms",  // 关键日志
                    latestStatus.size(),
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("刷新健康状态失败", e);
        }
    }

    private ServiceDetailStatus checkServiceHealth(String serviceName) {
        try {
            ServiceDetailStatus serviceStatus = new ServiceDetailStatus();
            serviceStatus.setServiceName(serviceName);

            // 获取服务详情
            ServiceInfo serviceInfo = namingService.getSubscribeServices().stream()
                    .filter(info -> info.getName().equals(serviceName))
                    .findFirst()
                    .orElse(null);

            if (serviceInfo != null) {
                serviceStatus.setGroupName(serviceInfo.getGroupName());
                serviceStatus.setProtectionThresholdEnabled(serviceInfo.isReachProtectionThreshold());
            }

            // 获取所有实例（强制从服务器拉取最新数据）
            List<Instance> allInstances = namingService.getAllInstances(serviceName, false);
            serviceStatus.setInstanceCount(allInstances.size());

            // 获取健康实例（实时检查）
            List<Instance> healthyInstances = allInstances.stream()
                    .filter(this::checkInstanceHealthRealTime)
                    .toList();
            serviceStatus.setHealthyInstanceCount(healthyInstances.size());

            // 按集群分组
            Map<String, List<Instance>> instancesByCluster = allInstances.stream()
                    .collect(Collectors.groupingBy(Instance::getClusterName));
            serviceStatus.setClusterCount(instancesByCluster.size());

            // 构建集群详情
            List<ServiceDetailStatus.ClusterDetail> clusterDetails = new ArrayList<>();
            instancesByCluster.forEach((clusterName, instances) -> {
                ServiceDetailStatus.ClusterDetail clusterDetail = new ServiceDetailStatus.ClusterDetail();
                clusterDetail.setClusterName(clusterName);

                List<ServiceDetailStatus.InstanceDetail> instanceDetails = instances.stream()
                        .map(instance -> {
                            ServiceDetailStatus.InstanceDetail detail = new ServiceDetailStatus.InstanceDetail();
                            detail.setIp(instance.getIp());
                            detail.setPort(instance.getPort());
                            detail.setEphemeral(instance.isEphemeral());
                            detail.setWeight(instance.getWeight());
                            // 使用实时健康检查结果
                            detail.setHealthy(checkInstanceHealthRealTime(instance));
                            detail.setMetadata(instance.getMetadata());
                            return detail;
                        })
                        .collect(Collectors.toList());

                clusterDetail.setInstances(instanceDetails);
                clusterDetails.add(clusterDetail);
            });

            serviceStatus.setClusters(clusterDetails);
            return serviceStatus;
        } catch (Exception e) {
            log.error("检查服务健康状态失败: {}", serviceName, e);
            return null;
        }
    }

    /**
     * 实时检查实例健康状态
     */
    private boolean checkInstanceHealthRealTime(Instance instance) {
        try {
            // 临时实例：直接使用Nacos的健康状态
            if (instance.isEphemeral()) {
                return instance.isHealthy();
            }

            // 持久化实例：综合判断
            // 1. 检查最后心跳时间
            Map<String, String> metadata = instance.getMetadata();
            if (metadata != null && metadata.containsKey("lastBeat")) {
                long lastBeat = Long.parseLong(metadata.get("lastBeat"));
                if (System.currentTimeMillis() - lastBeat > HEARTBEAT_TIMEOUT) {
                    return false;
                }
            }

            // 2. 主动TCP健康检查
            return checkTcpHealth(instance.getIp(), instance.getPort());
        } catch (Exception e) {
            log.warn("健康检查失败: {}:{}", instance.getIp(), instance.getPort(), e);
            return false;
        }
    }

    /**
     * TCP端口健康检查
     */
    private boolean checkTcpHealth(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), (int) HEALTH_CHECK_TIMEOUT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
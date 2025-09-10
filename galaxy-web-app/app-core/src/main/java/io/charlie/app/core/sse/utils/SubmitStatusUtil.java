package io.charlie.app.core.sse.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 10/09/2025
 * @description 提交状态 SSE 查询工具类
 */
@Slf4j
@Component
public class SubmitStatusUtil {
    // 存储活跃连接的映射
    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();
    // 存储客户端最后活动时间
    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();
    // 存储客户端心跳计数
    private final Map<String, AtomicLong> heartbeatCountMap = new ConcurrentHashMap<>();
    // 心跳调度器
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    // 使用更细粒度的锁
    private final Map<String, Object> clientLocks = new ConcurrentHashMap<>();

    // 连接超时时间（毫秒）
    private static final long CONNECTION_TIMEOUT = 30 * 60 * 1000L; // 30分钟
    // 心跳间隔时间（秒）
    private static final long HEARTBEAT_INTERVAL = 5L;
    // 客户端不活跃超时时间（毫秒）
    private static final long CLIENT_INACTIVE_TIMEOUT = 2 * 60 * 1000L; // 2分钟
    // 最大允许的心跳丢失次数
    private static final int MAX_MISSED_HEARTBEATS = 3;

}

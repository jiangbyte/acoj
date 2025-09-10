package io.charlie.app.core.sse.utils;

import io.charlie.app.core.sse.entity.SseEvent;
import io.charlie.app.core.sse.enums.EventEnums;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 10/09/2025
 * @description SSE 工具
 */
@Slf4j
@Component
public class SseUtil {
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

    public SseUtil() {
        // 启动心跳任务
        startHeartbeatTask();
    }

    public SseEmitter connection(String clientId) {
        log.info("为客户端建立 SSE 连接: {}", clientId);

        synchronized (getClientLock(clientId)) {
            try {
                // 如果已存在连接，先尝试优雅关闭
                SseEmitter existingEmitter = connections.get(clientId);
                if (existingEmitter != null) {
                    log.info("Closing existing connection for client: {}", clientId);
                    try {
                        // 不发送消息，直接完成，避免IO异常
                        existingEmitter.complete();
                    } catch (Exception e) {
                        log.info("Existing connection already closed for client: {}", clientId);
                    } finally {
                        // 立即清理资源，不等待回调
                        removeClientImmediately(clientId);
                    }
                }

                // 创建新的SSE发射器
                SseEmitter emitter = new SseEmitter(0L);
                connections.put(clientId, emitter);
                lastActivityMap.put(clientId, System.currentTimeMillis());
                heartbeatCountMap.put(clientId, new AtomicLong(0));

                // 简化回调处理
                emitter.onCompletion(() -> {
                    log.info("SSE connection completed for client: {}", clientId);
                    removeClientIfExists(clientId);
                });

                emitter.onTimeout(() -> {
                    log.warn("SSE connection timeout for client: {}", clientId);
                    removeClientIfExists(clientId);
                });

                emitter.onError((ex) -> {
                    // 忽略客户端中止连接的异常
                    if (!isClientAbortException(ex)) {
                        log.error("SSE connection error for client: {}", clientId, ex);
                    } else {
                        log.info("Client aborted connection: {}", clientId);
                    }
                    removeClientIfExists(clientId);
                });

                // 异步发送初始数据，避免阻塞连接建立
                asyncSendInitialData(clientId);

                log.info("SSE connection established for client: {}", clientId);
                return emitter;

            } catch (Exception e) {
                log.error("Failed to establish connection for client: {}", clientId, e);
                removeClientIfExists(clientId);
                throw new RuntimeException("Failed to establish SSE connection", e);
            }
        }
    }

    /**
     * 安全的移除客户端（如果存在）
     */
    public void removeClientIfExists(String clientId) {
        synchronized (getClientLock(clientId)) {
            if (connections.containsKey(clientId)) {
                removeClientImmediately(clientId);
                clientLocks.remove(clientId); // 清理锁
            }
        }
    }

    /**
     * 立即移除客户端资源
     */
    public void removeClientImmediately(String clientId) {
        try {
            SseEmitter emitter = connections.remove(clientId);
            if (emitter != null) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    // 忽略完成时的异常
                }
            }
        } finally {
            lastActivityMap.remove(clientId);
            heartbeatCountMap.remove(clientId);
            log.info("Client resources cleaned up: {}", clientId);
        }
    }

    /**
     * 异步发送初始数据
     */
    public void asyncSendInitialData(String clientId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100); // 短暂延迟，确保连接稳定
                SseEvent<String> initialEvent = new SseEvent<>(EventEnums.INITIAL_DATA.getValue(), "Welcome! Connection established at " + LocalDateTime.now());
                sendEventSafely(clientId, initialEvent);
            } catch (Exception e) {
                log.info("Failed to send initial data to client: {}", clientId, e);
            }
        });
    }

    /**
     * 安全发送事件（处理客户端中止）
     */
    public void sendEventSafely(String clientId, SseEvent<?> event) {
        SseEmitter emitter = connections.get(clientId);
        if (emitter != null) {
            try {
                sendEventToEmitter(emitter, event);
            } catch (Exception e) {
                if (isClientAbortException(e)) {
                    log.info("Client aborted during event sending: {}", clientId);
                    removeClientIfExists(clientId);
                } else {
                    log.error("Failed to send event to client: {}", clientId, e);
                }
            }
        }
    }

    /**
     * 获取客户端专用的锁对象
     */
    private Object getClientLock(String clientId) {
        return clientLocks.computeIfAbsent(clientId, k -> new Object());
    }

    public void close(String clientId) {
        SseEmitter emitter = connections.get(clientId);
        if (emitter != null) {
            try {
                // 发送连接关闭事件
                SseEvent<String> closeEvent = new SseEvent<>(EventEnums.CONNECTION_CLOSED.getValue(), "Connection closed by server due to inactivity");
                sendEventToEmitter(emitter, closeEvent);

                emitter.complete();
            } catch (Exception e) {
                log.warn("Error while closing connection for client: {}", clientId, e);
            } finally {
                removeClient(clientId);
                log.info("SSE connection closed for client: {}", clientId);
            }
        }
    }

    public void message(String clientId, SseEvent<?> event) {
        SseEmitter emitter = connections.get(clientId);
        if (emitter != null) {
            try {
                sendEventToEmitter(emitter, event);
                log.info("Message sent to client: {}, event: {}", clientId, event.getEventType());
            } catch (Exception e) {
                log.error("Failed to send message to client: {}", clientId, e);
                close(clientId);
            }
        } else {
            log.warn("Client not found: {}", clientId);
        }
    }

    public void broadcast(SseEvent<?> event) {
        log.info("Broadcasting event: {}", event.getEventType());
        connections.forEach((clientId, emitter) -> {
            try {
                sendEventToEmitter(emitter, event);
            } catch (Exception e) {
                log.error("Failed to broadcast to client: {}", clientId, e);
                close(clientId);
            }
        });
    }

    public Map<String, SseEmitter> getActiveConnections() {
        return Map.copyOf(connections);
    }

    public boolean isClientActive(String clientId) {
        return connections.containsKey(clientId);
    }

    public void serverHeartbeat() {
        long timeMillis = System.currentTimeMillis();

        // 广播心跳并更新计数
        connections.forEach((clientId, emitter) -> {
            Map<String, Object> heartbeatData = new HashMap<>();
            heartbeatData.put("timestamp", timeMillis);
            heartbeatData.put("missedHeartbeats", heartbeatCountMap.getOrDefault(clientId, new AtomicLong(0)).get());
            heartbeatData.put("lastActivity", lastActivityMap.getOrDefault(clientId, 0L));
            heartbeatData.put("clientId", clientId);
            heartbeatData.put("serverTime", System.currentTimeMillis());

            SseEvent<Map<String, Object>> heartbeatEvent = new SseEvent<>(EventEnums.HEARTBEAT.getValue(), heartbeatData);

            try {
                sendEventToEmitter(emitter, heartbeatEvent);
                // 增加心跳计数（客户端需要在下次心跳时重置）
                AtomicLong heartbeatCount = heartbeatCountMap.get(clientId);
                if (heartbeatCount != null) {
                    long currentCount = heartbeatCount.incrementAndGet();
                    if (currentCount > MAX_MISSED_HEARTBEATS) {
                        log.warn("Client {} has missed too many heartbeats: {}", clientId, currentCount);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send heartbeat to client: {}", clientId, e);
                close(clientId);
            }
        });

        log.info("Server heartbeat sent to {} clients", connections.size());
    }

    public void clientHeartbeat(String clientId) {
        log.info("Received heartbeat from client: {}", clientId);

        if (!connections.containsKey(clientId)) {
            log.warn("Heartbeat received from non-existent client: {}", clientId);
            return;
        }

        // 更新最后活动时间
        lastActivityMap.put(clientId, System.currentTimeMillis());

        // 重置心跳计数
        AtomicLong heartbeatCount = heartbeatCountMap.get(clientId);
        if (heartbeatCount != null) {
            heartbeatCount.set(0);
        }

        // 发送心跳确认响应
        SseEvent<String> ackEvent = new SseEvent<>(EventEnums.HEARTBEAT_ACK.getValue(), "heartbeat_acknowledged");
        message(clientId, ackEvent);

        log.info("Heartbeat processed for client: {}", clientId);
    }

    public void sendInitialData(String clientId) {
        SseEvent<String> initialEvent = new SseEvent<>(EventEnums.INITIAL_DATA.getValue(), "Welcome! Connection established at " + System.currentTimeMillis());
        message(clientId, initialEvent);
    }

    /**
     * 修改sendEventToEmitter方法，添加异常处理
     */
    public void sendEventToEmitter(SseEmitter emitter, SseEvent<?> event) throws IOException {
        try {
            SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().name(event.getEventType()).data(event.getData(), MediaType.APPLICATION_JSON).id(String.valueOf(System.currentTimeMillis())).reconnectTime(5000L);

            emitter.send(eventBuilder);
        } catch (Exception e) {
            // 连接已关闭的异常
            throw new IOException("Connection already closed", e);
        }
    }

    /**
     * 启动心跳任务
     */
    public void startHeartbeatTask() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                serverHeartbeat();
                log.info("Server heartbeat sent to all clients");
            } catch (Exception e) {
                log.error("Error in heartbeat task", e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 清理不活跃的连接
     */
    @Scheduled(fixedRate = 60 * 1000) // 每1分钟检查一次
    public void cleanupInactiveConnections() {
        long currentTime = System.currentTimeMillis();
        int cleanedCount = 0;
        int totalConnections = connections.size();

        log.info("Checking for inactive connections, current active: {}", totalConnections);

        // 检查不活跃的连接
        for (Map.Entry<String, Long> entry : lastActivityMap.entrySet()) {
            String clientId = entry.getKey();
            long lastActivityTime = entry.getValue();

            // 检查是否超时
            if (currentTime - lastActivityTime > CLIENT_INACTIVE_TIMEOUT) {
                log.warn("Client {} inactive for {} ms, closing connection", clientId, currentTime - lastActivityTime);
                close(clientId);
                cleanedCount++;
                continue;
            }

            // 检查心跳丢失情况
            AtomicLong missedHeartbeats = heartbeatCountMap.get(clientId);
            if (missedHeartbeats != null && missedHeartbeats.get() >= MAX_MISSED_HEARTBEATS) {
                log.warn("Client {} missed {} heartbeats, closing connection", clientId, missedHeartbeats.get());
                close(clientId);
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            log.info("Cleaned up {} inactive connections. Remaining active: {}", cleanedCount, connections.size());
        }

        // 记录连接统计信息
        logConnectionStats();
    }

    /**
     * 记录连接统计信息
     */
    public void logConnectionStats() {
        if (!connections.isEmpty()) {
            StringBuilder stats = new StringBuilder("Connection statistics: ");
            connections.keySet().forEach(clientId -> {
                Long lastActivity = lastActivityMap.get(clientId);
                AtomicLong missedHeartbeats = heartbeatCountMap.get(clientId);
                long inactiveTime = lastActivity != null ? (System.currentTimeMillis() - lastActivity) / 1000 : -1;

                stats.append(String.format("[%s: inactive %ds, missed %d heartbeats], ", clientId, inactiveTime, missedHeartbeats != null ? missedHeartbeats.get() : -1));
            });
            log.info(stats.toString());
        }
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up SSE service resources");
        heartbeatScheduler.shutdown();
        // 关闭所有连接
        connections.keySet().forEach(this::close);
    }

    /**
     * 线程安全的客户端移除
     */
    public void removeClient(String clientId) {
        synchronized (getClientLock(clientId)) {
            SseEmitter emitter = connections.get(clientId);
            if (emitter != null) {
                connections.remove(clientId);
            }
            lastActivityMap.remove(clientId);
            heartbeatCountMap.remove(clientId);
            log.info("Client {} completely removed from SSE service", clientId);
        }
    }

    /**
     * 检查异常是否表示客户端中止
     */
    public boolean isClientAbortException(Throwable ex) {
        if (ex == null) return false;

        // 检查IOException及其原因
        if (ex instanceof IOException) {
            String message = ex.getMessage();
            if (message != null && (message.contains("中止") || message.contains("aborted") || message.contains("broken pipe") || message.contains("Connection reset"))) {
                return true;
            }
        }

        // 检查Spring的AsyncRequestNotUsableException
        if (ex instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException) {
            return true;
        }

        // 检查Tomcat的ClientAbortException
        if (ex.getClass().getName().contains("ClientAbortException")) {
            return true;
        }

        // 递归检查cause
        return isClientAbortException(ex.getCause());
    }

    public int getActiveConnectionCount() {
        return connections.size();
    }

    public Set<String> getConnectedClients() {
        return Collections.unmodifiableSet(connections.keySet());
    }

    public Map<String, Long> getConnectionDurations() {
        Map<String, Long> durations = new HashMap<>();
        long now = System.currentTimeMillis();
        lastActivityMap.forEach((clientId, lastActivity) -> {
            durations.put(clientId, now - lastActivity);
        });
        return durations;
    }

}

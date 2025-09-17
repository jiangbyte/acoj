package io.charlie.app.core.sse.utils;

import cn.hutool.core.util.IdUtil;
import io.charlie.app.core.sse.entity.SseEvent;
import io.charlie.app.core.sse.enums.EventEnums;
import io.charlie.app.core.sse.enums.SseTypeEnums;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 10/09/2025
 * @description 通用 SSE 工具
 */
@Slf4j
@Component
public class SseUtil {
    // 存储活跃连接的映射
    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
    // 存储客户端最后活动时间
    private final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();
    // 存储客户端心跳计数
    private final Map<String, AtomicLong> heartbeatCountMap = new ConcurrentHashMap<>();
    // 客户端专用锁
    private final Map<String, ReentrantLock> clientLocks = new ConcurrentHashMap<>();
    // 连接超时时间（毫秒）
    private static final long CONNECTION_TIMEOUT = 30 * 60 * 1000L;
    // 心跳间隔时间（秒）
    private static final long HEARTBEAT_INTERVAL = 5L;
    // 客户端不活跃超时时间（毫秒）
    private static final long CLIENT_INACTIVE_TIMEOUT = 2 * 60 * 1000L;
    // 最大允许的心跳丢失次数
    private static final int MAX_MISSED_HEARTBEATS = 3;
    // 心跳调度器
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "sse-heartbeat-thread");
        thread.setDaemon(true);
        return thread;
    });

    public SseUtil() {
        startHeartbeatTask();
    }

    /**
     * 生成连接ID
     */
    public String generateConnectId(SseTypeEnums sseType, String id) {
        if (sseType == null) {
            return "default_" + id;
        } else {
            return sseType.getValue() + "_" + id;
        }
    }

    /**
     * 建立SSE连接
     */
    public SseEmitter connect(SseTypeEnums sseType, String clientId) {
        String connectId = generateConnectId(sseType, clientId);
        ReentrantLock lock = getClientLock(connectId);

        lock.lock();
        try {
            log.info("Establishing SSE connection for client: {}", connectId);

            // 清理现有连接
            cleanupExistingConnection(connectId);

            // 创建新的SSE发射器
            SseEmitter emitter = createSseEmitter(connectId);

            // 存储连接信息
            storeConnectionInfo(connectId, emitter);

            // 异步发送初始数据
            sendInitialDataAsync(connectId);

            log.info("SSE connection established for client: {}", connectId);
            return emitter;

        } catch (Exception e) {
            log.error("Failed to establish connection for client: {}", connectId, e);
            removeClientIfExists(connectId);
            throw new RuntimeException("Failed to establish SSE connection", e);
        } finally {
            lock.unlock();
        }
    }

    public SseEmitter connect(String sseType, String clientId) {
        String connectId = generateConnectId(SseTypeEnums.fromValue(sseType), clientId);
        ReentrantLock lock = getClientLock(connectId);

        lock.lock();
        try {
            log.info("Establishing SSE connection for client: {}", connectId);

            // 清理现有连接
            cleanupExistingConnection(connectId);

            // 创建新的SSE发射器
            SseEmitter emitter = createSseEmitter(connectId);

            // 存储连接信息
            storeConnectionInfo(connectId, emitter);

            // 异步发送初始数据
            sendInitialDataAsync(connectId);

            log.info("SSE connection established for client: {}", connectId);
            return emitter;

        } catch (Exception e) {
            log.error("Failed to establish connection for client: {}", connectId, e);
            removeClientIfExists(connectId);
            throw new RuntimeException("Failed to establish SSE connection", e);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 清理现有连接
     */
    private void cleanupExistingConnection(String connectId) {
        SseEmitter existingEmitter = sseEmitterMap.get(connectId);
        if (existingEmitter != null) {
            log.info("Closing existing connection for client: {}", connectId);
            completeEmitterSilently(existingEmitter);
            removeClientImmediately(connectId);
        }
    }

    /**
     * 创建SSE发射器
     */
    private SseEmitter createSseEmitter(String connectId) {
        SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("SSE connection completed for client: {}", connectId);
            removeClientIfExists(connectId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for client: {}", connectId);
            removeClientIfExists(connectId);
        });

        emitter.onError(ex -> {
            if (!isClientAbortException(ex)) {
                log.error("SSE connection error for client: {}", connectId, ex);
            } else {
                log.info("Client aborted connection: {}", connectId);
            }
            removeClientIfExists(connectId);
        });

        return emitter;
    }

    /**
     * 存储连接信息
     */
    private void storeConnectionInfo(String connectId, SseEmitter emitter) {
        sseEmitterMap.put(connectId, emitter);
        lastActivityMap.put(connectId, System.currentTimeMillis());
        heartbeatCountMap.put(connectId, new AtomicLong(0));
    }

    /**
     * 安全移除客户端
     */
    public void removeClientIfExists(String connectId) {
        ReentrantLock lock = getClientLock(connectId);
        lock.lock();
        try {
            if (sseEmitterMap.containsKey(connectId)) {
                removeClientImmediately(connectId);
            }
        } finally {
            lock.unlock();
            // 谨慎清理锁，避免内存泄漏
            if (!sseEmitterMap.containsKey(connectId)) {
                clientLocks.remove(connectId);
            }
        }
    }

    /**
     * 立即移除客户端资源
     */
    private void removeClientImmediately(String connectId) {
        try {
            SseEmitter emitter = sseEmitterMap.remove(connectId);
            if (emitter != null) {
                completeEmitterSilently(emitter);
            }
        } finally {
            lastActivityMap.remove(connectId);
            heartbeatCountMap.remove(connectId);
            log.info("Client resources cleaned up: {}", connectId);
        }
    }

    /**
     * 安全完成发射器
     */
    private void completeEmitterSilently(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            // 忽略完成时的异常
        }
    }

    /**
     * 异步发送初始数据
     */
    private void sendInitialDataAsync(String connectId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                SseEvent<String> initialEvent = new SseEvent<>(
                        EventEnums.INITIAL_DATA.getValue(),
                        "Connection established at " + LocalDateTime.now()
                );
                sendEventSafely(connectId, initialEvent);
            } catch (Exception e) {
                log.debug("Failed to send initial data to client: {}", connectId);
            }
        });
    }

    /**
     * 安全发送事件
     */
    public void sendEventSafely(String connectId, SseEvent<?> event) {
        SseEmitter emitter = sseEmitterMap.get(connectId);
        if (emitter != null) {
            try {
                sendEventToEmitter(emitter, event);
                updateClientActivity(connectId);
            } catch (Exception e) {
                if (isClientAbortException(e)) {
                    log.info("Client aborted during event sending: {}", connectId);
                    removeClientIfExists(connectId);
                } else {
                    log.error("Failed to send event to client: {}", connectId, e);
                }
            }
        }
    }

    /**
     * 更新客户端活动时间
     */
    private void updateClientActivity(String connectId) {
        lastActivityMap.put(connectId, System.currentTimeMillis());
    }

    /**
     * 获取客户端锁
     */
    private ReentrantLock getClientLock(String connectId) {
        return clientLocks.computeIfAbsent(connectId, k -> new ReentrantLock());
    }

    /**
     * 关闭连接
     */
    public void closeConnection(String connectId) {
        ReentrantLock lock = getClientLock(connectId);
        lock.lock();
        try {
            SseEmitter emitter = sseEmitterMap.get(connectId);
            if (emitter != null) {
                try {
                    SseEvent<String> closeEvent = new SseEvent<>(
                            EventEnums.CONNECTION_CLOSED.getValue(),
                            "Connection closed by server"
                    );
                    sendEventToEmitter(emitter, closeEvent);
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("Error while closing connection for client: {}", connectId, e);
                } finally {
                    removeClientImmediately(connectId);
                    log.info("SSE connection closed for client: {}", connectId);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(String connectId, SseEvent<?> event) {
        SseEmitter emitter = sseEmitterMap.get(connectId);
        if (emitter != null) {
            try {
                sendEventToEmitter(emitter, event);
                updateClientActivity(connectId);
                log.debug("Message sent to client: {}, event: {}", connectId, event.getEventType());
            } catch (Exception e) {
                log.error("Failed to send message to client: {}", connectId, e);
                closeConnection(connectId);
            }
        } else {
            log.warn("Client not found: {}", connectId);
        }
    }


    /**
     * 发送消息
     */
    public void sendMessage(SseTypeEnums sseType, String id, SseEvent<?> event) {
        String connectId = generateConnectId(sseType, id);
        SseEmitter emitter = sseEmitterMap.get(connectId);
        if (emitter != null) {
            try {
                sendEventToEmitter(emitter, event);
                updateClientActivity(connectId);
                log.debug("Message sent to client: {}, event: {}", connectId, event.getEventType());
            } catch (Exception e) {
                log.error("Failed to send message to client: {}", connectId, e);
                closeConnection(connectId);
            }
        } else {
            log.warn("Client not found: {}", connectId);
        }
    }

    /**
     * 广播消息
     */
    public void broadcast(SseEvent<?> event) {
        log.info("Broadcasting event: {} to {} clients", event.getEventType(), sseEmitterMap.size());

        List<String> failedClients = new ArrayList<>();
        sseEmitterMap.forEach((connectId, emitter) -> {
            try {
                sendEventToEmitter(emitter, event);
                updateClientActivity(connectId);
            } catch (Exception e) {
                log.error("Failed to broadcast to client: {}", connectId, e);
                failedClients.add(connectId);
            }
        });

        // 清理失败的连接
        failedClients.forEach(this::closeConnection);
    }

    /**
     * 广播消息
     */
    public void broadcast(SseTypeEnums sseType, SseEvent<?> event) {
        log.info("Broadcasting event: {} to {} clients", event.getEventType(), sseEmitterMap.size());

        List<String> failedClients = new ArrayList<>();
        sseEmitterMap.forEach((connectId, emitter) -> {
            // 如果不是当前sseType的sseType，则跳过
            if (!connectId.startsWith(sseType.getValue())) {
                return;
            }

            try {
                sendEventToEmitter(emitter, event);
                updateClientActivity(connectId);
            } catch (Exception e) {
                log.error("Failed to broadcast to client: {}", connectId, e);
                failedClients.add(connectId);
            }
        });

        // 清理失败的连接
        failedClients.forEach(this::closeConnection);
    }

    /**
     * 发送事件到发射器
     */
    private void sendEventToEmitter(SseEmitter emitter, SseEvent<?> event) throws IOException {
        try {
            SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                    .name(event.getEventType())
                    .data(event.getData(), MediaType.APPLICATION_JSON)
                    .id(UUID.randomUUID().toString())
                    .reconnectTime(5000L);

            emitter.send(eventBuilder);
        } catch (IllegalStateException e) {
            throw new IOException("Connection already closed", e);
        }
    }

    /**
     * 服务器心跳
     */
    public void sendServerHeartbeat() {
        long currentTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<String, SseEmitter> entry : sseEmitterMap.entrySet()) {
            String connectId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            Map<String, Object> heartbeatData = createHeartbeatData(connectId, currentTime);
            SseEvent<Map<String, Object>> heartbeatEvent = new SseEvent<>(
                    EventEnums.HEARTBEAT.getValue(),
                    heartbeatData
            );

            try {
                sendEventToEmitter(emitter, heartbeatEvent);
                incrementHeartbeatCount(connectId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send heartbeat to client: {}", connectId, e);
                failureCount++;
                closeConnection(connectId);
            }
        }

        if (successCount > 0 || failureCount > 0) {
            log.debug("Heartbeat sent: {} success, {} failures", successCount, failureCount);
        }
    }

    /**
     * 创建心跳数据
     */
    private Map<String, Object> createHeartbeatData(String connectId, long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", timestamp);
        data.put("missedHeartbeats", getMissedHeartbeatCount(connectId));
        data.put("lastActivity", lastActivityMap.getOrDefault(connectId, 0L));
        data.put("connectId", connectId);
        data.put("serverTime", System.currentTimeMillis());
        return data;
    }

    /**
     * 增加心跳计数
     */
    private void incrementHeartbeatCount(String connectId) {
        AtomicLong heartbeatCount = heartbeatCountMap.get(connectId);
        if (heartbeatCount != null) {
            long currentCount = heartbeatCount.incrementAndGet();
            if (currentCount > MAX_MISSED_HEARTBEATS) {
                log.warn("Client {} has missed too many heartbeats: {}", connectId, currentCount);
            }
        }
    }

    /**
     * 获取丢失的心跳计数
     */
    private long getMissedHeartbeatCount(String connectId) {
        AtomicLong heartbeatCount = heartbeatCountMap.get(connectId);
        return heartbeatCount != null ? heartbeatCount.get() : 0;
    }

    /**
     * 处理客户端心跳
     */
    public void handleClientHeartbeat(SseTypeEnums sseType, String id) {
        String connectId = generateConnectId(sseType, id);
        log.debug("Received heartbeat from client: {}", connectId);

        if (!sseEmitterMap.containsKey(connectId)) {
            log.warn("Heartbeat received from non-existent client: {}", connectId);
            return;
        }

        updateClientActivity(connectId);
        resetHeartbeatCount(connectId);

        SseEvent<String> ackEvent = new SseEvent<>(
                EventEnums.HEARTBEAT_ACK.getValue(),
                "heartbeat_acknowledged"
        );
        sendMessage(connectId, ackEvent);

        log.debug("Heartbeat processed for client: {}", connectId);
    }

    /**
     * 处理客户端心跳
     */
    public void handleClientHeartbeat(String sseType, String id) {
        String connectId = generateConnectId(SseTypeEnums.fromValue(sseType), id);
        log.debug("Received heartbeat from client: {}", connectId);

        if (!sseEmitterMap.containsKey(connectId)) {
            log.warn("Heartbeat received from non-existent client: {}", connectId);
            return;
        }

        updateClientActivity(connectId);
        resetHeartbeatCount(connectId);

        SseEvent<String> ackEvent = new SseEvent<>(
                EventEnums.HEARTBEAT_ACK.getValue(),
                "heartbeat_acknowledged"
        );
        sendMessage(connectId, ackEvent);

        log.debug("Heartbeat processed for client: {}", connectId);
    }

    /**
     * 重置心跳计数
     */
    private void resetHeartbeatCount(String connectId) {
        AtomicLong heartbeatCount = heartbeatCountMap.get(connectId);
        if (heartbeatCount != null) {
            heartbeatCount.set(0);
        }
    }

    /**
     * 启动心跳任务
     */
    private void startHeartbeatTask() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                sendServerHeartbeat();
            } catch (Exception e) {
                log.error("Error in heartbeat task", e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 清理不活跃连接
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void cleanupInactiveConnections() {
        long currentTime = System.currentTimeMillis();
        int cleanedCount = 0;

        for (String connectId : new ArrayList<>(sseEmitterMap.keySet())) {
            Long lastActivityTime = lastActivityMap.get(connectId);
            if (lastActivityTime == null) continue;

            long inactiveTime = currentTime - lastActivityTime;
            long missedHeartbeats = getMissedHeartbeatCount(connectId);

            if (inactiveTime > CLIENT_INACTIVE_TIMEOUT) {
                log.warn("Client {} inactive for {}ms, closing connection", connectId, inactiveTime);
                closeConnection(connectId);
                cleanedCount++;
            } else if (missedHeartbeats >= MAX_MISSED_HEARTBEATS) {
                log.warn("Client {} missed {} heartbeats, closing connection", connectId, missedHeartbeats);
                closeConnection(connectId);
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            log.info("Cleaned up {} inactive connections", cleanedCount);
        }
    }

    /**
     * 检查客户端中止异常
     */
    private boolean isClientAbortException(Throwable ex) {
        if (ex == null) return false;

        // 检查常见的客户端中止异常
        if (ex instanceof IOException) {
            String message = ex.getMessage();
            if (message != null && (message.contains("Broken pipe") ||
                    message.contains("Connection reset") ||
                    message.contains("aborted"))) {
                return true;
            }
        }

        // 检查特定的异常类
        String className = ex.getClass().getName();
        if (className.contains("ClientAbortException") ||
                className.contains("AsyncRequestNotUsableException")) {
            return true;
        }

        // 递归检查原因
        return isClientAbortException(ex.getCause());
    }

    /**
     * 应用关闭时清理
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SSE service");

        // 关闭心跳调度器
        heartbeatScheduler.shutdown();
        try {
            if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                heartbeatScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            heartbeatScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 关闭所有连接
        new ArrayList<>(sseEmitterMap.keySet()).forEach(this::closeConnection);

        log.info("SSE service shutdown completed");
    }

    // ========== 监控和统计方法 ==========

    public int getActiveConnectionCount() {
        return sseEmitterMap.size();
    }

    public Set<String> getConnectedClients() {
        return Collections.unmodifiableSet(sseEmitterMap.keySet());
    }

    public Map<String, Long> getConnectionDurations() {
        Map<String, Long> durations = new HashMap<>();
        long now = System.currentTimeMillis();
        lastActivityMap.forEach((connectId, lastActivity) -> {
            durations.put(connectId, now - lastActivity);
        });
        return durations;
    }

    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeConnections", getActiveConnectionCount());
        stats.put("connectionDurations", getConnectionDurations());
        return stats;
    }
}
package io.charlie.web.oj.modular.websocket.utils;

import io.charlie.web.oj.modular.websocket.config.WebSocketConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description websocket 工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUtil {
    private final SimpMessagingTemplate messagingTemplate;

    // 消息统计
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> topicMessageCounts = new ConcurrentHashMap<>();

    /**
     * 推送消息到指定主题
     *
     * @param topic         主题
     * @param destinationId 目标ID
     * @param message       消息内容
     */
    public void sendToTopic(String topic, String destinationId, Object message) {
        String destination = topic + "/" + destinationId;
        try {
//            log.info("推送消息: {} -> {}", destination, message);
            log.info("推送消息: {}", destination);
            messagingTemplate.convertAndSend(destination, message);
            recordMessageSent(topic);
        } catch (Exception e) {
            log.error("推送消息失败: {} -> {}", destination, message, e);
            throw new WebSocketException("消息推送失败: " + e.getMessage(), e);
        }
    }


    public void sendToTopicClose(String topic, String destinationId) {
        String destination = topic + "/" + destinationId;
        String message = WebSocketConfig.CLOSE_CONNECTION;
        try {
            log.info("关闭推送消息: {} -> {}", destination, message);
            messagingTemplate.convertAndSend(destination, message);
            recordMessageSent(topic);
        } catch (Exception e) {
            log.error("关闭推送消息失败: {} -> {}", destination, message, e);
            throw new WebSocketException("关闭消息推送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 推送判题状态消息
     *
     * @param submissionId 提交ID
     * @param message      消息内容
     */
    public void sendJudgeStatus(String submissionId, Object message) {
        sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, submissionId, message);
    }

    /**
     * 推送相似度检测状态消息
     *
     * @param taskId  任务ID
     * @param message 消息内容
     */
    public void sendSimilarityStatus(String taskId, Object message) {
        sendToTopic(WebSocketConfig.TOPIC_SIMILARITY_STATUS, taskId, message);
    }

    /**
     * 推送点对点消息（给特定用户）
     *
     * @param userId  用户ID
     * @param topic   主题
     * @param message 消息内容
     */
    public void sendToUser(String userId, String topic, Object message) {
        String destination = "/user/" + userId + topic;
        try {
            log.debug("推送用户消息: {} -> {}", destination, message);
            messagingTemplate.convertAndSendToUser(userId, topic, message);
            recordMessageSent("user/" + topic);
        } catch (Exception e) {
            log.error("推送用户消息失败: {} -> {}", destination, message, e);
            throw new WebSocketException("用户消息推送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 记录消息发送统计
     */
    private void recordMessageSent(String topic) {
        totalMessagesSent.incrementAndGet();
        topicMessageCounts.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取总消息发送数量
     */
    public long getTotalMessagesSent() {
        return totalMessagesSent.get();
    }

    /**
     * 获取指定主题的消息发送数量
     */
    public long getTopicMessageCount(String topic) {
        return topicMessageCounts.getOrDefault(topic, new AtomicLong(0)).get();
    }

    /**
     * 自定义WebSocket异常
     */
    public static class WebSocketException extends RuntimeException {
        public WebSocketException(String message) {
            super(message);
        }

        public WebSocketException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

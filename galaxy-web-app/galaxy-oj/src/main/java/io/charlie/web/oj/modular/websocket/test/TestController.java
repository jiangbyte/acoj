package io.charlie.web.oj.modular.websocket.test;

import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.modular.data.submit.param.DataSubmitExeParam;
import io.charlie.web.oj.modular.data.submit.service.DataSubmitService;
import io.charlie.web.oj.modular.websocket.config.WebSocketConfig;
import io.charlie.web.oj.modular.websocket.utils.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {
//    private final WebSocketUtil webSocketUtil;
//    private final DataSubmitService dataSubmitService;
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
//    private final Random random = new Random();
//
//    // 存储正在处理的任务，用于确保消息送达后再关闭连接
//    private final ConcurrentHashMap<String, Boolean> processingTasks = new ConcurrentHashMap<>();


    // 处理客户端发送的消息
//    @MessageMapping("/judge/status/{submitId}")
//    public void handleJudgeStatusMessage(String message,
//                                         @Header("Authorization") String token,
//                                         @DestinationVariable String submitId) {
//        log.info("收到客户端消息 - submitId: {}, token: {}, 内容: {}", submitId, token, message);
////        DataSubmitExeParam bean = JSONUtil.toBean(message, DataSubmitExeParam.class);
////        dataSubmitService.handleProblemSubmit(bean);
//
////        // 立即回复确认消息
////        String immediateResponse = "服务端已收到消息: " + message;
////        messagingTemplate.convertAndSend("/topic/judge/status/" + submitId, immediateResponse);
////        WebSocketConfig.TOPIC_JUDGE_STATUS + submitId;
////        // 开始向客户端发送随机消息序列
////        sendRandomMessages(submitId);
//    }

    // 处理客户端订阅事件（可选）
//    @SubscribeMapping("/topic/judge/status/{submitId}")
//    public String handleSubscription(@DestinationVariable String submitId) {
//        log.info("客户端订阅了主题: /topic/judge/status/{}", submitId);
//        return "订阅成功，开始接收判题状态更新";
//    }
//
//    private void sendRandomMessages(String submitId) {
//        final String[] testMessages = {
//                "开始判题处理...",
//                "编译代码中...",
//                "运行测试用例1...",
//                "测试用例1通过",
//                "运行测试用例2...",
//                "测试用例2通过",
//                "运行测试用例3...",
//                "测试用例3通过",
//                "所有测试用例完成",
//                "判题结束，生成结果"
//        };
//
//        final int[] index = {0};
//
//        Runnable sendTask = new Runnable() {
//            @Override
//            public void run() {
//                if (index[0] < testMessages.length) {
//                    String message = testMessages[index[0]];
//                    String fullMessage = message + " (时间戳: " + System.currentTimeMillis() + ")";
//
//                    try {
//                        // 发送消息到客户端订阅的主题
//                        messagingTemplate.convertAndSend("/topic/judge/status/" + submitId, fullMessage);
//                        log.info("向客户端 {} 发送消息: {}", submitId, message);
//                    } catch (Exception e) {
//                        log.error("发送消息到客户端 {} 失败: {}", submitId, e.getMessage());
//                        return;
//                    }
//
//                    index[0]++;
//
//                    // 计算下一个消息的随机延迟 (300-2000ms)
//                    int nextDelay = random.nextInt(1700) + 300;
//                    if (index[0] < testMessages.length) {
//                        scheduler.schedule(this, nextDelay, TimeUnit.MILLISECONDS);
//                    } else {
//                        // 所有消息发送完成后发送结束消息
//                        String endMessage = "所有消息发送完成，连接即将关闭";
//                        messagingTemplate.convertAndSend("/topic/judge/status/" + submitId, endMessage);
//                        log.info("向客户端 {} 发送结束消息", submitId);
//                    }
//                }
//            }
//        };
//
//        // 首次延迟500ms后开始发送
//        scheduler.schedule(sendTask, 500, TimeUnit.MILLISECONDS);
//    }
//
//
//
//    // 模拟提交(提交前客户端已经与服务端建立联系，submitId就是标识符，一对一的建立联系)
//    @GetMapping("/atest/submit/{submitId}")
//    public String submit(@PathVariable String submitId) {
//        log.info("收到提交请求，submitId: {}", submitId);
//
//        // 将任务标记为处理中
//        processingTasks.put(submitId, true);
//
//        // 立即返回确认响应
//        return "OK";
//    }
//
//    // 人为触发模拟动态处理过程触发，对提交的 submitId 进行动态结果推送，一对一推送
//    @GetMapping("/atest/touch-dynamic-result/{submitId}")
//    public String touchDynamicResult(@PathVariable String submitId) {
//        log.info("触发动态结果推送，submitId: {}", submitId);
//
//        if (!processingTasks.containsKey(submitId)) {
//            return "错误：未找到对应的提交任务";
//        }
//
//        // 模拟处理过程，分多个阶段推送状态
//        simulateProcessingStages(submitId);
//
//        return "已开始推送动态结果";
//    }
//
//    /**
//     * 模拟处理阶段并推送动态结果
//     */
//    private void simulateProcessingStages(String submitId) {
//        // 定义处理阶段
//        String[] stages = {
//                "代码编译中...",
//                "编译完成，开始运行测试用例",
//                "测试用例1通过",
//                "测试用例2通过",
//                "测试用例3运行中...",
//                "所有测试用例通过",
//                "代码质量分析中...",
//                "分析完成，生成报告"
//        };
//
//        // 定义可能的最终状态
//        String[] finalStatuses = {"ACCEPTED", "WRONG_ANSWER", "TIME_LIMIT_EXCEEDED", "COMPILE_ERROR"};
//
//        // 为每个阶段安排推送
//        for (int i = 0; i < stages.length; i++) {
//            final int stageIndex = i;
//            scheduler.schedule(() -> {
//                if (processingTasks.containsKey(submitId)) {
//                    // 构建状态消息
//                    StatusMessage message = new StatusMessage();
//                    message.setSubmitId(submitId);
//                    message.setStage(stages[stageIndex]);
//                    message.setProgress((stageIndex + 1) * 100 / stages.length);
//                    message.setTimestamp(System.currentTimeMillis());
//
//                    // 如果是最后阶段，设置最终状态
//                    if (stageIndex == stages.length - 1) {
//                        String finalStatus = finalStatuses[random.nextInt(finalStatuses.length)];
//                        message.setFinalStatus(finalStatus);
//                        message.setCompleted(true);
//
//                        // 延迟一段时间后移除任务
//                        scheduler.schedule(() -> {
//                            processingTasks.remove(submitId);
//                            log.info("任务完成，移除submitId: {}", submitId);
//                        }, 2, TimeUnit.SECONDS);
//                    }
//
//                    // 通过WebSocket推送消息到特定用户
//                    webSocketUtil.sendToTopic(WebSocketConfig.TOPIC_JUDGE_STATUS, submitId, message);
//                    log.info("推送状态消息: {} -> {}", submitId, stages[stageIndex]);
//                }
//            }, (i + 1) * 2L, TimeUnit.SECONDS); // 每个阶段间隔2秒
//        }
//    }
//
//    /**
//     * 状态消息实体类
//     */
//    public static class StatusMessage {
//        private String submitId;
//        private String stage;
//        private int progress;
//        private String finalStatus;
//        private boolean completed;
//        private long timestamp;
//
//        // getter和setter方法
//        public String getSubmitId() { return submitId; }
//        public void setSubmitId(String submitId) { this.submitId = submitId; }
//
//        public String getStage() { return stage; }
//        public void setStage(String stage) { this.stage = stage; }
//
//        public int getProgress() { return progress; }
//        public void setProgress(int progress) { this.progress = progress; }
//
//        public String getFinalStatus() { return finalStatus; }
//        public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
//
//        public boolean isCompleted() { return completed; }
//        public void setCompleted(boolean completed) { this.completed = completed; }
//
//        public long getTimestamp() { return timestamp; }
//        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
//
//        @Override
//        public String toString() {
//            return "StatusMessage{" +
//                    "submitId='" + submitId + '\'' +
//                    ", stage='" + stage + '\'' +
//                    ", progress=" + progress +
//                    ", finalStatus='" + finalStatus + '\'' +
//                    ", completed=" + completed +
//                    ", timestamp=" + timestamp +
//                    '}';
//        }
//    }
}
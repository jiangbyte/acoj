package io.charlie.web.oj.modular.llm.controller;

import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.llm.param.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 30/06/2025
 * @description Spring AI 对话控制器
 */
@Tag(name = "AI控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class LLMController {
    private final ChatClient client;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 消息 {}", request.getProblemId(), request.getConversantId(), request.getMessage());

        // 根据消息类型设置不同的系统提示词
        String systemPrompt = getSystemPrompt(request.getMessageType(), request.getProblemId());
        PromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of(
                "question_id", request.getProblemId(),
                "user_code", request.getUserCode() != null ? request.getUserCode() : "",
                "language", request.getLanguage() != null ? request.getLanguage() : ""
        ));

        return client.prompt()
                .messages(systemMessage)
                .user(request.getMessage())
                .advisors(spec -> spec.param(CONVERSATION_ID, request.getConversantId()))
                .stream()
                .content()
                .map(content -> new ChatResponse(
                        request.getConversantId(),
                        content,
                        request.getMessageType(),
                        new Date()
                ));
    }

    /**
     * 根据消息类型获取对应的系统提示词
     */
    private String getSystemPrompt(String messageType, String problemId) {
        return switch (messageType) {
            case "generate_solution" -> """
                    你是一个编程专家，正在处理问题 ID 为 {question_id} 的编程题目。
                    请生成详细的解题思路，包括：
                    1. 问题分析和理解
                    2. 算法思路
                    3. 关键步骤
                    4. 复杂度分析
                    请使用专业但易懂的语言进行解释。
                    """;
            case "analyze_code" -> """
                    你是一个代码审查专家，正在分析问题 ID 为 {question_id} 的用户代码。
                    编程语言：{language}
                    用户代码：{user_code}
                    请从以下角度分析代码：
                    1. 正确性分析
                    2. 代码逻辑
                    3. 潜在问题
                    4. 改进建议
                    请提供具体、有针对性的反馈。
                    """;
            case "explain_complexity" -> """
                    你是一个算法专家，正在解释问题 ID 为 {question_id} 的时间复杂度。
                    请详细解释：
                    1. 算法的时间复杂度分析
                    2. 空间复杂度分析
                    3. 优化可能性
                    4. 不同情况下的性能表现
                    """;
            case "boundary_cases" -> """
                    你是一个测试专家，正在分析问题 ID 为 {question_id} 的边界情况。
                    请列出需要考虑的边界情况：
                    1. 输入边界值
                    2. 特殊数据结构情况
                    3. 极端性能情况
                    4. 常见的错误情况
                    """;
            case "optimize_code" -> """
                    你是一个性能优化专家，正在优化问题 ID 为 {question_id} 的代码。
                    编程语言：{language}
                    用户代码：{user_code}
                    请提供优化建议：
                    1. 性能优化点
                    2. 代码结构改进
                    3. 最佳实践建议
                    4. 重构方案
                    """;
            default -> // chat
                    "正在处理问题 ID 为 {question_id} 的请求，请以专业、友好的方式回答用户的问题。";
        };
    }
}

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
        PromptTemplate systemPromptTemplate = new SystemPromptTemplate("正在处理问题 ID 为 {question_id} 的请求");
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("question_id", request.getProblemId()));

        System.out.println(JSONUtil.toJsonPrettyStr(request));

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

//    @Operation(summary = "生成题目解题思路")
//    @PostMapping(value = "/generate/solution", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ChatResponse> generateSolution(@Valid @RequestBody ChatRequest request) {
//        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 描述 {}", request.getProblemId(), request.getConversantId(), request.getMessage());
//        PromptTemplate systemPromptTemplate = new SystemPromptTemplate("正在处理问题 ID 为 {question_id} 的请求");
//        Message systemMessage = systemPromptTemplate.createMessage(Map.of("question_id", request.getProblemId()));
//
//        return client.prompt()
//                .messages(systemMessage)
//                .user(request.getMessage())
//                .advisors(spec -> spec.param(CONVERSATION_ID, request.getConversantId()))
//                .stream()
//                .content()
//                .map(content -> new ChatResponse(
//                        request.getConversantId(),
//                        content,
//                        "text",
//                        new Date()
//                ));
//    }
//
//    @Operation(summary = "分析用户的代码")
//    @GetMapping(value = "/analyze/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ChatResponse> analyzeCode(@Valid @RequestBody ChatRequest request) {
//        log.info("开始处理问题 ID 为 {} 的请求，会话ID {}", request.getProblemId(), request.getConversantId());
//        PromptTemplate systemPromptTemplate = new SystemPromptTemplate("正在处理问题 ID 为 {question_id} 的请求");
//        Message systemMessage = systemPromptTemplate.createMessage(Map.of("question_id", request.getProblemId()));
//
//        return client.prompt()
//                .messages(systemMessage)
//                .user(request.getMessage())
//                .advisors(spec -> spec.param(CONVERSATION_ID, request.getConversantId()))
//                .stream()
//                .content()
//                .map(content -> new ChatResponse(
//                        request.getConversantId(),
//                        content,
//                        "text",
//                        new Date()
//                ));
//    }
}

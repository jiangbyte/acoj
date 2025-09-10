package io.charlie.app.core.modular.llm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;
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

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam(defaultValue = "你好，你是谁？") String message,
            @RequestParam(required = false) String problemId,
            @RequestParam @NotNull String conversantId
    ) {
        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 消息 {}", problemId, conversantId, message);

        SseEmitter emitter = new SseEmitter();
        executor.execute(() -> {
            try {
                PromptTemplate systemPromptTemplate = new SystemPromptTemplate("正在处理问题 ID 为 {question_id} 的请求");
                Message systemMessage = systemPromptTemplate.createMessage(Map.of("question_id", problemId));

                Flux<String> flux = client.prompt()
                        .messages(systemMessage)
                        .user(message)
                        .advisors(spec -> spec.param(CONVERSATION_ID, conversantId))
                        .stream()
                        .content();

                flux.subscribe(
                        content -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("chat")
                                        .data(content)
                                );
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}


//    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> stream(
//            @RequestParam(defaultValue = "你好，你是谁？") String message,
//            @RequestParam(required = false) String problemId,
//            @RequestParam @NotNull String conversantId
//    ) {
//        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 消息 {}", problemId, conversantId, message);
//        PromptTemplate systemPromptTemplate = new SystemPromptTemplate("正在处理问题 ID 为 {question_id} 的请求");
//        Message systemMessage = systemPromptTemplate.createMessage(Map.of("question_id", problemId));
//        return client.prompt()
//                // 添加系统消息
//                .messages(systemMessage)
//                .user(message)
//                .advisors(spec -> spec.param(CONVERSATION_ID, conversantId))
//                .stream()
//                .content();
//    }

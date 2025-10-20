package io.charlie.web.oj.modular.llm.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.llm.param.ChatResponse;
import io.charlie.web.oj.modular.llm.service.LLMService;
import io.charlie.web.oj.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.oj.modular.sys.conversation.service.SysConversationService;
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
    private final LLMService llmService;

    @Operation(summary = "流式对话")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamChat(@Valid @RequestBody ChatRequest request) {
        return llmService.streamChat(request);
    }

}

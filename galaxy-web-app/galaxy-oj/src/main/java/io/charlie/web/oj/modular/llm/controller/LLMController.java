package io.charlie.web.oj.modular.llm.controller;

import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.llm.service.LLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
    public Flux<?> streamChat(@Valid @RequestBody ChatRequest request) {
        return llmService.streamChat(request);
    }

}

package io.charlie.web.oj.modular.llm.service;

import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.llm.param.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/10/2025
 * @description TODO
 */
public interface LLMService {
    Flux<ChatResponse> streamChat(ChatRequest request);
}

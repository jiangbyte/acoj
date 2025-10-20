package io.charlie.web.oj.modular.llm.service.impl;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import io.charlie.web.oj.modular.llm.config.LLMProperties;
import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.llm.param.ChatResponse;
import io.charlie.web.oj.modular.llm.service.LLMService;
import io.charlie.web.oj.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.oj.modular.sys.conversation.service.SysConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/10/2025
 * @description LLM 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMServiceImpl implements LLMService {
    private final ChatClient chatClient;
    private final SysConversationService sysConversationService;
    private final ConfigurablePromptTemplateFactory promptTemplateFactory;

    @Override
    public Flux<ChatResponse> streamChat(ChatRequest request) {
        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 消息 {}", request.getProblemId(), request.getConversationId(), request.getMessage());

        ConfigurablePromptTemplate configurablePromptTemplate = promptTemplateFactory.getTemplate(request.getMessageType());

        if (configurablePromptTemplate == null) {
            return Flux.error(new RuntimeException("未找到对应的 prompt 模版"));
        }

        // 保存用户消息到数据库
        sysConversationService.saveConversation(request);

        Prompt prompt = configurablePromptTemplate.create(request.toMapExcludeNull());

        log.info("最终构建的 prompt 为：{}", prompt.getContents());

        StringBuilder fullResponse = new StringBuilder();
        return chatClient.prompt(prompt)
                .user(request.getMessage())
                .advisors(spec -> spec.param(CONVERSATION_ID, request.getConversationId()))
                .stream()
                .content()
                .map(content -> {
                    fullResponse.append(content);
                    return new ChatResponse(
                            request.getConversationId(),
                            content,
                            request.getMessageType(),
                            new Date()
                    );
                })
                .doOnComplete(() -> {
                    log.info("问题处理完成，会话记录ID: {}", request.getConversationId());
                    long endTime = System.currentTimeMillis();
                })
                .doOnError(error -> {
                    log.error("对话处理失败，会话记录ID: {}", request.getConversationId(), error);
                });
    }
}

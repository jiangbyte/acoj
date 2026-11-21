package io.charlie.web.modular.llm.service.impl;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.charlie.web.modular.llm.config.LLMProperties;
import io.charlie.web.modular.llm.config.LlmToolTraceContext;
import io.charlie.web.modular.llm.param.ChatRequest;
import io.charlie.web.modular.llm.result.PromptExportResult;
import io.charlie.web.modular.llm.service.LLMService;
import io.charlie.web.modular.sys.conversation.constant.MessageRole;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.service.SysConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Date;
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
    private final LLMProperties llmProperties;

    @Override
    public Flux<SysConversation> streamChat(ChatRequest chatRequest) {
        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 消息 {}", chatRequest.getProblemId(), chatRequest.getConversationId(), chatRequest.getMessageContent());

        // 1. 绑定工具轨迹上下文（供 ProblemTools 落库）
        String userId = chatRequest.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : null;
        }
        LlmToolTraceContext.set(chatRequest.getConversationId(), userId, chatRequest.getProblemId());

        // messageType 为空时直接走默认系统提示，避免 Nacos 模板工厂对 null key NPE
        ConfigurablePromptTemplate configurablePromptTemplate = null;
        if (chatRequest.getMessageType() != null && !chatRequest.getMessageType().isBlank()) {
            configurablePromptTemplate = promptTemplateFactory.getTemplate(chatRequest.getMessageType());
        }
        log.info("使用对应的 prompt 模版：{}", chatRequest.getMessageType());

        long startTime = System.currentTimeMillis();
        StringBuilder fullResponse = new StringBuilder();

        // 保存用户消息到数据库
        sysConversationService.saveUserConversation(chatRequest);

        Map<String, Object> mapExcludeNull = chatRequest.toMapExcludeNull();
        // 模板变量补空，避免 PromptTemplate 因缺变量失败
        mapExcludeNull.putIfAbsent("problemId", chatRequest.getProblemId() == null ? "" : chatRequest.getProblemId());
        mapExcludeNull.putIfAbsent("setId", chatRequest.getSetId() == null ? "" : chatRequest.getSetId());
        mapExcludeNull.putIfAbsent("problemSetId", chatRequest.getSetId() == null ? "" : chatRequest.getSetId());
        mapExcludeNull.putIfAbsent("language", chatRequest.getLanguage() == null ? "" : chatRequest.getLanguage());
        mapExcludeNull.putIfAbsent("userCode", chatRequest.getUserCode() == null ? "" : chatRequest.getUserCode());
        log.info("构建的参数为：{}", mapExcludeNull);

        ChatClient.ChatClientRequestSpec requestSpec;
        if (configurablePromptTemplate == null) {
            // 无 messageType：直接使用 ChatClient 默认系统提示（已含 hardened default-prompt）
            log.info("未找到该 prompt 模版，使用系统默认提示词");
            requestSpec = chatClient.prompt().user(chatRequest.getMessageContent());
        } else {
            Prompt prompt = configurablePromptTemplate.create(mapExcludeNull);
            log.info("最终构建的 prompt 为：{}", prompt.getContents());
            requestSpec = chatClient.prompt(prompt).user(chatRequest.getMessageContent());
        }

        return requestSpec
                .advisors(spec -> spec.param(CONVERSATION_ID, chatRequest.getConversationId()))
                .stream()
                .content().map(messageContent -> {
                    fullResponse.append(messageContent);
                    SysConversation sysConversation = new SysConversation();
                    sysConversation.setConversationId(chatRequest.getConversationId());
                    sysConversation.setMessageContent(messageContent);
                    sysConversation.setMessageType(chatRequest.getMessageType());
                    sysConversation.setMessageRole(MessageRole.ASSISTANT);
                    sysConversation.setUserPlatform(chatRequest.getUserPlatform());
                    sysConversation.setResponseTime(new Date());
                    return sysConversation;
                })
                .doOnComplete(() -> {
                    long endTime = System.currentTimeMillis();
                    log.info("问题处理完成，会话记录ID: {},，耗时: {}ms", chatRequest.getConversationId(), endTime - startTime);
                    sysConversationService.saveBotConversation(chatRequest, fullResponse.toString(), endTime - startTime, null);
                    LlmToolTraceContext.clear();
                })
                .doOnError(error -> {
                    long endTime = System.currentTimeMillis();
                    log.error("对话处理失败，会话记录ID: {}, 耗时: {}ms", chatRequest.getConversationId(), endTime - startTime, error);
                    sysConversationService.saveBotConversation(chatRequest, null, endTime - startTime, error.getMessage());
                    LlmToolTraceContext.clear();
                });
    }

    @Override
    public PromptExportResult exportPrompts() {
        // 1. 读取当前 default-prompt
        // 2. 计算摘要后返回
        PromptExportResult result = new PromptExportResult();
        String defaultPrompt = "";
        if (llmProperties.getPrompts() != null && llmProperties.getPrompts().getDefaultPrompt() != null) {
            defaultPrompt = llmProperties.getPrompts().getDefaultPrompt();
        }
        result.setDefaultPrompt(defaultPrompt);
        result.setDefaultPromptSha256(DigestUtil.sha256Hex(defaultPrompt));
        return result;
    }
}

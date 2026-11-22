package io.charlie.web.modular.llm.service.impl;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
    private static final String DEFAULT_MESSAGE_TYPE = "DailyConversation";

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

        // 2. 解析 messageType：空则回退 DailyConversation，保证普通聊天也走带上下文的 Nacos 模板
        String messageType = StrUtil.blankToDefault(chatRequest.getMessageType(), DEFAULT_MESSAGE_TYPE);
        chatRequest.setMessageType(messageType);
        ConfigurablePromptTemplate configurablePromptTemplate = promptTemplateFactory.getTemplate(messageType);
        if (configurablePromptTemplate == null && !DEFAULT_MESSAGE_TYPE.equals(messageType)) {
            configurablePromptTemplate = promptTemplateFactory.getTemplate(DEFAULT_MESSAGE_TYPE);
            log.warn("未找到 prompt 模版 {}，回退 {}", messageType, DEFAULT_MESSAGE_TYPE);
        }
        log.info("使用对应的 prompt 模版：{}", messageType);

        long startTime = System.currentTimeMillis();
        StringBuilder fullResponse = new StringBuilder();
        // 流式末包通常才带 usage，用引用保存最后一次非空用量
        AtomicReference<Usage> latestUsage = new AtomicReference<>();

        // 保存用户消息到数据库
        sysConversationService.saveUserConversation(chatRequest);

        // 3. 仅注入常驻 ID/代码变量；题面等详情由模型按 ID 调 Tool 查询
        Map<String, Object> mapExcludeNull = chatRequest.toMapExcludeNull();
        enrichPromptContext(chatRequest, mapExcludeNull);
        log.info("构建的参数为：{}", mapExcludeNull.keySet());

        ChatClient.ChatClientRequestSpec requestSpec;
        if (configurablePromptTemplate == null) {
            log.info("未找到任何 prompt 模版，使用系统默认提示词");
            requestSpec = chatClient.prompt().user(chatRequest.getMessageContent());
        } else {
            Prompt prompt = configurablePromptTemplate.create(mapExcludeNull);
            log.info("最终构建的 prompt 为：{}", prompt.getContents());
            requestSpec = chatClient.prompt(prompt).user(chatRequest.getMessageContent());
        }

        // 工具调用常在其他线程执行，ThreadLocal 会丢；通过 ToolContext 显式传递会话标识
        Map<String, Object> toolContext = Map.of(
                "conversationId", chatRequest.getConversationId() == null ? "" : chatRequest.getConversationId(),
                "userId", userId == null ? "" : userId,
                "problemId", chatRequest.getProblemId() == null ? "" : chatRequest.getProblemId()
        );

        return requestSpec
                .toolContext(toolContext)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatRequest.getConversationId()))
                .stream()
                // 用 chatResponse 才能拿到 metadata.usage；content() 会丢 token 信息
                .chatResponse()
                .doOnNext(response -> captureUsage(response, latestUsage))
                .mapNotNull(response -> {
                    String messageContent = extractText(response);
                    if (messageContent == null || messageContent.isEmpty()) {
                        return null;
                    }
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
                    Usage usage = latestUsage.get();
                    Integer promptTokens = usage == null ? null : usage.getPromptTokens();
                    Integer completionTokens = usage == null ? null : usage.getCompletionTokens();
                    Integer totalTokens = usage == null ? null : usage.getTotalTokens();
                    log.info("问题处理完成，会话记录ID: {}，耗时: {}ms，tokens prompt={}, completion={}, total={}",
                            chatRequest.getConversationId(), endTime - startTime, promptTokens, completionTokens, totalTokens);
                    sysConversationService.saveBotConversation(
                            chatRequest, fullResponse.toString(), endTime - startTime, null,
                            promptTokens, completionTokens, totalTokens);
                    LlmToolTraceContext.clear();
                })
                .doOnError(error -> {
                    long endTime = System.currentTimeMillis();
                    Usage usage = latestUsage.get();
                    log.error("对话处理失败，会话记录ID: {}, 耗时: {}ms", chatRequest.getConversationId(), endTime - startTime, error);
                    sysConversationService.saveBotConversation(
                            chatRequest, null, endTime - startTime, error.getMessage(),
                            usage == null ? null : usage.getPromptTokens(),
                            usage == null ? null : usage.getCompletionTokens(),
                            usage == null ? null : usage.getTotalTokens());
                    LlmToolTraceContext.clear();
                });
    }

    /**
     * 仅补齐常驻模板变量（ID/语言/用户代码）；有则注入，无则空串。题面详情交给 Tool。
     */
    private void enrichPromptContext(ChatRequest chatRequest, Map<String, Object> mapExcludeNull) {
        mapExcludeNull.putIfAbsent("problemId", StrUtil.blankToDefault(chatRequest.getProblemId(), ""));
        mapExcludeNull.putIfAbsent("setId", StrUtil.blankToDefault(chatRequest.getSetId(), ""));
        mapExcludeNull.putIfAbsent("problemSetId", StrUtil.blankToDefault(chatRequest.getSetId(), ""));
        mapExcludeNull.putIfAbsent("language", StrUtil.blankToDefault(chatRequest.getLanguage(), ""));
        mapExcludeNull.putIfAbsent("userCode", StrUtil.blankToDefault(chatRequest.getUserCode(), ""));
    }

    /**
     * 从流式响应中提取文本增量。
     */
    private static String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return null;
        }
        Generation generation = response.getResult();
        AssistantMessage output = generation.getOutput();
        if (output == null) {
            return null;
        }
        return output.getText();
    }

    /**
     * 记录流式过程中出现的 usage（通常末包才有）。
     */
    private static void captureUsage(ChatResponse response, AtomicReference<Usage> latestUsage) {
        if (response == null || response.getMetadata() == null) {
            return;
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage == null) {
            return;
        }
        Integer total = usage.getTotalTokens();
        Integer prompt = usage.getPromptTokens();
        Integer completion = usage.getCompletionTokens();
        if ((total != null && total > 0) || (prompt != null && prompt > 0) || (completion != null && completion > 0)) {
            latestUsage.set(usage);
        }
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

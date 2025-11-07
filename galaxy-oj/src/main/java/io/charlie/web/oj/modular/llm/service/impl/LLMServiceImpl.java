package io.charlie.web.oj.modular.llm.service.impl;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import io.charlie.web.oj.modular.llm.param.ChatRequest;
import io.charlie.web.oj.modular.llm.service.LLMService;
import io.charlie.web.oj.modular.sys.conversation.constant.MessageRole;
import io.charlie.web.oj.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.oj.modular.sys.conversation.service.SysConversationService;
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

    @Override
    public Flux<SysConversation> streamChat(ChatRequest chatRequest) {
        log.info("开始处理问题 ID 为 {} 的请求，会话ID {} 消息 {}", chatRequest.getProblemId(), chatRequest.getConversationId(), chatRequest.getMessageContent());

        ConfigurablePromptTemplate configurablePromptTemplate = promptTemplateFactory.getTemplate(chatRequest.getMessageType());
        log.info("使用对应的 prompt 模版：{}", chatRequest.getMessageType());

        long startTime = System.currentTimeMillis();
        StringBuilder fullResponse = new StringBuilder();

        // 保存用户消息到数据库
        sysConversationService.saveUserConversation(chatRequest);

        Map<String, Object> mapExcludeNull = chatRequest.toMapExcludeNull();
        log.info("构建的参数为：{}", mapExcludeNull);
        if (configurablePromptTemplate == null) {
            log.info("未找到该 prompt 模版，使用系统默认提示词");

            ConfigurablePromptTemplate promptTemplate = promptTemplateFactory.create(
                    "DailyConversation",
                    "你是一个友好的编程学习助手。请基于以下背景信息与用户进行日常对话：\n- 当前题目ID：{{problemId}}\n- 当前题集ID：{{problemSetId}}\n- 用户代码语言：{{language}}\n- 用户代码内容：{{userCode}}\n\n请以自然、友好的方式与用户交流，回答用户的各类问题。如果问题与编程、当前题目或代码相关，请充分利用这些背景信息提供专业回答；如果是其他领域的问题，请基于你的知识尽力帮助用户。保持对话流畅自然。"
            );
            Prompt prompt = promptTemplate.create(mapExcludeNull);
            log.info("最终构建的 prompt 为：{}", prompt.getContents());

            return chatClient.prompt(prompt)
                    .user(chatRequest.getMessageContent())
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
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("对话处理失败，会话记录ID: {}, 耗时: {}ms", chatRequest.getConversationId(), endTime - startTime, error);
                        sysConversationService.saveBotConversation(chatRequest, null, endTime - startTime, error.getMessage());
                    });
        }

        log.info("使用对应的 prompt 模版");
        // prompt 模板
        Prompt prompt = configurablePromptTemplate.create(mapExcludeNull);
        log.info("最终构建的 prompt 为：{}", prompt.getContents());

        return chatClient.prompt(prompt)
                .user(chatRequest.getMessageContent())
                .advisors(spec -> spec.param(CONVERSATION_ID, chatRequest.getConversationId()))
                .stream()
                .content().map(messageContent -> {
                    fullResponse.append(messageContent);
                    SysConversation sysConversation = new SysConversation();
                    sysConversation.setConversationId(chatRequest.getConversationId());
                    sysConversation.setMessageContent(messageContent);
                    sysConversation.setMessageType(chatRequest.getMessageType());
                    sysConversation.setMessageRole(MessageRole.ASSISTANT);
                    sysConversation.setResponseTime(new Date());
                    return sysConversation;
                })
                .doOnComplete(() -> {
                    long endTime = System.currentTimeMillis();
                    log.info("问题处理完成，会话记录ID: {},，耗时: {}ms", chatRequest.getConversationId(), endTime - startTime);
                    sysConversationService.saveBotConversation(chatRequest, fullResponse.toString(), endTime - startTime, null);
                })
                .doOnError(error -> {
                    long endTime = System.currentTimeMillis();
                    log.error("对话处理失败，会话记录ID: {}, 耗时: {}ms", chatRequest.getConversationId(), endTime - startTime, error);
                    sysConversationService.saveBotConversation(chatRequest, null, endTime - startTime, error.getMessage());
                });
    }
}

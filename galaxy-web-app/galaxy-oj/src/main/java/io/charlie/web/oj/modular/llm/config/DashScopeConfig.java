package io.charlie.web.oj.modular.llm.config;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 30/06/2025
 * @description AI 配置
 */
@Configuration
@RequiredArgsConstructor
public class DashScopeConfig {
    private final ProblemTools problemTool;
    private final Environment env;
    private final LLMProperties llmProperties;

    private final MysqlChatMemoryRepository mysqlChatMemoryRepository;

    private static final int MAX_MESSAGES = 100;

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(env.getProperty("spring.ai.dashscope.api-key")).build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(
                MessageWindowChatMemory
                        .builder()
//                        .chatMemoryRepository(new InMemoryChatMemoryRepository()) // 内存存储
                        .chatMemoryRepository(mysqlChatMemoryRepository) // mysql存储
                        .maxMessages(MAX_MESSAGES)
                        .build()
        ).build();

        DashScopeDocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(
                dashScopeApi(),
                DashScopeDocumentRetrieverOptions
                        .builder()
                        .withIndexName(llmProperties.getExternalKnowledgeBase().getIndexName())
                        .build());
        DocumentRetrievalAdvisor documentRetrievalAdvisor = new DocumentRetrievalAdvisor(dashScopeDocumentRetriever);

        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions
                .builder()
                .withTopP(0.7)
                .build();

        return builder
                .defaultSystem(llmProperties.getPrompts().getDefaultPrompt())
                .defaultAdvisors(new SimpleLoggerAdvisor(), messageChatMemoryAdvisor, documentRetrievalAdvisor)
                .defaultOptions(dashScopeChatOptions)
                .defaultTools(problemTool)
                .build();
    }
}

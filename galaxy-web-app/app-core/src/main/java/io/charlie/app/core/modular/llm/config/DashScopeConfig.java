package io.charlie.app.core.modular.llm.config;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 30/06/2025
 * @description AI 配置
 */
@Configuration
@RequiredArgsConstructor
public class DashScopeConfig {
    private final ResourceLoader resourceLoader;
    private final ProblemTools problemTool;
    private final Environment env;
    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(env.getProperty("spring.ai.dashscope.api-key")).build();
    }
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(resourceLoader.getResource("classpath:system-message.txt"))
                .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                                        .chatMemoryRepository(new InMemoryChatMemoryRepository()).maxMessages(100).build()).build(),
                        new DocumentRetrievalAdvisor(new DashScopeDocumentRetriever(dashScopeApi(),
                                DashScopeDocumentRetrieverOptions.builder().withIndexName("astro oj").build())))
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).defaultTools(problemTool).build();
    }
}

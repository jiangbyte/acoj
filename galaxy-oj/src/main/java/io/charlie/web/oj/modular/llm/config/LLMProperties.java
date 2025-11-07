package io.charlie.web.oj.modular.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/10/2025
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "oj.llm")
public class LLMProperties {

    // 提示词
    private Prompts prompts;

    // 外部知识库索引
    private ExternalKnowledgeBase externalKnowledgeBase;

    @Data
    public static class Prompts {
        // 默认提示
        private String defaultPrompt;
    }

    @Data
    public static class ExternalKnowledgeBase {
        private String indexName;
    }
}

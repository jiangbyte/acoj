package io.charlie.web.oj.modular.task.similarity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/10/2025
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "oj.similarity.config")
public class SimilarityConfigProperties {
    private Integer singleProblemLibrarySize = 50;
    private Integer singleSetProblemLibrarySize = 50;

    // 题目单提交分批大小
    private Integer problemSingleSubmitBatchSize = 500;
    // 题目单提交检测灵敏度
    private Integer problemSingleSubmitSensitivity = 5;
    // 题目单提交分批截止批次大小
    private Integer problemSingleSubmitBatchEndSize = 5;

    // 题目集内单提交分批大小
    private Integer setSingleSubmitBatchSize = 500;
    // 题目集内单提交检测灵敏度
    private Integer setSingleSubmitSensitivity = 5;

    // 自定义检测批次大小
    private Integer customDetectionBatchSize = 500;
    // 自定义检测灵敏度
    private Integer customDetectionSensitivity = 5;
}

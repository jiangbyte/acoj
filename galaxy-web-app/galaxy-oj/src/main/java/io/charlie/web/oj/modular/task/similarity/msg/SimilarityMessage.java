package io.charlie.web.oj.modular.task.similarity.msg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 */
@Data
public class SimilarityMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String taskId; // 任务ID
    private String submitId; // 提交ID

    private String setId; // 题集
    private String problemId; // 单个题目
    private Boolean isSet; // 是否是题集

    private String language; // 语言
    private Integer minMatchLength; // 敏感度
    private BigDecimal threshold; // 阈值

    @Schema(description = "手动")
    private Boolean taskType; // 是否手动

    private String code;
    private Integer codeLength;
    private List<Integer> codeTokens;
    private List<String> codeTokenNames;
    private List<String> codeTokenTexts;

    private String userId;
}

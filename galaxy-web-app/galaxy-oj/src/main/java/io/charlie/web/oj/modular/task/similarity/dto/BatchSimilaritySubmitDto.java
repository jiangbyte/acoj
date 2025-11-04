package io.charlie.web.oj.modular.task.similarity.dto;

import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 25/09/2025
 * @description 批量
 */
@Data
public class BatchSimilaritySubmitDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String reportId; // 统计ID

    private String taskId; // 任务ID

    private List<String> libIds; // 代码库ID

    // 敏感度
    private Integer minMatchLength;

    // 检测阈值
    private BigDecimal threshold;

    @Schema(description = "手动")
    private Boolean taskType;
}

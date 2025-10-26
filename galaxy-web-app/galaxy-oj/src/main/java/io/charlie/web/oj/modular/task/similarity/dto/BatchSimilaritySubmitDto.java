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

    private List<String> problemIds;
    private String setId;
    private String language;
    private Boolean isSet;
    private List<String> userIds;
    private String taskId;
    private Boolean isGroup;
    private String groupId;
    private String batchTaskId;
    private Integer minMatchLength;
    private BigDecimal threshold;

    @Schema(description = "手动")
    private Boolean taskType;
}

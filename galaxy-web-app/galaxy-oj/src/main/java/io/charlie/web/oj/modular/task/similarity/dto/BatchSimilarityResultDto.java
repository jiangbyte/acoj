package io.charlie.web.oj.modular.task.similarity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 相似度结果类
 */
@Data
public class BatchSimilarityResultDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private List<String> userId;

    @Schema(description = "题目ID")
    private List<String> problemId;

    @Schema(description = "题集ID")
    private List<String> setId;
    //
//    @Schema(description = "语言")
//    private String language;
//
//    @Schema(description = "代码")
//    private String code;
//
    @Schema(description = "是否是题集")
    private Boolean isSet;

    // 任务参数

    @Schema(description = "主键")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "手动")
    private Boolean taskType;
}

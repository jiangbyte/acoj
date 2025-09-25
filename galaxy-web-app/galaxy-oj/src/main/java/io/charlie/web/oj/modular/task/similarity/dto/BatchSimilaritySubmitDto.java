package io.charlie.web.oj.modular.task.similarity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
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

    // 提交参数

    @Schema(description = "用户ID")
    private List<String> userId;

    @Schema(description = "题目ID")
    private List<String> problemId;

    @Schema(description = "题集ID")
    private List<String> setId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "是否是题集")
    private Boolean isSet;

    // 任务参数

    @Schema(description = "主键")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "手动")
    private Boolean taskType;

    private Date createTime;


    @Schema(description = "任务ID")
    private String judgeTaskId;
}

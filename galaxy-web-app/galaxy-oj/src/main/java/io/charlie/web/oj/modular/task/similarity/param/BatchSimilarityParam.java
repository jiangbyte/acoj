package io.charlie.web.oj.modular.task.similarity.param;

import io.charlie.web.oj.modular.task.similarity.data.Config;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 26/09/2025
 * @description 批量检测参数
 */
@Data
public class BatchSimilarityParam {
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

    @Schema(description = "任务ID")
    private String taskId;

    private Date createTime;

    @Schema(description = "任务ID")
    private String batchTaskId;

    @Schema(description = "配置")
    private Config config;
}

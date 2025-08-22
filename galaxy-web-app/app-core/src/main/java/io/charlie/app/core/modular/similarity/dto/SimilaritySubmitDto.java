package io.charlie.app.core.modular.similarity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 克隆检测提交
 */
@Data
public class SimilaritySubmitDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 提交参数

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题集ID")
    private String problemSetId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "是否是题集")
    private Boolean isSet;

    // 任务参数

    @Schema(description = "主键")
    private String id;

}

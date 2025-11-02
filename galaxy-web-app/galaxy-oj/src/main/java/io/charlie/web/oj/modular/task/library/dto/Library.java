package io.charlie.web.oj.modular.task.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 31/10/2025
 * @description TODO
 */
@Data
public class Library implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String userId;
    private String submitId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "是否是题集")
    private Boolean isSet;


}

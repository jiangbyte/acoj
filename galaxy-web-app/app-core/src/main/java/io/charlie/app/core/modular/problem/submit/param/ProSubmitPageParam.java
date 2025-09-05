package io.charlie.app.core.modular.problem.submit.param;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 提交 分页参数
 */
@Data
@Schema(name = "ProSubmit", description = "提交 分页参数")
public class ProSubmitPageParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方式")
    private String sortOrder;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "题目")
    private String problem;

    @Schema(description = "题目")
    private String problemId;

    @Schema(description = "编程语言")
    private String language;

    @Schema(description = "执行类型")
    private Boolean submitType;

    @Schema(description = "执行状态")
    private String status;
}
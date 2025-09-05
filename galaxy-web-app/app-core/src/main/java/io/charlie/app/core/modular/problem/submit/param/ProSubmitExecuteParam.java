package io.charlie.app.core.modular.problem.submit.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 提交 增加参数
*/
@Data
@Schema(name = "ProSubmit", description = "提交 增加参数")
public class ProSubmitExecuteParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    @NotEmpty(message = "题目ID不能为空")
    private String problemId;

    @Schema(description = "语言")
    @NotEmpty(message = "语言不能为空")
    private String language;

    @Schema(description = "代码")
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "执行类型")
    @NotNull(message = "执行类型不能为空")
    private Boolean submitType;

}
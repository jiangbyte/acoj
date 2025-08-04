package io.charlie.app.core.modular.set.param.submit;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 题单提交参数
*/
@Data
@Schema(name = "ProSetSubmit", description = "题单提交参数")
public class ProSetSubmitAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题集ID")
    private String problemSetId;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "执行类型")
    private Boolean submitType;

}
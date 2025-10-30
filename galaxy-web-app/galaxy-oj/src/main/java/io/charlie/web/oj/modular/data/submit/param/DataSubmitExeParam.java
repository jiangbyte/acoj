package io.charlie.web.oj.modular.data.submit.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 提交参数
 */
@Data
@Schema(name = "DataSubmitExeParam", description = "提交参数")
public class DataSubmitExeParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    @NotEmpty(message = "任务ID不能为空")
    private String judgeTaskId;

//    @Schema(description = "用户ID")
//    private String userId;

    @Schema(description = "题目ID")
    @NotEmpty(message = "题目ID不能为空")
    private String problemId;

    @Schema(description = "题集ID")
    private String setId;

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

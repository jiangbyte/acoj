package io.charlie.app.core.modular.set.param.solved;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 用户题集解决记录表
*/
@Data
@Schema(name = "ProSetSolved", description = "用户题集解决记录表")
public class ProSetSolvedAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "题单ID")
    private String problemSetId;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "是否解决")
    private Boolean solved;

}
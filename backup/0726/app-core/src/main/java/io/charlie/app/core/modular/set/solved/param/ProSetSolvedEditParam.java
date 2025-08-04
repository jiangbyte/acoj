package io.charlie.app.core.modular.set.solved.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户题集解决记录 编辑参数
*/
@Data
@Schema(name = "ProSetSolved", description = "用户题集解决记录 编辑参数")
public class ProSetSolvedEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

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
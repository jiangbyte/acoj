package io.charlie.app.core.modular.set.progress.param;

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
* @description 题集进度 编辑参数
*/
@Data
@Schema(name = "ProSetProgress", description = "题集进度 编辑参数")
public class ProSetProgressEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题单ID")
    private String problemSetId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "是否完成")
    private Boolean completed;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

}
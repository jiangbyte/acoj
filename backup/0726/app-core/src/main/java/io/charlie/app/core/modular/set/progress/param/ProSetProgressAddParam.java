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
* @description 题集进度 增加参数
*/
@Data
@Schema(name = "ProSetProgress", description = "题集进度 增加参数")
public class ProSetProgressAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题单ID")
    private String problemSetId;

    @Schema(description = "进度信息")
    private String progress;

    @Schema(description = "是否完成")
    private Boolean completed;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

}
package io.charlie.web.oj.modular.data.progress.param;

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
* @date 2025-09-20
* @description 题集进度 增加参数
*/
@Data
@Schema(name = "DataProgress", description = "题集进度 增加参数")
public class DataProgressAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "额外信息")
    private String extraJson;

    @Schema(description = "是否完成")
    private Boolean completed;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

}
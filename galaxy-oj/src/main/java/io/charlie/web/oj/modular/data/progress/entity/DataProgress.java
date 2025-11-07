package io.charlie.web.oj.modular.data.progress.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 题集进度表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_progress")
@Schema(name = "DataProgress", description = "题集进度表")
public class DataProgress extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "模块类型")
    private String moduleType;
    @Schema(description = "模块ID")
    private String moduleId;

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

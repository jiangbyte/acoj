package io.charlie.web.oj.modular.data.solved.entity;

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
* @description 用户解决表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_solved")
@Schema(name = "DataSolved", description = "用户解决表")
public class DataSolved extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "题集ID")
    private String setId;

    @Schema(description = "是否是题集提交")
    private Boolean isSet;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "题目ID")
    private String problemId;

    @Schema(description = "提交ID")
    private String submitId;

    @Schema(description = "是否解决")
    private Boolean solved;
}

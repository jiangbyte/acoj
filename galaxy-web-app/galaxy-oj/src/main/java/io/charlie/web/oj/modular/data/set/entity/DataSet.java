package io.charlie.web.oj.modular.data.set.entity;

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
* @description 题集
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_set")
@Schema(name = "DataSet", description = "题集")
public class DataSet extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "题集类型")
    private Integer setType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类")
    private String categoryId;

    @Schema(description = "难度")
    private Integer difficulty;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否可见")
    private Boolean isVisible;

    @Schema(description = "是否使用AI")
    private Boolean useAi;

    @Schema(description = "额外的信息")
    private String exJson;

    @Schema(description = "排行")
    @TableField(exist = false)
    private Long rank;
}

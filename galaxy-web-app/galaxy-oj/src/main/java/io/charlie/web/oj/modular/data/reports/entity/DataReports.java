package io.charlie.web.oj.modular.data.reports.entity;

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
* @description 报告库表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_reports")
@Schema(name = "DataReports", description = "报告库表")
public class DataReports extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "报告类型")
    private Integer reportType;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "是否是题单报告")
    private Boolean isSet;

    @Schema(description = "报告ID")
    private String reportId;
}

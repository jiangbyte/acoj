package io.charlie.web.oj.modular.data.reports.param;

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
* @description 报告库 编辑参数
*/
@Data
@Schema(name = "DataReports", description = "报告库 编辑参数")
public class DataReportsEditParam implements Serializable {
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
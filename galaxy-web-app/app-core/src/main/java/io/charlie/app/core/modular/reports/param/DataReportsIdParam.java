package io.charlie.app.core.modular.reports.param;

import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-09
* @description 报告库
*/
@Data
@Schema(name = "DataReports", description = "报告库 ID参数")
public class DataReportsIdParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "id不能为空")
    private String id;

}
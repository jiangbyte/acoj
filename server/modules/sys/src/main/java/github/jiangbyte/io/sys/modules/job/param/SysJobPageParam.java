package github.jiangbyte.io.sys.modules.job.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "任务分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobPageParam extends PageQuery {

    @Schema(description = "任务名称（模糊）。")
    /** 任务名称（模糊）。 */
    private String name;

    @Schema(description = "触发类型：CRON / FIXED。")
    /** 触发类型：CRON / FIXED。 */
    private String triggerType;

    @Schema(description = "启用状态。")
    /** 启用状态。 */
    private Boolean enabled;
}

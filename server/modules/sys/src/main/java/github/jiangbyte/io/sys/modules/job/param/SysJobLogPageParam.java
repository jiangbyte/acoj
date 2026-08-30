package github.jiangbyte.io.sys.modules.job.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务执行记录分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "任务执行记录分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobLogPageParam extends PageQuery {

    @Schema(description = "任务 ID。")
    /** 任务 ID。 */
    private String jobId;

    @Schema(description = "执行结果：是否成功。")
    /** 执行结果：是否成功。 */
    private Boolean success;
}

package github.jiangbyte.io.sys.modules.job.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 编辑任务入参。
 *
 * Author: Charlie
 */
@Schema(description = "编辑任务入参。")
@Data
public class SysJobEditParam {

    @NotBlank
    @Schema(description = "主键ID")
    private String id;

    @NotBlank
    @Size(max = 128)
    @Schema(description = "名称")
    private String name;

    @Schema(description = "处理器标识（Boot 为 JobHandler 全限定类名，其他栈为注册 key）。")
    /** 处理器标识（Boot 为 JobHandler 全限定类名，其他栈为注册 key）。 */
    @NotBlank
    @Size(max = 255)
    private String handler;

    @Schema(description = "触发类型：CRON（表达式）/ FIXED（固定间隔）。")
    /** 触发类型：CRON（表达式）/ FIXED（固定间隔）。 */
    @NotBlank
    @Size(max = 16)
    private String triggerType;

    @Schema(description = "触发配置：CRON 表达式或固定间隔秒数。")
    /** 触发配置：CRON 表达式或固定间隔秒数。 */
    @NotBlank
    @Size(max = 255)
    private String triggerConfig;

    @Schema(description = "执行参数（JSON）。")
    /** 执行参数（JSON）。 */
    private Map<String, Object> params;

    @Schema(description = "任务描述。")
    /** 任务描述。 */
    @Size(max = 500)
    private String description;
    @Schema(description = "排序号（越小越靠前）")

    private Integer sort = 0;
    @Schema(description = "是否启用调度：1 启用 / 0 停用")

    private Boolean enabled = true;
}

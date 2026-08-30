package github.jiangbyte.io.sys.modules.job.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 任务定义实体，对应表 sys_job。
 *
 * Author: Charlie
 */
@Schema(description = "任务定义实体，对应表 sys_job。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_job", autoResultMap = true)
public class SysJob extends BaseEntity {

    @Schema(description = "任务名称。")
    /** 任务名称。 */
    private String name;

    @Schema(description = "处理器标识（Boot 为 JobHandler 全限定类名，其他栈为注册 key）。")
    /** 处理器标识（Boot 为 JobHandler 全限定类名，其他栈为注册 key）。 */
    private String handler;

    @Schema(description = "触发类型：CRON（表达式）/ FIXED（固定间隔）。")
    /** 触发类型：CRON（表达式）/ FIXED（固定间隔）。 */
    private String triggerType;

    @Schema(description = "触发配置：CRON 表达式或固定间隔秒数。")
    /** 触发配置：CRON 表达式或固定间隔秒数。 */
    private String triggerConfig;

    @Schema(description = "执行参数（JSON 存储）。")
    /** 执行参数（JSON 存储）。 */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> params;

    @Schema(description = "上次执行时间。")
    /** 上次执行时间。 */
    private OffsetDateTime lastRunTime;

    @Schema(description = "下次执行时间。")
    /** 下次执行时间。 */
    private OffsetDateTime nextRunTime;

    @Schema(description = "上次执行结果摘要。")
    /** 上次执行结果摘要。 */
    private String lastResult;

    @Schema(description = "启用状态。")
    /** 启用状态。 */
    private Boolean enabled;

    @Schema(description = "任务描述。")
    /** 任务描述。 */
    private String description;

    @Schema(description = "排序。")
    /** 排序。 */
    private Integer sort;
}

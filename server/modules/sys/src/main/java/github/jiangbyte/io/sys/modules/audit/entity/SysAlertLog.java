package github.jiangbyte.io.sys.modules.audit.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 告警日志实体，对应表 sys_alert_log。
 *
 * Author: Charlie
 */
@Schema(description = "告警日志实体，对应表 sys_alert_log。")
@Data
@TableName(value = "sys_alert_log", autoResultMap = true)
public class SysAlertLog {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "触发告警的规则名称")
    private String ruleName;
    @Schema(description = "严重级别：INFO/WARNING/CRITICAL")
    private String severity;
    @Schema(description = "告警摘要（展示用）")
    private String summary;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "告警详情上下文（JSON）")
    private Map<String, Object> details;
    @Schema(description = "通知渠道：email/webhook 等")
    private String notifiedVia;
    @Schema(description = "告警产生/通知时间")
    private OffsetDateTime createdAt;
}

package github.jiangbyte.io.workspace.modules.overview.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 工作台个人近期日志摘要项。
 *
 * Author: Charlie
 */
@Schema(description = "工作台个人近期日志摘要项。")
@Data
public class WorkspaceActivityItemResult {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "module")
    private String module;
    @Schema(description = "moduleLabel")
    private String moduleLabel;
    @Schema(description = "动作")
    private String action;
    @Schema(description = "actionName")
    private String actionName;
    @Schema(description = "actionType")
    private String actionType;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "是否成功：1 成功 / 0 失败")
    private Boolean success;
    @Schema(description = "客户端/实例 IP 地址")
    private String ip;
    @Schema(description = "客户端 User-Agent")
    private String userAgent;
    @Schema(description = "operatorName")
    private String operatorName;
    @Schema(description = "耗时（毫秒）")
    private Integer durationMs;
    @Schema(description = "resourceId")
    private String resourceId;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}

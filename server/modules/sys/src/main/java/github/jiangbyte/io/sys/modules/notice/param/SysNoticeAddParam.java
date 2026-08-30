package github.jiangbyte.io.sys.modules.notice.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建公告/通知的请求参数（类型、标题内容、目标范围、发布位置与状态等）。
 *
 * Author: Charlie
 */
@Schema(description = "创建公告/通知的请求参数（类型、标题内容、目标范围、发布位置与状态等）。")
@Data
public class SysNoticeAddParam {

    @NotBlank
    @Schema(description = "消息种类：NOTIFICATION（通知）/ ANNOUNCEMENT（公告）")
    private String kind;
    @NotBlank
    @Schema(description = "标题")
    private String title;
    @NotBlank
    @Schema(description = "内容")
    private String content;
    @Schema(description = "内容格式：TEXT/HTML/MARKDOWN 等")
    private String contentType = "TEXT";
    @Schema(description = "通知分类编码")
    private String category;
    @Schema(description = "重要等级：INFO/WARNING/ERROR 等")
    private String severity = "INFO";
    @Schema(description = "投放范围：ALL/ACCOUNT/DEPT/ROLE 等")
    private String targetScope = "ALL";
    @Schema(description = "目标账户类型列表（JSON 数组）")
    private List<String> targetAccountTypes = new ArrayList<>();
    @Schema(description = "目标账户ID列表（JSON 数组）")
    private List<String> targetAccountIds = new ArrayList<>();
    @Schema(description = "目标部门ID列表（JSON 数组）")
    private List<String> targetDeptIds = new ArrayList<>();
    @Schema(description = "目标角色ID列表（JSON 数组）")
    private List<String> targetRoleIds = new ArrayList<>();
    @Schema(description = "发布位置（公告）")
    private Map<String, Object> publishLocations = new HashMap<>();
    @Schema(description = "是否置顶（公告）")
    private Boolean isPinned = false;
    @Schema(description = "置顶截止时间")
    private OffsetDateTime pinnedUntil;
    @Schema(description = "来源业务模块标识")
    private String sourceType;
    @Schema(description = "来源业务记录ID")
    private String sourceId;
    @Schema(description = "发布状态：DRAFT/PUBLISHED/REVOKED 等")
    private String status;
    @Schema(description = "计划/实际发布时间")
    private OffsetDateTime publishAt;
    @Schema(description = "过期时间（公告有效截止）")
    private OffsetDateTime expireAt;
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra = Map.of();
}

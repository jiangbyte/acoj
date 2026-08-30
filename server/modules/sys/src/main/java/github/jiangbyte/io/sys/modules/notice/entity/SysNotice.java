package github.jiangbyte.io.sys.modules.notice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 公告/通知实体，对应表 {@code sys_notice}；含类型、目标范围、发布状态、置顶与阅读相关字段。
 *
 * Author: Charlie
 */
@Schema(description = "公告/通知实体，对应表 sys_notice；含类型、目标范围、发布状态、置顶与阅读相关字段。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_notice", autoResultMap = true)
public class SysNotice extends BaseEntity {
    @Schema(description = "消息种类：NOTIFICATION（通知）/ ANNOUNCEMENT（公告）")
    private String kind;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "内容格式：TEXT/HTML/MARKDOWN 等")
    private String contentType;
    @Schema(description = "通知分类编码")
    private String category;
    @Schema(description = "重要等级：INFO/WARNING/ERROR 等")
    private String severity;
    @Schema(description = "投放范围：ALL/ACCOUNT/DEPT/ROLE 等")
    private String targetScope;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "目标账户类型列表（JSON 数组）")
    private List<String> targetAccountTypes;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "目标账户ID列表（JSON 数组）")
    private List<String> targetAccountIds;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "目标部门ID列表（JSON 数组）")
    private List<String> targetDeptIds;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "目标角色ID列表（JSON 数组）")
    private List<String> targetRoleIds;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "发布位置（公告）")
    private Map<String, Object> publishLocations;
    @Schema(description = "是否置顶（公告）")
    private Boolean isPinned;
    @Schema(description = "置顶截止时间")
    private OffsetDateTime pinnedUntil;
    @Schema(description = "发送方账户类型")
    private String senderAccountType;
    @Schema(description = "发送方账户ID")
    private String senderAccountId;
    @Schema(description = "来源业务模块标识")
    private String sourceType;
    @Schema(description = "来源业务记录ID")
    private String sourceId;
    @Schema(description = "发布状态：DRAFT/PUBLISHED/REVOKED 等")
    private String status;
    @Schema(description = "计划/实际发布时间")
    private OffsetDateTime publishAt;
    @Schema(description = "撤回时间")
    private OffsetDateTime revokedAt;
    @Schema(description = "过期时间（公告有效截止）")
    private OffsetDateTime expireAt;
    @Schema(description = "浏览/查看次数")
    private Integer viewCount;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;

    @TableField(exist = false)
    @Schema(description = "当前用户是否已读")
    private Boolean isRead;
}

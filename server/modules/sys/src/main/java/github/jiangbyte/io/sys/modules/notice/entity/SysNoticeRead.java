package github.jiangbyte.io.sys.modules.notice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(
        value = "sys_notice_read",
        excludeProperty = {"createdAt", "createdBy", "updatedAt", "updatedBy"})
/**
 * 消息已读记录实体，对应表 {@code sys_notice_read}；按账号类型与账号 ID 记录已读时间。
 *
 * Author: Charlie
 */
@Schema(description = "消息已读记录实体，对应表 sys_notice_read；按账号类型与账号 ID 记录已读时间。")
public class SysNoticeRead extends BaseEntity {
    @Schema(description = "公告/通知ID（sys_notice.id）")
    private String noticeId;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "用户阅读时间")
    private OffsetDateTime readAt;
}

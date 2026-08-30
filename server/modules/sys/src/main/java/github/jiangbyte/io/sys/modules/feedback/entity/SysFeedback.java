package github.jiangbyte.io.sys.modules.feedback.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.sys.modules.feedback.result.SysFeedbackAttachmentResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户反馈实体，对应表 {@code sys_feedback}；含提交人、状态、回复与附件对象名等。
 *
 * Author: Charlie
 */
@Schema(description = "用户反馈实体，对应表 sys_feedback；含提交人、状态、回复与附件对象名等。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_feedback", autoResultMap = true)
public class SysFeedback extends BaseEntity {
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "联系方式")
    private String contact;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "用户上传附件 object_name 列表")
    private List<String> attachObjectNames;
    @Schema(description = "反馈状态：PENDING/REPLIED/CLOSED 等")
    private String status;
    @Schema(description = "管理员回复内容")
    private String reply;
    @Schema(description = "回复人账户ID")
    private String repliedBy;
    @Schema(description = "管理员回复时间")
    private OffsetDateTime repliedAt;
    @Schema(description = "提交人账户类型")
    private String submitterAccountType;
    @Schema(description = "提交人账户ID")
    private String submitterAccountId;

    @TableField(exist = false)
    @Schema(description = "attachments")
    private List<SysFeedbackAttachmentResult> attachments = new ArrayList<>();
    @TableField(exist = false)
    @Schema(description = "submitterAvatar")
    private String submitterAvatar;
    @TableField(exist = false)
    @Schema(description = "submitterNickname")
    private String submitterNickname;
}

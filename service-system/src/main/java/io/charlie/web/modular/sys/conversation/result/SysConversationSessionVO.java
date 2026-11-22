package io.charlie.web.modular.sys.conversation.result;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.cores.config.timestamp.DateToTimestampSerializer;
import io.charlie.cores.config.timestamp.TimestampToDateDeserializer;
import io.charlie.web.modular.data.problem.entity.DataProblem;
import io.charlie.web.modular.data.set.entity.DataSet;
import io.charlie.web.modular.sys.file.config.StorageAccessUrl;
import io.charlie.web.modular.sys.user.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 按 conversationId 聚合的会话摘要，用于管理端与用户历史列表。
 */
@Data
@JsonIgnoreProperties(value = "transMap")
@Schema(name = "SysConversationSessionVO", description = "大模型对话会话摘要")
public class SysConversationSessionVO implements Serializable, TransPojo {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Easy-Trans 要求存在名为 id 的主键字段；会话摘要以 conversationId 作为主键。
     */
    @Schema(description = "主键（同 conversationId）")
    private String id;

    @Schema(description = "对话ID")
    private String conversationId;

    @Schema(description = "题目ID")
    @Trans(type = TransType.SIMPLE, target = DataProblem.class, fields = "title", ref = "problemIdName")
    private String problemId;

    @Schema(description = "题目名称")
    @TableField(exist = false)
    private String problemIdName;

    @Schema(description = "题集ID")
    @Trans(type = TransType.SIMPLE, target = DataSet.class, fields = "title", ref = "setIdName")
    private String setId;

    @Schema(description = "题集名称")
    @TableField(exist = false)
    private String setIdName;

    @Schema(description = "是否是题集提交")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isSet;

    @Schema(description = "用户ID")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"userIdName", "userAvatar"})
    private String userId;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String userIdName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    @StorageAccessUrl
    private String userAvatar;

    @Schema(description = "消息条数")
    private Integer messageCount;

    @Schema(description = "最近消息类型")
    @Trans(type = TransType.DICTIONARY, key = "MESSAGE_TYPE")
    private String lastMessageType;

    @Schema(description = "会话状态 SUCCESS/ERROR")
    private String status;

    @Schema(description = "Token 汇总")
    private Integer totalTokens;

    @Schema(description = "流式耗时汇总毫秒")
    private Integer totalDurationMs;

    @Schema(description = "会话开始时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date startedAt;

    @Schema(description = "最近活跃时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date lastActiveAt;

    @Schema(description = "首条用户消息预览")
    private String preview;
}

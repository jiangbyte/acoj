package io.charlie.web.oj.modular.sys.log.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;

import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-17
* @description 系统活动/日志记录表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_log")
@Schema(name = "SysLog", description = "系统活动/日志记录表")
public class SysLog extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户ID")
    @Trans(type = TransType.SIMPLE, target = SysUser.class, fields = {"nickname", "avatar"}, refs = {"userIdName", "userAvatar"})
    private String userId;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String userIdName;

    @Schema(description = "用户头像")
    @TableField(exist = false)
    private String userAvatar;

    @Schema(description = "操作")
    private String operation;

    @Schema(description = "方法")
    private String method;

    @Schema(description = "参数")
    private String params;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "操作时间")
    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date operationTime;

    @Schema(description = "操作分类")
    private String category;

    @Schema(description = "模块名称")
    private String module;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作结果")
    private String status;

    @Schema(description = "操作消息")
    private String message;
}

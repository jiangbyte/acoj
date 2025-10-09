package io.charlie.web.oj.modular.sys.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 用户表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "sys_user", autoResultMap = true)
@Schema(name = "SysUser", description = "用户表")
public class SysUser extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户组")
    @Trans(type = TransType.SIMPLE, target = SysGroup.class, fields = "name", ref = "groupIdName")
    private String groupId;

    @Schema(description = "用户组名称")
    @TableField(exist = false)
    private String groupIdName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "背景图片")
    private String background;

    @Schema(description = "签名")
    private String quote;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "学号")
    private String studentNumber;

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "登录时间")
    private Date loginTime;

    // =====================
    @TableField(exist = false)
    private Double score;

    @TableField(exist = false)
    private Long rank;

    @TableField(exist = false)
    private Long submitCount;

    @TableField(exist = false)
    private Long solvedProblem;

    @TableField(exist = false)
    private Long tryProblem;

    @TableField(exist = false)
    private Long participatedSet;

    @TableField(exist = false)
    private Double activeScore;

    @TableField(exist = false)
    private List<ACRecord> acRecord;

    @TableField(exist = false)
    private List<String> assignRoles;
}

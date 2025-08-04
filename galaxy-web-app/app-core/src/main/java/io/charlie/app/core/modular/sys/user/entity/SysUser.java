package io.charlie.app.core.modular.sys.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.app.core.modular.sys.group.entity.SysGroup;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serial;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 用户表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
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
    @Trans(type = TransType.DICTIONARY, key = "SYS_GENDER", ref = "genderName")
    private Integer gender;

    @Schema(description = "性别名称")
    @TableField(exist = false)
    private String genderName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "登录时间")
    private Date loginTime;
}

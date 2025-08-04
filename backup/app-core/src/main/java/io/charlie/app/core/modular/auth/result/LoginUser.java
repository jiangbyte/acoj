package io.charlie.app.core.modular.auth.result;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.app.core.modular.group.entity.SysGroup;
import io.charlie.galaxy.pojo.CommonEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.TransPojo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-22
 * @description 登录用户
 */
@Data
@Schema(name = "LoginUser", description = "登录用户")
public class LoginUser implements Serializable, TransPojo {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户组")
    @Trans(type = TransType.SIMPLE, target = SysGroup.class, fields = "name", ref = "groupName")
    private String groupId;

    @Schema(description = "用户组名称")
    @TableField(exist = false)
    private String groupName;

    @Schema(description = "用户名")
    private String username;

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

    @Schema(description = "电话")
    private String telephone;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建用户")
    private String createUser;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新用户")
    private String updateUser;

}

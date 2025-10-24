package io.charlie.web.oj.modular.sys.user.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 用户 增加参数
*/
@Data
@Schema(name = "SysUser", description = "用户 增加参数")
public class SysUserAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户组")
    private String groupId;

    @Schema(description = "用户名")
    private String username;

//    @Schema(description = "密码")
//    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "背景图片")
    private String background;

    @Schema(description = "签名")
    private String quote;

    @Schema(description = "性别")
    private Boolean gender;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "学号")
    private String studentNumber;

    @Schema(description = "电话")
    private String telephone;

//    @Schema(description = "登录时间")
//    private LocalDateTime loginTime;

}
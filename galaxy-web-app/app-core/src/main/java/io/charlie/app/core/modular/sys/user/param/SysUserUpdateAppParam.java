package io.charlie.app.core.modular.sys.user.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 28/08/2025
 * @description app端用户更新参数
 */
@Data
public class SysUserUpdateAppParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

//    @Schema(description = "用户组")
//    private String groupId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

//    @Schema(description = "头像")
//    private String avatar;
//
//    @Schema(description = "背景图片")
//    private String background;

    @Schema(description = "签名")
    private String quote;

    @Schema(description = "学号")
    private String studentNumber;

    @Schema(description = "性别")
    private Boolean gender;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "电话")
    private String telephone;
}

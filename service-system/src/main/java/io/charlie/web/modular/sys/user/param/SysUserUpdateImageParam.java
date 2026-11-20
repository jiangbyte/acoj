package io.charlie.web.modular.sys.user.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class SysUserUpdateImageParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（可忽略，后端以登录用户为准）")
    private String id;

    @NotBlank(message = "图片不能为空")
    @Schema(description = "图片对象名或访问 URL")
    private String img;

}

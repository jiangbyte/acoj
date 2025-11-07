package io.charlie.web.oj.modular.sys.user.param;

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
public class SysUserUpdateImageParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "图片")
    private String img;

}

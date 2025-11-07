package io.charlie.web.oj.modular.sys.banner.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 横幅 增加参数
*/
@Data
@Schema(name = "SysBanner", description = "横幅 增加参数")
public class SysBannerJumpTargetParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "跳转模块")
    private String jumpModule;

    @Schema(description = "跳转类别")
    private String jumpType;

}
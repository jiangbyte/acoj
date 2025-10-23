package io.charlie.web.oj.modular.sys.banner.param;

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
* @description 横幅 增加参数
*/
@Data
@Schema(name = "SysBanner", description = "横幅 增加参数")
public class SysBannerAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "横幅")
    private String banner;

    @Schema(description = "按钮文字")
    private String buttonText;

    @Schema(description = "按钮是否可见")
    private Boolean isVisibleButton;

    @Schema(description = "跳转模块")
    private String jumpModule;

    @Schema(description = "跳转类别")
    private String jumpType;

    @Schema(description = "跳转目标")
    private String jumpTarget;

    @Schema(description = "新窗口打开")
    private Boolean targetBlank;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子标题")
    private String subtitle;

    @Schema(description = "子标题是否可见")
    private Boolean isVisibleSubtitle;

    @Schema(description = "是否可见")
    private Boolean isVisible;

}
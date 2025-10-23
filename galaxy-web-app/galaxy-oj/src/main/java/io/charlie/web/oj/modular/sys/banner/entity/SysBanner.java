package io.charlie.web.oj.modular.sys.banner.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
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
* @date 2025-09-20
* @description 横幅表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_banner")
@Schema(name = "SysBanner", description = "横幅表")
public class SysBanner extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "横幅")
    private String banner;

    @Schema(description = "按钮文字")
    private String buttonText;

    @Schema(description = "按钮是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisibleButton;

    @Schema(description = "跳转模块")
    @Trans(type = TransType.DICTIONARY, key = "JUMP_MODULE")
    private String jumpModule;

    @Schema(description = "跳转类别")
    @Trans(type = TransType.DICTIONARY, key = "JUMP_TYPE")
    private String jumpType;

    @Schema(description = "跳转目标")
    private String jumpTarget;

    @Schema(description = "新窗口打开")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean targetBlank;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子标题")
    private String subtitle;

    @Schema(description = "子标题是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisibleSubtitle;

    @Schema(description = "是否可见")
    @Trans(type = TransType.DICTIONARY, key = "YES_NO")
    private Boolean isVisible;

}

package io.charlie.app.core.modular.banner.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
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

    @Schema(description = "链接")
    private String toUrl;

    @Schema(description = "排序")
    private Byte sort;

    @Schema(description = "子标题")
    private String subtitle;
}

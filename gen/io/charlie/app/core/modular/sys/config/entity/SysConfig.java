package io.charlie.app.core.modular.sys.config.entity;

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

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 系统配置表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_config")
@Schema(name = "SysConfig", description = "系统配置表")
public class SysConfig extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "值")
    private String value;

    @Schema(description = "组件类型")
    private String componentType;

    @Schema(description = "描述")
    private String description;
}

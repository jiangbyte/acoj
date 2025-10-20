package io.charlie.web.oj.modular.sys.config.entity;

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

    @Schema(description = "配置类型")
    @Trans(type = TransType.DICTIONARY, key = "CONFIG_TYPE")
    private String configType;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "值")
    private String value;

    @Schema(description = "组件类型")
    @Trans(type = TransType.DICTIONARY, key = "COMPONENT_TYPE")
    private String componentType;

    @Schema(description = "描述")
    private String description;
}

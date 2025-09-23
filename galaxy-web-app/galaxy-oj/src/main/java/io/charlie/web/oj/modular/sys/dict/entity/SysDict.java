package io.charlie.web.oj.modular.sys.dict.entity;

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
* @date 2025-09-20
* @description 系统字典表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_dict")
@Schema(name = "SysDict", description = "系统字典表")
public class SysDict extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "类型名称")
    private String typeLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "排序")
    private Integer sortOrder;
}

package io.charlie.app.core.modular.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 字典数据表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_dict_data")
@Schema(name = "SysDictData", description = "字典数据表")
public class SysDictData extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "类型")
    @Trans(type = TransType.SIMPLE, target = SysDictType.class, fields = "typeName", ref = "typeIdName")
    private String typeId;

    @Schema(description = "类型名称")
    @TableField(exist = false)
    private String typeIdName;

    @Schema(description = "标签")
    private String dictLabel;

    @Schema(description = "值")
    private String dictValue;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "是否默认值")
    private Boolean isDefault;

}

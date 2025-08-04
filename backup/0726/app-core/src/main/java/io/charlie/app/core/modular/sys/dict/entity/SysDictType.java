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
* @description 字典类型表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_dict_type")
@Schema(name = "SysDictType", description = "字典类型表")
public class SysDictType extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String typeName;

    @Schema(description = "编码")
    private String typeCode;

    @Schema(description = "内置")
    private Boolean isSystem;

    @Schema(description = "父级")
    @Trans(type = TransType.SIMPLE, target = SysDictType.class, fields = "typeName", ref = "parentIdName")
    private String parentId;

    @Schema(description = "类型名称")
    @TableField(exist = false)
    private String parentIdName;

}

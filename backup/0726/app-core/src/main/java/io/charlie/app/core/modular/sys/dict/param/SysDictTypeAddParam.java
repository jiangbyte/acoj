package io.charlie.app.core.modular.sys.dict.param;

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
* @date 2025-07-25
* @description 字典类型 增加参数
*/
@Data
@Schema(name = "SysDictType", description = "字典类型 增加参数")
public class SysDictTypeAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "名称")
    private String typeName;

    @Schema(description = "编码")
    private String typeCode;

    @Schema(description = "内置")
    private Boolean isSystem;

    @Schema(description = "父级")
    private String parentId;

}
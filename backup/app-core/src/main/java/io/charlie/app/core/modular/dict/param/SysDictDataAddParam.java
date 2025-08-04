package io.charlie.app.core.modular.dict.param;

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
* @description 字典数据 增加参数
*/
@Data
@Schema(name = "SysDictData", description = "字典数据 增加参数")
public class SysDictDataAddParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "类型")
    private String typeId;

    @Schema(description = "标签")
    private String dictLabel;

    @Schema(description = "值")
    private String dictValue;

    @Schema(description = "父级")
    private String parentId;

}
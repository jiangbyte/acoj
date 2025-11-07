package io.charlie.web.oj.modular.sys.role.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 角色 编辑参数
*/
@Data
@Schema(name = "SysRole", description = "角色 编辑参数")
public class SysRoleEditParam implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "数据范围")
    private String dataScope;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分配用户组ID")
    private List<String> assignGroupIds;

}
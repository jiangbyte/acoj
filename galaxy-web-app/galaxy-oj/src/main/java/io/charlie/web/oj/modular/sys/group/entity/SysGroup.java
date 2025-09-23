package io.charlie.web.oj.modular.sys.group.entity;

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
* @description 用户组表
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_group")
@Schema(name = "SysGroup", description = "用户组表")
public class SysGroup extends CommonEntity {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户组")
    private String id;

    @Schema(description = "父级用户组")
    private String parentId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Byte sort;

    @Schema(description = "负责人")
    private String adminId;

    @Schema(description = "系统组")
    private Boolean groupType;
}

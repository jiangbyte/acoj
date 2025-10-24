package io.charlie.web.oj.modular.sys.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableName;
import io.charlie.galaxy.pojo.CommonEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.io.Serial;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 角色表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
@Schema(name = "SysRole", description = "角色表")
public class SysRole extends CommonEntity {
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
    @Trans(type = TransType.DICTIONARY, key = "DATA_SCOPE")
    private String dataScope;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "角色层级")
    private Integer level;

    @TableField(exist = false)
    private Boolean isOpen;

    @TableField(exist = false)
    private List<String> assignResource;
}

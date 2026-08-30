package github.jiangbyte.io.iam.modules.role.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.Map;

/**
 * 角色实体，对应表 sys_role。
 *
 * Author: Charlie
 */
@Schema(description = "角色实体，对应表 sys_role。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role", autoResultMap = true)
public class SysRole extends BaseEntity {
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "角色分类")
    private String category;
    @Schema(description = "角色作用域：GLOBAL/DEPT 等")
    private String scopeType;
    @Trans(
            type = TransType.SIMPLE,
            target = SysDept.class,
            fields = "name",
            ref = "ownerDeptName")
    @Schema(description = "所属部门ID（数据权限范围）")
    private String ownerDeptId;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "角色状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "是否内置角色：1 内置 / 0 自定义")
    private Boolean isBuiltin;
    @Schema(description = "角色描述")
    private String description;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;

    @TableField(exist = false)
    @Schema(description = "所属部门名称（展示）")
    private String ownerDeptName;
}

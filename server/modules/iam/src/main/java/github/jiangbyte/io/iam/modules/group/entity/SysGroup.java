package github.jiangbyte.io.iam.modules.group.entity;

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
 * 用户组实体，对应表 sys_group。
 *
 * Author: Charlie
 */
@Schema(description = "用户组实体，对应表 sys_group。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_group", autoResultMap = true)
public class SysGroup extends BaseEntity {
    @Schema(description = "用户组名称")
    private String name;
    @Trans(
            type = TransType.SIMPLE,
            target = SysDept.class,
            fields = "name",
            ref = "ownerDeptName")
    @Schema(description = "所属部门ID（数据权限范围）")
    private String ownerDeptId;
    @Schema(description = "用户组描述")
    private String description;
    @Schema(description = "用户组状态：ENABLED/DISABLED")
    private String status;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;

    @TableField(exist = false)
    @Schema(description = "所属部门名称（展示）")
    private String ownerDeptName;
}

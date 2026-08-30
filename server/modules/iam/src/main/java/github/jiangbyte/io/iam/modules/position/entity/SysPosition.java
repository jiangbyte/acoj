package github.jiangbyte.io.iam.modules.position.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.Map;

/**
 * 岗位实体，对应表 sys_position。
 *
 * Author: Charlie
 */
@Schema(description = "岗位实体，对应表 sys_position。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_position", autoResultMap = true)
public class SysPosition extends BaseEntity {
    @Schema(description = "职位名称")
    private String name;
    @Schema(description = "职位类别")
    private String category;
    @Trans(
            type = TransType.SIMPLE,
            target = SysDept.class,
            fields = "name",
            ref = "ownerDeptName")
    @Schema(description = "所属部门ID（数据权限范围）")
    private String ownerDeptId;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "是否虚拟组织：1 虚拟 / 0 实体")
    private Boolean isVirtual;
    @Schema(description = "职位状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "职位描述")
    private String description;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;

    @TableField(exist = false)
    @Schema(description = "所属部门名称（展示）")
    private String ownerDeptName;
}

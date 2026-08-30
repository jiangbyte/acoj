package github.jiangbyte.io.iam.modules.dept.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 部门实体，对应表 sys_dept；支持父子层级组织。
 *
 * Author: Charlie
 */
@Schema(description = "部门实体，对应表 sys_dept；支持父子层级组织。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dept", autoResultMap = true)
public class SysDept extends BaseEntity {
    @Trans(
            type = TransType.SIMPLE,
            target = SysDept.class,
            fields = "name",
            ref = "parentIdName")
    @Schema(description = "父级ID")
    private String parentId;
    @Trans(
            type = TransType.RPC,
            targetClassName = "github.jiangbyte.io.profile.modules.admin.entity.ProfileUserAdmin",
            fields = "nickname",
            ref = "masterName")
    @Schema(description = "部门主管账户ID")
    private String masterId;
    @Trans(
            type = TransType.RPC,
            targetClassName = "github.jiangbyte.io.profile.modules.admin.entity.ProfileUserAdmin",
            fields = "nickname",
            ref = "deputyMasterName")
    @Schema(description = "部门副主管账户ID")
    private String deputyMasterId;
    @Schema(description = "部门名称")
    private String name;
    @Schema(description = "部门类别/层级类型")
    private String category;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "是否虚拟组织：1 虚拟 / 0 实体")
    private Boolean isVirtual;
    @Schema(description = "部门状态：ENABLED/DISABLED")
    private String status;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private Map<String, Object> extra;

    @TableField(exist = false)
    @Schema(description = "父级名称（展示）")
    private String parentIdName;

    @TableField(exist = false)
    @Schema(description = "masterName")
    private String masterName;

    @TableField(exist = false)
    @Schema(description = "deputyMasterName")
    private String deputyMasterName;

    @TableField(exist = false)
    @Schema(description = "子节点列表")
    private List<SysDept> children = new ArrayList<>();
}

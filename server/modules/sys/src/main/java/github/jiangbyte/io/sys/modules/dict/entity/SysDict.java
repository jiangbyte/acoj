package github.jiangbyte.io.sys.modules.dict.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典实体，对应表 sys_dict。
 *
 * Author: Charlie
 */
@Schema(description = "数据字典实体，对应表 sys_dict。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    @Schema(description = "字典项编码（同父级下唯一）")
    private String code;
    @Schema(description = "字典项展示标签")
    private String label;
    @Schema(description = "字典项实际值")
    private String value;
    @Schema(description = "前端展示颜色")
    private String color;
    @Schema(description = "字典分类：SYSTEM（系统）/ BUSINESS（业务）")
    private String category;
    @Trans(
            type = TransType.SIMPLE,
            target = SysDict.class,
            fields = "label",
            ref = "parentIdName")
    @Schema(description = "父级字典项ID")
    private String parentId;
    @Schema(description = "字典项状态：ENABLED/DISABLED")
    private String status;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;

    @TableField(exist = false)
    @Schema(description = "父级名称（展示）")
    private String parentIdName;

    @TableField(exist = false)
    @Schema(description = "子节点列表")
    private List<SysDict> children = new ArrayList<>();
}

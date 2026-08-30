package github.jiangbyte.io.biz.modules.cg_test_catalog.entity;

/**
 * Catalog实体，对应表 {@code cg_test_catalog}。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import github.jiangbyte.io.common.mybatis.handler.JacksonJsonTypeHandler;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "代码生成测试-目录")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cg_test_catalog", autoResultMap = true)
public class CgTestCatalog extends BaseEntity {
    @Schema(description = "父级ID")
    private String parentId;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "排序号（越小越靠前）")
    private Integer sort;
    @Schema(description = "是否可见：1 可见 / 0 隐藏")
    private Boolean isVisible;
    @Schema(description = "图标标识")
    private String icon;
    @Schema(description = "描述说明")
    private String description;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
    @Schema(description = "所属部门ID（数据范围）")
    private String ownerDeptId;

    @TableField(exist = false)
    @Schema(description = "子节点列表")
    private List<CgTestCatalog> children = new ArrayList<>();
}

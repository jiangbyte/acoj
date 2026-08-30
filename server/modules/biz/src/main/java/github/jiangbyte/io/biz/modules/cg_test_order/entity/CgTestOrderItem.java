package github.jiangbyte.io.biz.modules.cg_test_order.entity;

/**
 * 订单明细实体，对应表 {@code cg_test_order_item}。
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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "代码生成测试-订单明细")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cg_test_order_item", autoResultMap = true)
public class CgTestOrderItem extends BaseEntity {
    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "SKU编码")
    private String skuCode;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "数量")
    private Integer quantity;
    @Schema(description = "单价")
    private BigDecimal unitPrice;
    @Schema(description = "发货时间")
    private OffsetDateTime shippedAt;
    @Schema(description = "是否赠品：1 是 / 0 否")
    private Boolean isGift;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "明细配置（JSON）")
    private java.util.Map<String, Object> itemConfig;
    @Schema(description = "备注说明")
    private String remark;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}

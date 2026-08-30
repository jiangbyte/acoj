package github.jiangbyte.io.biz.modules.cg_test_order.entity;

/**
 * Order实体，对应表 {@code cg_test_order}。
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

@Schema(description = "代码生成测试-订单")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cg_test_order", autoResultMap = true)
public class CgTestOrder extends BaseEntity {
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "客户手机号")
    private String customerPhone;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "下单时间")
    private OffsetDateTime orderedAt;
    @Schema(description = "支付时间")
    private OffsetDateTime paidAt;
    @Schema(description = "订单金额")
    private BigDecimal totalAmount;
    @Schema(description = "商品数量")
    private Integer itemCount;
    @Schema(description = "是否开票：1 需要 / 0 不需要")
    private Boolean needInvoice;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "发票配置（JSON）")
    private java.util.Map<String, Object> invoiceConfig;
    @Schema(description = "备注说明")
    private String remark;
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
    @Schema(description = "所属部门ID（数据范围）")
    private String ownerDeptId;
}

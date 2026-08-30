package github.jiangbyte.io.biz.modules.cg_test_order.param;

/**
 * 编辑Order入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CgTestOrder编辑入参")
@Data
public class CgTestOrderEditParam {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "主键ID")
    private String id;
    @NotBlank
    @Schema(description = "订单号")
    private String orderNo;
    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "客户名称")
    private String customerName;
    @NotBlank
    @Schema(description = "客户手机号")
    private String customerPhone;
    @NotBlank
    @Schema(description = "状态")
    private String status;
    @NotBlank
    @Schema(description = "类型")
    private String type;
    @Schema(description = "下单时间")
    private OffsetDateTime orderedAt;
    @Schema(description = "支付时间")
    private OffsetDateTime paidAt;
    @NotNull
    @Schema(description = "订单金额")
    private BigDecimal totalAmount;
    @NotNull
    @Schema(description = "商品数量")
    private Integer itemCount;
    @NotNull
    @Schema(description = "是否开票：1 需要 / 0 不需要")
    private Boolean needInvoice;
    @Schema(description = "发票配置（JSON）")
    private java.util.Map<String, Object> invoiceConfig;
    @NotBlank
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}

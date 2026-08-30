package github.jiangbyte.io.biz.modules.cg_test_order.param;

/**
 * 创建订单明细入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CgTestOrderItem创建入参")
@Data
public class CgTestOrderItemAddParam {
    @NotBlank
    @Schema(description = "订单ID")
    private String orderId;
    @NotBlank
    @Schema(description = "SKU编码")
    private String skuCode;
    @NotBlank
    @Schema(description = "名称")
    private String name;
    @NotBlank
    @Schema(description = "分类")
    private String category;
    @NotBlank
    @Schema(description = "状态")
    private String status;
    @NotNull
    @Schema(description = "数量")
    private Integer quantity;
    @NotNull
    @Schema(description = "单价")
    private BigDecimal unitPrice;
    @Schema(description = "发货时间")
    private OffsetDateTime shippedAt;
    @NotNull
    @Schema(description = "是否赠品：1 是 / 0 否")
    private Boolean isGift;
    @Schema(description = "明细配置（JSON）")
    private java.util.Map<String, Object> itemConfig;
    @NotBlank
    @Schema(description = "备注说明")
    private String remark;
    @Schema(description = "扩展信息（JSON）")
    private java.util.Map<String, Object> extra;
}

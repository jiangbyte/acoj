package github.jiangbyte.io.biz.modules.cg_test_order.param;

/**
 * 订单明细分页查询入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "CgTestOrderItem分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class CgTestOrderItemPageParam extends PageQuery {
    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "SKU编码")
    private String skuCode;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "状态")
    private String status;
}

package github.jiangbyte.io.biz.modules.cg_test_order.param;

/**
 * Order分页查询入参。
 *
 * Author: Charlie
 */

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "CgTestOrder分页查询入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class CgTestOrderPageParam extends PageQuery {
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "类型")
    private String type;
}

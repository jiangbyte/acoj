package github.jiangbyte.io.iam.modules.client.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户端模块分页查询入参。
 *
 * Author: Charlie
 */
@Schema(description = "客户端模块分页查询入参。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysClientModulePageParam extends PageQuery {
    @Schema(description = "名称")

    private String name;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "适用账户体系：ADMIN/PORTAL")
    private String accountType;
    @Schema(description = "模块状态：ENABLED/DISABLED")
    private String status;
}

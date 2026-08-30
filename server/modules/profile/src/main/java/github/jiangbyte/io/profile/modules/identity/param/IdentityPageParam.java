package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端已认证实名快照分页参数。
 *
 * Author: Charlie
 */
@Schema(description = "管理端已认证实名快照分页参数。")
@Data
@EqualsAndHashCode(callSuper = true)
public class IdentityPageParam extends PageQuery {
    @Schema(description = "状态")
    private String status;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "证件类型：ID_CARD/PASSPORT 等")
    private String documentType;
}

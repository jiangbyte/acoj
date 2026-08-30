package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端实名认证待审分页参数。
 *
 * Author: Charlie
 */
@Schema(description = "管理端实名认证待审分页参数。")
@Data
@EqualsAndHashCode(callSuper = true)
public class RealNameCaseReviewPageParam extends PageQuery {
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "账户ID")
    private String accountId;
}

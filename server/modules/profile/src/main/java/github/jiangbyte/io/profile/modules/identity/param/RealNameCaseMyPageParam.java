package github.jiangbyte.io.profile.modules.identity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的实名认证工单分页参数。
 *
 * Author: Charlie
 */
@Schema(description = "我的实名认证工单分页参数。")
@Data
@EqualsAndHashCode(callSuper = true)
public class RealNameCaseMyPageParam extends PageQuery {
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "状态")
    private String status;
}

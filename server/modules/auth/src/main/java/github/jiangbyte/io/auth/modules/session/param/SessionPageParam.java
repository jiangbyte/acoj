package github.jiangbyte.io.auth.modules.session.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 在线会话分页查询参数：可按账号类型、ID、账号名、IP 或关键字过滤。
 *
 * Author: Charlie
 */
@Schema(description = "在线会话分页查询参数：可按账号类型、ID、账号名、IP 或关键字过滤。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SessionPageParam extends PageQuery {
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "客户端/实例 IP 地址")
    private String ip;
    @Schema(description = "keyword")
    private String keyword;
}

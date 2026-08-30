package github.jiangbyte.io.iam.modules.account.param;

import io.swagger.v3.oas.annotations.media.Schema;
import github.jiangbyte.io.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号分页查询入参（关键字、状态、账号类型等过滤条件）。
 *
 * Author: Charlie
 */
@Schema(description = "账号分页查询入参（关键字、状态、账号类型等过滤条件）。")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAccountPageParam extends PageQuery {
    @Schema(description = "登录账号/用户名")

    private String account;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "phone")
    private String phone;
    @Schema(description = "email")
    private String email;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "账户状态：ACTIVE（正常）/ LOCKED（锁定）/ CANCELLED（已注销）")
    private String accountStatus;
}

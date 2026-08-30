package github.jiangbyte.io.iam.modules.client.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户端资源树查询入参（账号类型等过滤）。
 *
 * Author: Charlie
 */
@Schema(description = "客户端资源树查询入参（账号类型等过滤）。")
@Data
public class SysClientResourceTreeParam {
    @Schema(description = "所属模块ID")
    private String moduleId;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
}

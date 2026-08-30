package github.jiangbyte.io.auth.modules.session.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量强制下线账号会话的请求参数（按账号 ID + 账号类型）。
 *
 * Author: Charlie
 */
@Schema(description = "批量强制下线账号会话的请求参数（按账号 ID + 账号类型）。")
@Data
public class SessionExitParam {

    @NotEmpty
    @Valid
    @Schema(description = "targets")
    private List<Target> targets = new ArrayList<>();

    /**
     * 单个强制下线目标。
     */
    @Data
    public static class Target {
        @NotBlank
        @Schema(description = "账户ID")
        private String accountId;
        @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
        private String accountType;
    }
}

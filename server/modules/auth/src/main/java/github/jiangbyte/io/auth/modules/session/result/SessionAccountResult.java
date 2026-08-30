package github.jiangbyte.io.auth.modules.session.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个在线账号的会话汇总：账号资料、最近登录信息及下属 Token 列表。
 *
 * Author: Charlie
 */
@Schema(description = "单个在线账号的会话汇总：账号资料、最近登录信息及下属 Token 列表。")
@Data
public class SessionAccountResult {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "登录账号/用户名")
    private String account;
    @Schema(description = "账户类型：ADMIN（管理端）/ PORTAL（门户端）")
    private String accountType;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "nickname")
    private String nickname;
    @Schema(description = "avatar")
    private String avatar;
    @Schema(description = "latestLoginIp")
    private String latestLoginIp;
    @Schema(description = "latestLoginTime")
    private String latestLoginTime;
    @Schema(description = "tokenCount")
    private Integer tokenCount;
    @Schema(description = "firstLoginAt")
    private String firstLoginAt;
    @Schema(description = "latestActiveAt")
    private String latestActiveAt;
    @Schema(description = "clientIp")
    private String clientIp;
    @Schema(description = "deviceLabel")
    private String deviceLabel;
    @Schema(description = "tokens")
    private List<SessionTokenResult> tokens = new ArrayList<>();
}

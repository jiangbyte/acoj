package github.jiangbyte.io.iam.modules.account.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import github.jiangbyte.io.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * 三方登录绑定实体，对应表 sys_account_oauth_binding。
 *
 * Author: Charlie
 */
@Schema(description = "三方登录绑定实体，对应表 sys_account_oauth_binding。")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_account_oauth_binding")
public class SysAccountOauthBinding extends BaseEntity {
    @Schema(description = "账户ID")
    private String accountId;
    @Schema(description = "OAuth 提供方：wechat/github/google 等")
    private String provider;
    @Schema(description = "第三方平台 OpenID")
    private String openId;
    @Schema(description = "第三方平台 UnionID（跨应用统一标识）")
    private String unionId;
    @Schema(description = "第三方账号昵称快照")
    private String nickname;
    @Schema(description = "第三方账号头像 URL 快照")
    private String avatar;
    @Schema(description = "第三方原始资料（JSON）")
    private String rawProfile;
    @Schema(description = "与本系统账号绑定时间")
    private OffsetDateTime boundAt;
}

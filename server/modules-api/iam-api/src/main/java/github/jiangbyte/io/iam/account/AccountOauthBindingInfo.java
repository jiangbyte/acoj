package github.jiangbyte.io.iam.account;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 账号三方登录绑定快照。
 *
 * Author: Charlie
 */
@Data
public class AccountOauthBindingInfo {
    private String id;
    private String accountId;
    private String provider;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatar;
    private String rawProfile;
    private OffsetDateTime boundAt;
}

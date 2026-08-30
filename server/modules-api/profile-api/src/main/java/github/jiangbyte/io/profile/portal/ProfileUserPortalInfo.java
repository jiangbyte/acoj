package github.jiangbyte.io.profile.portal;

import lombok.Data;

/**
 * 门户端用户档案跨模块快照：昵称、头像与联系方式等展示字段。
 * {@code name} 保留供 IAM 展示，来源于 {@link github.jiangbyte.io.profile.ProfileIdentityApi}，不从 profile 表映射。
 *
 * Author: Charlie
 */
@Data
public class ProfileUserPortalInfo {
    private String accountId;
    private String name;
    private String nickname;
    private String avatar;
    private String signature;
    private String phone;
    private String email;
}

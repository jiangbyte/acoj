package github.jiangbyte.io.profile.admin;

import lombok.Data;

/**
 * 管理端用户档案跨模块快照：昵称、头像、联系方式与备注等展示字段。
 * {@code name} 保留供 IAM 账号详情展示，来源于 {@link github.jiangbyte.io.profile.ProfileIdentityApi}，不从 profile 表映射。
 *
 * Author: Charlie
 */
@Data
public class ProfileUserAdminInfo {
    private String accountId;
    private String name;
    private String nickname;
    private String avatar;
    private String signature;
    private String phone;
    private String email;
    private String remark;
}

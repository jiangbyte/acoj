package github.jiangbyte.io.profile;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 跨模块实名认证状态快照（管理端账号详情展示）。
 *
 * Author: Charlie
 */
@Data
public class ProfileIdentityStatusInfo {
    private String status;
    private String documentType;
    private String realNameMasked;
    private String documentNoMasked;
    private String verifyChannel;
    private String provider;
    private OffsetDateTime verifiedAt;
    private OffsetDateTime revokedAt;
}

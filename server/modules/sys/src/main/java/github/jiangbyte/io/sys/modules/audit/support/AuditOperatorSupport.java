package github.jiangbyte.io.sys.modules.audit.support;

import github.jiangbyte.io.common.log.audit.AuditEventMessage;
import github.jiangbyte.io.profile.admin.ProfileUserAdminApi;
import github.jiangbyte.io.profile.admin.ProfileUserAdminInfo;
import github.jiangbyte.io.profile.portal.ProfileUserPortalApi;
import github.jiangbyte.io.profile.portal.ProfileUserPortalInfo;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 审计操作人展示：写入昵称快照，查询时对历史数据批量回显。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class AuditOperatorSupport {

    private final ProfileUserAdminApi adminUserProfileApi;
    private final ProfileUserPortalApi portalUserProfileApi;

    /** 写入前解析操作人昵称快照（仅 nickname，不用 name）。 */
    public String snapshotOperatorName(AuditEventMessage event) {
        if (event == null) {
            return null;
        }
        if (StringUtils.hasText(event.getAccountId())) {
            String nickname = resolveNickname(event.getAccountId(), event.getAccountType());
            if (StringUtils.hasText(nickname)) {
                return nickname;
            }
        }
        return StringUtils.hasText(event.getOperatorName()) ? event.getOperatorName().trim() : null;
    }

    /** 分页/详情查询后，对缺失昵称的历史记录批量回显。 */
    public void enrichOperatorNames(Collection<SysOperationAuditLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        Set<String> adminIds = new HashSet<>();
        Set<String> portalIds = new HashSet<>();
        for (SysOperationAuditLog log : logs) {
            if (!needsNicknameBackfill(log)) {
                continue;
            }
            if (isPortalAccountType(log.getAccountType())) {
                portalIds.add(log.getAccountId());
            } else {
                adminIds.add(log.getAccountId());
            }
        }
        Map<String, String> nicknames = new HashMap<>();
        if (!adminIds.isEmpty()) {
            Map<String, ProfileUserAdminInfo> profiles = adminUserProfileApi.getProfiles(adminIds);
            profiles.forEach((accountId, profile) -> putNickname(nicknames, accountId, profile == null ? null : profile.getNickname()));
        }
        if (!portalIds.isEmpty()) {
            Map<String, ProfileUserPortalInfo> profiles = portalUserProfileApi.getProfiles(portalIds);
            profiles.forEach((accountId, profile) -> putNickname(nicknames, accountId, profile == null ? null : profile.getNickname()));
        }
        for (SysOperationAuditLog log : logs) {
            if (!needsNicknameBackfill(log)) {
                continue;
            }
            String nickname = nicknames.get(log.getAccountId());
            if (StringUtils.hasText(nickname)) {
                log.setOperatorName(nickname);
            }
        }
    }

    private String resolveNickname(String accountId, String accountType) {
        if (!StringUtils.hasText(accountId)) {
            return null;
        }
        if (isPortalAccountType(accountType)) {
            ProfileUserPortalInfo profile = portalUserProfileApi.getProfile(accountId);
            return profile == null ? null : profile.getNickname();
        }
        ProfileUserAdminInfo profile = adminUserProfileApi.getProfile(accountId);
        return profile == null ? null : profile.getNickname();
    }

    private static boolean needsNicknameBackfill(SysOperationAuditLog log) {
        if (log == null || !StringUtils.hasText(log.getAccountId())) {
            return false;
        }
        if (!StringUtils.hasText(log.getOperatorName())) {
            return true;
        }
        return log.getOperatorName().trim().equals(log.getAccountId().trim());
    }

    private static boolean isPortalAccountType(String accountType) {
        return accountType != null && "portal".equalsIgnoreCase(accountType.trim());
    }

    private static void putNickname(Map<String, String> target, String accountId, String nickname) {
        if (StringUtils.hasText(accountId) && StringUtils.hasText(nickname)) {
            target.put(accountId, nickname.trim());
        }
    }
}

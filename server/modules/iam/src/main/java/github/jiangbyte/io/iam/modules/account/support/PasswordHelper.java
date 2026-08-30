package github.jiangbyte.io.iam.modules.account.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountPasswordHistory;
import github.jiangbyte.io.iam.modules.account.mapper.SysAccountPasswordHistoryMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 密码辅助：编码校验、历史记录、过期判断与近期密码复用检测。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class PasswordHelper {

    private final PasswordEncoder passwordEncoder;
    private final SysAccountPasswordHistoryMapper historyMapper;

    /** 编码明文密码。 */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /** 校验明文与哈希。 */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 写入密码历史。
     * @param accountId 账号 id
     * @param rawPassword 明文密码
     * @param changedBy 操作人
     * @param reason 原因
     */
    public void recordHistory(String accountId, String rawPassword, String changedBy, String reason) {
        SysAccountPasswordHistory history = new SysAccountPasswordHistory();
        history.setAccountId(accountId);
        history.setPasswordHash(encode(rawPassword));
        history.setChangedBy(changedBy);
        history.setChangeReason(reason);
        history.setCreatedAt(OffsetDateTime.now());
        historyMapper.insert(history);
    }

    /** 判断密码是否过期。 */
    public boolean isPasswordExpired(String accountId, int expireDays) {
        if (expireDays <= 0) {
            return false;
        }
        Integer ageDays = getPasswordAgeDays(accountId);
        if (ageDays == null) {
            return false;
        }
        return ageDays >= expireDays;
    }

    /** 获取密码使用天数。 */
    public Integer getPasswordAgeDays(String accountId) {
        List<SysAccountPasswordHistory> list = historyMapper.selectList(Wrappers.<SysAccountPasswordHistory>lambdaQuery()
                .eq(SysAccountPasswordHistory::getAccountId, accountId)
                .orderByDesc(SysAccountPasswordHistory::getCreatedAt)
                .last("limit 1"));
        if (list.isEmpty() || list.getFirst().getCreatedAt() == null) {
            return null;
        }
        long days = java.time.Duration.between(list.getFirst().getCreatedAt(), OffsetDateTime.now()).toDays();
        return (int) Math.max(0, days);
    }

    /** 按账号批量删除密码历史。 */
    public void deleteHistory(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }
        historyMapper.delete(Wrappers.<SysAccountPasswordHistory>lambdaQuery()
                .in(SysAccountPasswordHistory::getAccountId, accountIds));
    }

    /** 是否命中近期历史密码。 */
    public boolean matchesRecentPassword(String accountId, String rawPassword, int limit) {
        if (limit <= 0 || accountId == null || rawPassword == null) {
            return false;
        }
        List<SysAccountPasswordHistory> list = historyMapper.selectList(
                Wrappers.<SysAccountPasswordHistory>lambdaQuery()
                        .eq(SysAccountPasswordHistory::getAccountId, accountId)
                        .orderByDesc(SysAccountPasswordHistory::getCreatedAt)
                        .last("limit " + limit));
        for (SysAccountPasswordHistory row : list) {
            if (matches(rawPassword, row.getPasswordHash())) {
                return true;
            }
        }
        return false;
    }
}

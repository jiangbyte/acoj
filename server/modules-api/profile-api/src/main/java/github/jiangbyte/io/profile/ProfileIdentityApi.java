package github.jiangbyte.io.profile;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 跨模块实名认证快照 API：展示名与姓名筛选。
 *
 * Author: Charlie
 */
public interface ProfileIdentityApi {

    /** 批量获取已认证真实姓名（明文，供内部展示）。 */
    Map<String, String> getVerifiedRealNames(Collection<String> accountIds);

    /** 按姓名模糊匹配已认证账号 ID。 */
    Set<String> findAccountIdsByRealName(String name);

    /** 账号是否已通过实名认证。 */
    boolean isVerified(String accountId);

    /** 读取账号实名认证快照（脱敏字段，供管理端详情展示）。 */
    ProfileIdentityStatusInfo getStatusForAccount(String accountId);
}

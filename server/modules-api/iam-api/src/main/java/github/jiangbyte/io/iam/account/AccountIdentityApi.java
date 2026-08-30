package github.jiangbyte.io.iam.account;

import java.util.Collection;
import java.util.Map;

/**
 * 窄化身份查询门面，供不得依赖 {@link AccountApi} 的调用方使用
 *（避免 Account ↔ UserProfile 的 Bean 循环依赖）。
 *
 * Author: Charlie
 */
public interface AccountIdentityApi {

    /** 判断账号是否已绑定指定类型身份。 */
    boolean hasIdentity(String accountId, String identityType);

    /** 批量解析账号登录名（ACCOUNT 身份 identifier）；缺失时回退 accountId。 */
    Map<String, String> getAccountIdentifiers(Collection<String> accountIds);
}

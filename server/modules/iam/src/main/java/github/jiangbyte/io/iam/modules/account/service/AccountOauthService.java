package github.jiangbyte.io.iam.modules.account.service;

import github.jiangbyte.io.iam.modules.account.entity.SysAccountOauthBinding;

import java.util.Collection;
import java.util.List;

/**
 * 三方登录绑定领域服务。
 *
 * Author: Charlie
 */
public interface AccountOauthService {

    SysAccountOauthBinding findByProviderOpenId(String provider, String openId);

    SysAccountOauthBinding findByWechatUnionId(String unionId);

    List<SysAccountOauthBinding> listByAccount(String accountId);

    /** 按账号 ID 批量查询绑定，供列表组装避免 N+1。 */
    List<SysAccountOauthBinding> listByAccountIds(Collection<String> accountIds);

    int countByAccount(String accountId);

    SysAccountOauthBinding upsertBinding(
            String accountId,
            String provider,
            String openId,
            String unionId,
            String nickname,
            String avatar,
            String rawProfileJson);

    void unbind(String accountId, String provider);

    void deleteByAccountIds(List<String> accountIds);
}

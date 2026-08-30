package github.jiangbyte.io.iam.account;

import java.util.Collection;
import java.util.List;

/**
 * 跨模块三方登录绑定门面。
 *
 * Author: Charlie
 */
public interface AccountOauthApi {

    /** 按提供商 + openId 查找绑定；无则 null。 */
    AccountOauthBindingInfo findByProviderOpenId(String provider, String openId);

    /**
     * 按微信 unionId 查找任一微信族绑定（WECHAT_OPEN / WECHAT_MP）；无则 null。
     */
    AccountOauthBindingInfo findByWechatUnionId(String unionId);

    /** 列出账号全部三方绑定。 */
    List<AccountOauthBindingInfo> listByAccount(String accountId);

    /** 按账号 ID 批量列出三方绑定。 */
    List<AccountOauthBindingInfo> listByAccountIds(Collection<String> accountIds);

    /** 绑定数量。 */
    int countByAccount(String accountId);

    /**
     * 新增或更新绑定（同账号同 provider 唯一；openId 全局唯一）。
     */
    AccountOauthBindingInfo upsertBinding(
            String accountId,
            String provider,
            String openId,
            String unionId,
            String nickname,
            String avatar,
            String rawProfileJson);

    /** 解绑指定提供商；不存在时静默成功。 */
    void unbind(String accountId, String provider);
}

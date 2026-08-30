package github.jiangbyte.io.iam.modules.account.provider;

import github.jiangbyte.io.iam.account.AccountOauthApi;
import github.jiangbyte.io.iam.account.AccountOauthBindingInfo;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountOauthBinding;
import github.jiangbyte.io.iam.modules.account.service.AccountOauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * {@link AccountOauthApi} 适配器。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AccountOauthApiProvider implements AccountOauthApi {

    private final AccountOauthService accountOauthService;

    @Override
    public AccountOauthBindingInfo findByProviderOpenId(String provider, String openId) {
        return toInfo(accountOauthService.findByProviderOpenId(provider, openId));
    }

    @Override
    public AccountOauthBindingInfo findByWechatUnionId(String unionId) {
        return toInfo(accountOauthService.findByWechatUnionId(unionId));
    }

    @Override
    public List<AccountOauthBindingInfo> listByAccount(String accountId) {
        return accountOauthService.listByAccount(accountId).stream().map(this::toInfo).toList();
    }

    @Override
    public List<AccountOauthBindingInfo> listByAccountIds(Collection<String> accountIds) {
        return accountOauthService.listByAccountIds(accountIds).stream().map(this::toInfo).toList();
    }

    @Override
    public int countByAccount(String accountId) {
        return accountOauthService.countByAccount(accountId);
    }

    @Override
    public AccountOauthBindingInfo upsertBinding(
            String accountId,
            String provider,
            String openId,
            String unionId,
            String nickname,
            String avatar,
            String rawProfileJson) {
        return toInfo(accountOauthService.upsertBinding(
                accountId, provider, openId, unionId, nickname, avatar, rawProfileJson));
    }

    @Override
    public void unbind(String accountId, String provider) {
        accountOauthService.unbind(accountId, provider);
    }

    private AccountOauthBindingInfo toInfo(SysAccountOauthBinding entity) {
        if (entity == null) {
            return null;
        }
        AccountOauthBindingInfo info = new AccountOauthBindingInfo();
        info.setId(entity.getId());
        info.setAccountId(entity.getAccountId());
        info.setProvider(entity.getProvider());
        info.setOpenId(entity.getOpenId());
        info.setUnionId(entity.getUnionId());
        info.setNickname(entity.getNickname());
        info.setAvatar(entity.getAvatar());
        info.setRawProfile(entity.getRawProfile());
        info.setBoundAt(entity.getBoundAt());
        return info;
    }
}

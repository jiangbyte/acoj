package github.jiangbyte.io.iam.modules.account.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountOauthBinding;
import github.jiangbyte.io.iam.modules.account.mapper.SysAccountOauthBindingMapper;
import github.jiangbyte.io.iam.modules.account.service.AccountOauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * {@link AccountOauthService} 实现。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AccountOauthServiceImpl implements AccountOauthService {

    private static final List<String> WECHAT_PROVIDERS = List.of("WECHAT_OPEN", "WECHAT_MP");

    private final SysAccountOauthBindingMapper bindingMapper;

    @Override
    public SysAccountOauthBinding findByProviderOpenId(String provider, String openId) {
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(openId)) {
            return null;
        }
        return bindingMapper.selectOne(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .eq(SysAccountOauthBinding::getProvider, normalizeProvider(provider))
                .eq(SysAccountOauthBinding::getOpenId, openId.trim())
                .last("limit 1"));
    }

    @Override
    public SysAccountOauthBinding findByWechatUnionId(String unionId) {
        if (!StringUtils.hasText(unionId)) {
            return null;
        }
        return bindingMapper.selectOne(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .in(SysAccountOauthBinding::getProvider, WECHAT_PROVIDERS)
                .eq(SysAccountOauthBinding::getUnionId, unionId.trim())
                .last("limit 1"));
    }

    @Override
    public List<SysAccountOauthBinding> listByAccount(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return List.of();
        }
        return bindingMapper.selectList(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .eq(SysAccountOauthBinding::getAccountId, accountId)
                .orderByAsc(SysAccountOauthBinding::getProvider));
    }

    @Override
    public List<SysAccountOauthBinding> listByAccountIds(Collection<String> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)) {
            return List.of();
        }
        List<String> ids = accountIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        List<SysAccountOauthBinding> all = new ArrayList<>();
        int batchSize = 500;
        for (int i = 0; i < ids.size(); i += batchSize) {
            List<String> batch = ids.subList(i, Math.min(i + batchSize, ids.size()));
            all.addAll(bindingMapper.selectList(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                    .in(SysAccountOauthBinding::getAccountId, batch)
                    .orderByAsc(SysAccountOauthBinding::getAccountId)
                    .orderByAsc(SysAccountOauthBinding::getProvider)));
        }
        return all;
    }

    @Override
    public int countByAccount(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return 0;
        }
        Long count = bindingMapper.selectCount(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .eq(SysAccountOauthBinding::getAccountId, accountId));
        return count == null ? 0 : count.intValue();
    }

    @Override
    @Transactional
    public SysAccountOauthBinding upsertBinding(
            String accountId,
            String provider,
            String openId,
            String unionId,
            String nickname,
            String avatar,
            String rawProfileJson) {
        if (!StringUtils.hasText(accountId) || !StringUtils.hasText(provider) || !StringUtils.hasText(openId)) {
            throw new BizException("三方绑定参数不完整");
        }
        String normalizedProvider = normalizeProvider(provider);
        String normalizedOpenId = openId.trim();

        SysAccountOauthBinding byOpenId = findByProviderOpenId(normalizedProvider, normalizedOpenId);
        if (byOpenId != null && !accountId.equals(byOpenId.getAccountId())) {
            throw new BizException("该三方账号已绑定其他用户");
        }

        SysAccountOauthBinding existing = bindingMapper.selectOne(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .eq(SysAccountOauthBinding::getAccountId, accountId)
                .eq(SysAccountOauthBinding::getProvider, normalizedProvider)
                .last("limit 1"));

        OffsetDateTime now = OffsetDateTime.now();
        if (existing == null) {
            SysAccountOauthBinding created = new SysAccountOauthBinding();
            created.setAccountId(accountId);
            created.setProvider(normalizedProvider);
            created.setOpenId(normalizedOpenId);
            created.setUnionId(blankToNull(unionId));
            created.setNickname(nickname);
            created.setAvatar(avatar);
            created.setRawProfile(StringUtils.hasText(rawProfileJson) ? rawProfileJson : "{}");
            created.setBoundAt(now);
            bindingMapper.insert(created);
            return created;
        }

        if (!normalizedOpenId.equals(existing.getOpenId())) {
            SysAccountOauthBinding conflict = findByProviderOpenId(normalizedProvider, normalizedOpenId);
            if (conflict != null && !existing.getId().equals(conflict.getId())) {
                throw new BizException("该三方账号已绑定其他用户");
            }
        }
        existing.setOpenId(normalizedOpenId);
        existing.setUnionId(blankToNull(unionId));
        existing.setNickname(nickname);
        existing.setAvatar(avatar);
        if (StringUtils.hasText(rawProfileJson)) {
            existing.setRawProfile(rawProfileJson);
        }
        existing.setBoundAt(now);
        bindingMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void unbind(String accountId, String provider) {
        if (!StringUtils.hasText(accountId) || !StringUtils.hasText(provider)) {
            return;
        }
        bindingMapper.delete(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .eq(SysAccountOauthBinding::getAccountId, accountId)
                .eq(SysAccountOauthBinding::getProvider, normalizeProvider(provider)));
    }

    @Override
    @Transactional
    public void deleteByAccountIds(List<String> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)) {
            return;
        }
        bindingMapper.delete(Wrappers.<SysAccountOauthBinding>lambdaQuery()
                .in(SysAccountOauthBinding::getAccountId, accountIds));
    }

    private static String normalizeProvider(String provider) {
        return provider.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

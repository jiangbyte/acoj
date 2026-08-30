package github.jiangbyte.io.iam.modules.account.provider;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.iam.account.AccountIdentityApi;
import github.jiangbyte.io.iam.modules.account.entity.SysAccountIdentity;
import github.jiangbyte.io.iam.modules.account.mapper.SysAccountIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跨模块账号身份查询 API 适配器：判断账号是否具备某类身份标识。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class AccountIdentityApiProvider implements AccountIdentityApi {

    private final SysAccountIdentityMapper identityMapper;

    @Override
    public boolean hasIdentity(String accountId, String identityType) {
        Long count = identityMapper.selectCount(Wrappers.<SysAccountIdentity>lambdaQuery()
                .eq(SysAccountIdentity::getAccountId, accountId)
                .eq(SysAccountIdentity::getIdentityType, identityType)
                .eq(SysAccountIdentity::getBindStatus, "BOUND"));
        return count != null && count > 0;
    }

    @Override
    public Map<String, String> getAccountIdentifiers(Collection<String> accountIds) {
        Map<String, String> map = new HashMap<>();
        if (accountIds == null || accountIds.isEmpty()) {
            return map;
        }
        for (String accountId : accountIds) {
            if (StringUtils.hasText(accountId)) {
                map.put(accountId, accountId);
            }
        }
        for (List<String> batch : BatchPartition.partition(accountIds.stream().filter(StringUtils::hasText).toList())) {
            identityMapper.selectList(Wrappers.<SysAccountIdentity>lambdaQuery()
                            .in(SysAccountIdentity::getAccountId, batch)
                            .eq(SysAccountIdentity::getIdentityType, "ACCOUNT")
                            .eq(SysAccountIdentity::getBindStatus, "BOUND")
                            .orderByDesc(SysAccountIdentity::getIsPrimary)
                            .orderByAsc(SysAccountIdentity::getId))
                    .forEach(identity -> map.putIfAbsent(identity.getAccountId(), identity.getIdentifier()));
        }
        return map;
    }
}

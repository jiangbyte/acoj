package github.jiangbyte.io.profile.modules.identity.provider;

import github.jiangbyte.io.profile.ProfileIdentityApi;
import github.jiangbyte.io.profile.ProfileIdentityStatusInfo;
import github.jiangbyte.io.profile.modules.identity.service.ProfileIdentityService;
import github.jiangbyte.io.profile.modules.identity.result.IdentityStatusResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 跨模块 {@link ProfileIdentityApi} 适配器。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ProfileIdentityApiProvider implements ProfileIdentityApi {

    private final ProfileIdentityService profileIdentityService;

    @Override
    public Map<String, String> getVerifiedRealNames(Collection<String> accountIds) {
        return profileIdentityService.getVerifiedRealNames(accountIds);
    }

    @Override
    public Set<String> findAccountIdsByRealName(String name) {
        return profileIdentityService.findAccountIdsByRealName(name);
    }

    @Override
    public boolean isVerified(String accountId) {
        return profileIdentityService.isVerified(accountId);
    }

    @Override
    public ProfileIdentityStatusInfo getStatusForAccount(String accountId) {
        IdentityStatusResult status = profileIdentityService.getStatusForAccount(accountId);
        ProfileIdentityStatusInfo info = new ProfileIdentityStatusInfo();
        info.setStatus(status.getStatus());
        info.setDocumentType(status.getDocumentType());
        info.setRealNameMasked(status.getRealNameMasked());
        info.setDocumentNoMasked(status.getDocumentNoMasked());
        info.setVerifyChannel(status.getVerifyChannel());
        info.setProvider(status.getProvider());
        info.setVerifiedAt(status.getVerifiedAt());
        info.setRevokedAt(status.getRevokedAt());
        return info;
    }
}

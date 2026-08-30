package github.jiangbyte.io.profile.modules.identity.support;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.profile.modules.identity.service.IdentityVerifyProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 按 provider code 或通道路由 {@link IdentityVerifyProvider}。
 *
 * Author: Charlie
 */
@Component
public class IdentityVerifyProviderRegistry {

    private final List<IdentityVerifyProvider> providers;

    public IdentityVerifyProviderRegistry(List<IdentityVerifyProvider> providers) {
        this.providers = providers;
    }

    public IdentityVerifyProvider resolve(String verifyChannel, String documentType, String preferredProvider) {
        if (StringUtils.hasText(preferredProvider)) {
            for (IdentityVerifyProvider provider : providers) {
                if (preferredProvider.equalsIgnoreCase(provider.providerCode())) {
                    return provider;
                }
            }
            throw new BizException("Unsupported identity provider: " + preferredProvider);
        }
        for (IdentityVerifyProvider provider : providers) {
            if ("MOCK".equals(provider.providerCode())) {
                continue;
            }
            if (provider.supports(verifyChannel, documentType)) {
                return provider;
            }
        }
        for (IdentityVerifyProvider provider : providers) {
            if (provider.supports(verifyChannel, documentType)) {
                return provider;
            }
        }
        throw new BizException("No identity provider for channel=" + verifyChannel);
    }
}

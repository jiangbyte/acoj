package github.jiangbyte.io.profile.modules.identity.provider;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.enums.VerifyChannel;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseCallbackParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseInitThirdPartyParam;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseInitResult;
import github.jiangbyte.io.profile.modules.identity.service.IdentityVerifyProvider;
import org.springframework.stereotype.Component;

/**
 * 通用第三方实人认证 Provider 占位实现（Phase 2 接入支付宝/腾讯云等）。
 *
 * Author: Charlie
 */
@Component
public class ThirdPartyIdentityVerifyProvider implements IdentityVerifyProvider {

    @Override
    public String providerCode() {
        return "THIRD_PARTY";
    }

    @Override
    public boolean supports(String verifyChannel, String documentType) {
        return VerifyChannel.THIRD_PARTY.name().equalsIgnoreCase(verifyChannel);
    }

    @Override
    public RealNameCaseInitResult initVerify(RealNameCase caseEntity, RealNameCaseInitThirdPartyParam param) {
        throw new BizException("Third-party identity provider is not configured");
    }

    @Override
    public void handleCallback(RealNameCase caseEntity, RealNameCaseCallbackParam param) {
        throw new BizException("Third-party identity provider is not configured");
    }
}

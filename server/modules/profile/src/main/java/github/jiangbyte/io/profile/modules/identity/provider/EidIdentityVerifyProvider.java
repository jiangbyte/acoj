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
 * 电子身份证（网证）Provider 占位实现（Phase 3）。
 *
 * Author: Charlie
 */
@Component
public class EidIdentityVerifyProvider implements IdentityVerifyProvider {

    @Override
    public String providerCode() {
        return "EID";
    }

    @Override
    public boolean supports(String verifyChannel, String documentType) {
        return VerifyChannel.EID.name().equalsIgnoreCase(verifyChannel)
                || "EID".equalsIgnoreCase(documentType);
    }

    @Override
    public RealNameCaseInitResult initVerify(RealNameCase caseEntity, RealNameCaseInitThirdPartyParam param) {
        throw new BizException("EID identity provider is not configured");
    }

    @Override
    public void handleCallback(RealNameCase caseEntity, RealNameCaseCallbackParam param) {
        throw new BizException("EID identity provider is not configured");
    }
}

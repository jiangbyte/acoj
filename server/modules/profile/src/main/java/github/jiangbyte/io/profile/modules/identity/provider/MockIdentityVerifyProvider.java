package github.jiangbyte.io.profile.modules.identity.provider;

import cn.hutool.core.util.IdUtil;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.enums.VerifyChannel;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseCallbackParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseInitThirdPartyParam;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseInitResult;
import github.jiangbyte.io.profile.modules.identity.service.IdentityVerifyProvider;
import org.springframework.stereotype.Component;

/**
 * Mock 第三方实人认证 Provider（开发/测试用）。
 *
 * Author: Charlie
 */
@Component
public class MockIdentityVerifyProvider implements IdentityVerifyProvider {

    @Override
    public String providerCode() {
        return "MOCK";
    }

    @Override
    public boolean supports(String verifyChannel, String documentType) {
        return VerifyChannel.THIRD_PARTY.name().equalsIgnoreCase(verifyChannel);
    }

    @Override
    public RealNameCaseInitResult initVerify(RealNameCase caseEntity, RealNameCaseInitThirdPartyParam param) {
        RealNameCaseInitResult result = new RealNameCaseInitResult();
        result.setCaseId(caseEntity.getCaseId());
        result.setProvider(providerCode());
        result.setProviderOrderNo("MOCK-" + IdUtil.simpleUUID());
        result.setRedirectUrl("/mock/identity-verify?caseId=" + caseEntity.getCaseId());
        return result;
    }

    @Override
    public void handleCallback(RealNameCase caseEntity, RealNameCaseCallbackParam param) {
        // 回调结果由 RealNameCaseService 统一处理状态流转
    }
}

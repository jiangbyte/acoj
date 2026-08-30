package github.jiangbyte.io.profile.modules.identity.service;

import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseCallbackParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseInitThirdPartyParam;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseInitResult;

/**
 * 第三方实人认证 Provider 抽象。
 *
 * Author: Charlie
 */
public interface IdentityVerifyProvider {

    /** Provider 标识，如 {@code MOCK}、{@code ALIPAY}。 */
    String providerCode();

    /** 是否支持指定通道与证件类型。 */
    boolean supports(String verifyChannel, String documentType);

    /** 发起第三方认证并返回跳转信息。 */
    RealNameCaseInitResult initVerify(RealNameCase caseEntity, RealNameCaseInitThirdPartyParam param);

    /** 处理第三方回调。 */
    void handleCallback(RealNameCase caseEntity, RealNameCaseCallbackParam param);
}

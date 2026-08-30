package github.jiangbyte.io.profile.modules.identity.service;

import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseSubmitParam;

/**
 * 实名业务类型插件：校验提交与审核通过/驳回后的领域动作。
 *
 * Author: Charlie
 */
public interface RealNameBusinessHandler {

    /** 支持的业务类型 code，如 {@code ACCOUNT_VERIFY}。 */
    String businessType();

    /** 提交前业务校验。 */
    void validateSubmit(String accountId, RealNameCaseSubmitParam param);

    /** 审核通过后回调。 */
    void onApproved(RealNameCase caseEntity, String reviewerId);

    /** 审核驳回后回调。 */
    void onRejected(RealNameCase caseEntity, String reviewerId, String reason);
}

package github.jiangbyte.io.profile.modules.identity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseApproveParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseCallbackParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseInitThirdPartyParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseMyPageParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseRejectParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseReviewPageParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseSubmitParam;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseDetailResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseInitResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseOptionsResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseSummaryResult;

/**
 * 实名业务工单领域服务。
 *
 * Author: Charlie
 */
public interface RealNameCaseService {

    RealNameCaseOptionsResult options();

    void submit(RealNameCaseSubmitParam param);

    RealNameCaseInitResult initThirdParty(RealNameCaseInitThirdPartyParam param);

    void callback(RealNameCaseCallbackParam param);

    Page<RealNameCaseSummaryResult> myPage(RealNameCaseMyPageParam param);

    Page<RealNameCaseSummaryResult> reviewPage(RealNameCaseReviewPageParam param);

    RealNameCaseDetailResult detail(String caseId);

    void approve(RealNameCaseApproveParam param);

    void reject(RealNameCaseRejectParam param);
}

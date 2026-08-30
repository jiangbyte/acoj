package github.jiangbyte.io.profile.modules.identity.support;

import github.jiangbyte.io.profile.modules.identity.result.IdentityStatusResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseSummaryResult;

/**
 * 用户侧实名视图脱敏：隐藏审核通道、他人不可见的姓名等字段。
 *
 * Author: Charlie
 */
public final class IdentityUserViewSupport {

    private IdentityUserViewSupport() {
    }

    public static IdentityStatusResult sanitizeStatus(IdentityStatusResult source) {
        if (source == null) {
            return null;
        }
        source.setVerifyChannel(null);
        source.setProvider(null);
        sanitizeSummary(source.getPendingCase());
        return source;
    }

    public static RealNameCaseSummaryResult sanitizeSummary(RealNameCaseSummaryResult summary) {
        if (summary == null) {
            return null;
        }
        summary.setVerifyChannel(null);
        summary.setRealNameMasked(null);
        summary.setDocumentNoMasked(null);
        return summary;
    }
}

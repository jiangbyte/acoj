package github.jiangbyte.io.profile.modules.identity.handler;

import github.jiangbyte.io.profile.modules.identity.entity.ProfileIdentity;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.enums.IdentitySnapshotStatus;
import github.jiangbyte.io.profile.modules.identity.enums.RealNameBusinessType;
import github.jiangbyte.io.profile.modules.identity.enums.RealNameCaseStatus;
import github.jiangbyte.io.profile.modules.identity.mapper.ProfileIdentityMapper;
import github.jiangbyte.io.profile.modules.identity.mapper.RealNameCaseMapper;
import github.jiangbyte.io.profile.modules.identity.service.IdentityCryptoService;
import github.jiangbyte.io.profile.modules.identity.service.ProfileIdentityService;
import github.jiangbyte.io.profile.modules.identity.service.RealNameBusinessHandler;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseSubmitParam;
import github.jiangbyte.io.common.core.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 账号实名认证业务 Handler：校验提交条件，审核通过后写入 {@code profile_identity}。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class AccountVerifyHandler implements RealNameBusinessHandler {

    private final ProfileIdentityMapper profileIdentityMapper;
    private final RealNameCaseMapper realNameCaseMapper;
    private final IdentityCryptoService identityCryptoService;
    @Lazy
    private final ProfileIdentityService profileIdentityService;

    @Override
    public String businessType() {
        return RealNameBusinessType.ACCOUNT_VERIFY.name();
    }

    @Override
    public void validateSubmit(String accountId, RealNameCaseSubmitParam param) {
        var identity = profileIdentityMapper.selectById(accountId);
        if (identity != null && IdentitySnapshotStatus.VERIFIED.name().equals(identity.getStatus())) {
            throw new BizException("账号已完成实名认证");
        }
        Long pending = realNameCaseMapper.selectCount(Wrappers.<RealNameCase>lambdaQuery()
                .eq(RealNameCase::getAccountId, accountId)
                .eq(RealNameCase::getBusinessType, businessType())
                .eq(RealNameCase::getStatus, RealNameCaseStatus.PENDING.name()));
        if (pending != null && pending > 0) {
            throw new BizException("已有进行中的实名认证申请");
        }
        assertDocumentAvailable(param.getDocumentType(), param.getDocumentNo(), accountId);
    }

    @Override
    public void onApproved(RealNameCase caseEntity, String reviewerId) {
        profileIdentityService.upsertOnApprove(caseEntity, reviewerId);
    }

    @Override
    public void onRejected(RealNameCase caseEntity, String reviewerId, String reason) {
        // 认证驳回无需额外领域动作
    }

    void assertDocumentAvailable(String documentType, String documentNo, String excludeAccountId) {
        if (!StringUtils.hasText(documentNo)) {
            throw new BizException("证件号码不能为空");
        }
        String hash = identityCryptoService.hashDocumentNo(documentType, documentNo);
        var bound = profileIdentityMapper.selectOne(Wrappers.<ProfileIdentity>lambdaQuery()
                .eq(ProfileIdentity::getDocumentNoHash, hash)
                .eq(ProfileIdentity::getStatus, IdentitySnapshotStatus.VERIFIED.name())
                .ne(StringUtils.hasText(excludeAccountId), ProfileIdentity::getAccountId, excludeAccountId)
                .last("LIMIT 1"));
        if (bound != null) {
            throw new BizException("该证件已被其他账号绑定");
        }
        var pendingCase = realNameCaseMapper.selectOne(Wrappers.<RealNameCase>lambdaQuery()
                .eq(RealNameCase::getDocumentNoHash, hash)
                .eq(RealNameCase::getStatus, RealNameCaseStatus.PENDING.name())
                .ne(StringUtils.hasText(excludeAccountId), RealNameCase::getAccountId, excludeAccountId)
                .last("LIMIT 1"));
        if (pendingCase != null) {
            throw new BizException("该证件已有进行中的认证申请");
        }
    }
}

package github.jiangbyte.io.profile.modules.identity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.profile.modules.identity.entity.ProfileIdentity;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.enums.IdentitySnapshotStatus;
import github.jiangbyte.io.profile.modules.identity.enums.RealNameCaseStatus;
import github.jiangbyte.io.profile.modules.identity.mapper.ProfileIdentityMapper;
import github.jiangbyte.io.profile.modules.identity.mapper.RealNameCaseMapper;
import github.jiangbyte.io.profile.modules.identity.param.IdentityPageParam;
import github.jiangbyte.io.profile.modules.identity.param.IdentityRevokeParam;
import github.jiangbyte.io.profile.modules.identity.result.IdentityPageResult;
import github.jiangbyte.io.profile.modules.identity.result.IdentityStatusResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseSummaryResult;
import github.jiangbyte.io.profile.modules.identity.service.IdentityCryptoService;
import github.jiangbyte.io.profile.modules.identity.service.ProfileIdentityService;
import github.jiangbyte.io.profile.modules.identity.support.IdentityUserViewSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link ProfileIdentityService} 实现。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ProfileIdentityServiceImpl implements ProfileIdentityService {

    private final ProfileIdentityMapper profileIdentityMapper;
    private final RealNameCaseMapper realNameCaseMapper;
    private final IdentityCryptoService identityCryptoService;

    @Override
    @ReadDataSource
    public IdentityStatusResult getStatusForAccount(String accountId) {
        IdentityStatusResult result = new IdentityStatusResult();
        ProfileIdentity identity = profileIdentityMapper.selectById(accountId);
        if (identity == null) {
            result.setStatus(IdentitySnapshotStatus.UNVERIFIED.name());
        } else {
            result.setStatus(identity.getStatus());
            result.setDocumentType(identity.getDocumentType());
            result.setVerifyChannel(identity.getVerifyChannel());
            result.setProvider(identity.getProvider());
            result.setVerifiedAt(identity.getVerifiedAt());
            result.setRevokedAt(identity.getRevokedAt());
            if (StringUtils.hasText(identity.getRealNameCipher())) {
                result.setRealNameMasked(identityCryptoService.maskRealName(
                        identityCryptoService.decrypt(identity.getRealNameCipher())));
            }
            if (StringUtils.hasText(identity.getDocumentNoCipher())) {
                result.setDocumentNoMasked(identityCryptoService.maskDocumentNo(
                        identityCryptoService.decrypt(identity.getDocumentNoCipher())));
            }
        }
        RealNameCase pending = realNameCaseMapper.selectOne(Wrappers.<RealNameCase>lambdaQuery()
                .eq(RealNameCase::getAccountId, accountId)
                .eq(RealNameCase::getStatus, RealNameCaseStatus.PENDING.name())
                .orderByDesc(RealNameCase::getCreatedAt)
                .last("LIMIT 1"));
        if (pending != null) {
            result.setPendingCase(toSummary(pending));
        }
        return result;
    }

    @Override
    @ReadDataSource
    public IdentityStatusResult getUserStatusForAccount(String accountId) {
        return IdentityUserViewSupport.sanitizeStatus(getStatusForAccount(accountId));
    }

    @Override
    @Transactional
    public void upsertOnApprove(RealNameCase caseEntity, String reviewerId) {
        ProfileIdentity identity = profileIdentityMapper.selectById(caseEntity.getAccountId());
        if (identity == null) {
            identity = new ProfileIdentity();
            identity.setAccountId(caseEntity.getAccountId());
        } else {
            AuditSnapshots.before(identity);
        }
        identity.setStatus(IdentitySnapshotStatus.VERIFIED.name());
        identity.setDocumentType(caseEntity.getDocumentType());
        identity.setRealNameCipher(caseEntity.getRealNameCipher());
        identity.setDocumentNoCipher(caseEntity.getDocumentNoCipher());
        identity.setDocumentNoHash(caseEntity.getDocumentNoHash());
        identity.setVerifyChannel(caseEntity.getVerifyChannel());
        identity.setProvider(caseEntity.getProvider());
        identity.setProviderOrderNo(caseEntity.getProviderOrderNo());
        identity.setVerifiedAt(OffsetDateTime.now());
        identity.setSourceCaseId(caseEntity.getCaseId());
        identity.setRevokedAt(null);
        identity.setRevokedBy(null);
        if (identity.getCreatedAt() == null) {
            profileIdentityMapper.insert(identity);
            AuditSnapshots.created(identity);
        } else {
            profileIdentityMapper.updateById(identity);
            AuditSnapshots.after(identity);
        }
    }

    @Override
    @Transactional
    public void revoke(IdentityRevokeParam param, String operatorId) {
        ProfileIdentity identity = profileIdentityMapper.selectById(param.getAccountId());
        if (identity == null || !IdentitySnapshotStatus.VERIFIED.name().equals(identity.getStatus())) {
            throw new BizException(404, "Verified identity not found");
        }
        AuditSnapshots.before(identity);
        identity.setStatus(IdentitySnapshotStatus.REVOKED.name());
        identity.setRevokedAt(OffsetDateTime.now());
        identity.setRevokedBy(operatorId);
        profileIdentityMapper.updateById(identity);
        AuditSnapshots.after(identity);
    }

    @Override
    @ReadDataSource
    public Page<IdentityPageResult> page(IdentityPageParam param) {
        Page<ProfileIdentity> entityPage = profileIdentityMapper.selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<ProfileIdentity>lambdaQuery()
                        .eq(StringUtils.hasText(param.getStatus()), ProfileIdentity::getStatus, param.getStatus())
                        .eq(StringUtils.hasText(param.getAccountId()), ProfileIdentity::getAccountId, param.getAccountId())
                        .eq(StringUtils.hasText(param.getDocumentType()), ProfileIdentity::getDocumentType, param.getDocumentType())
                        .orderByDesc(ProfileIdentity::getVerifiedAt));
        Page<IdentityPageResult> resultPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        resultPage.setRecords(entityPage.getRecords().stream().map(this::toPageResult).toList());
        return resultPage;
    }

    @Override
    @ReadDataSource
    public Map<String, String> getVerifiedRealNames(Collection<String> accountIds) {
        Map<String, String> map = new HashMap<>();
        if (accountIds == null || accountIds.isEmpty()) {
            return map;
        }
        profileIdentityMapper.selectByIds(accountIds).forEach(identity -> {
            if (!IdentitySnapshotStatus.VERIFIED.name().equals(identity.getStatus())) {
                return;
            }
            if (!StringUtils.hasText(identity.getRealNameCipher())) {
                return;
            }
            map.put(identity.getAccountId(), identityCryptoService.decrypt(identity.getRealNameCipher()));
        });
        return map;
    }

    @Override
    @ReadDataSource
    public Set<String> findAccountIdsByRealName(String name) {
        Set<String> matched = new HashSet<>();
        if (!StringUtils.hasText(name)) {
            return matched;
        }
        String keyword = name.trim().toLowerCase(Locale.ROOT);
        List<ProfileIdentity> identities = profileIdentityMapper.selectList(Wrappers.<ProfileIdentity>lambdaQuery()
                .eq(ProfileIdentity::getStatus, IdentitySnapshotStatus.VERIFIED.name())
                .isNotNull(ProfileIdentity::getRealNameCipher));
        for (ProfileIdentity identity : identities) {
            String realName = identityCryptoService.decrypt(identity.getRealNameCipher());
            if (realName != null && realName.toLowerCase(Locale.ROOT).contains(keyword)) {
                matched.add(identity.getAccountId());
            }
        }
        return matched;
    }

    @Override
    @ReadDataSource
    public boolean isVerified(String accountId) {
        ProfileIdentity identity = profileIdentityMapper.selectById(accountId);
        return identity != null && IdentitySnapshotStatus.VERIFIED.name().equals(identity.getStatus());
    }

    private IdentityPageResult toPageResult(ProfileIdentity identity) {
        IdentityPageResult result = new IdentityPageResult();
        result.setAccountId(identity.getAccountId());
        result.setStatus(identity.getStatus());
        result.setDocumentType(identity.getDocumentType());
        result.setVerifyChannel(identity.getVerifyChannel());
        result.setProvider(identity.getProvider());
        result.setVerifiedAt(identity.getVerifiedAt());
        result.setRevokedAt(identity.getRevokedAt());
        if (StringUtils.hasText(identity.getRealNameCipher())) {
            result.setRealNameMasked(identityCryptoService.maskRealName(
                    identityCryptoService.decrypt(identity.getRealNameCipher())));
        }
        if (StringUtils.hasText(identity.getDocumentNoCipher())) {
            result.setDocumentNoMasked(identityCryptoService.maskDocumentNo(
                    identityCryptoService.decrypt(identity.getDocumentNoCipher())));
        }
        return result;
    }

    private RealNameCaseSummaryResult toSummary(RealNameCase entity) {
        RealNameCaseSummaryResult summary = new RealNameCaseSummaryResult();
        summary.setCaseId(entity.getCaseId());
        summary.setAccountId(entity.getAccountId());
        summary.setBusinessType(entity.getBusinessType());
        summary.setVerifyChannel(entity.getVerifyChannel());
        summary.setStatus(entity.getStatus());
        summary.setDocumentType(entity.getDocumentType());
        summary.setCreatedAt(entity.getCreatedAt());
        summary.setReviewedAt(entity.getReviewedAt());
        summary.setRejectReason(entity.getRejectReason());
        if (StringUtils.hasText(entity.getRealNameCipher())) {
            summary.setRealNameMasked(identityCryptoService.maskRealName(
                    identityCryptoService.decrypt(entity.getRealNameCipher())));
        }
        if (StringUtils.hasText(entity.getDocumentNoCipher())) {
            summary.setDocumentNoMasked(identityCryptoService.maskDocumentNo(
                    identityCryptoService.decrypt(entity.getDocumentNoCipher())));
        }
        return summary;
    }
}

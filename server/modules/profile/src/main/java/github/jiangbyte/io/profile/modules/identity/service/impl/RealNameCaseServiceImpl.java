package github.jiangbyte.io.profile.modules.identity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCase;
import github.jiangbyte.io.profile.modules.identity.entity.RealNameCaseRecord;
import github.jiangbyte.io.profile.modules.identity.enums.RealNameBusinessType;
import github.jiangbyte.io.profile.modules.identity.enums.RealNameCaseStatus;
import github.jiangbyte.io.profile.modules.identity.enums.VerifyChannel;
import github.jiangbyte.io.profile.modules.identity.mapper.RealNameCaseMapper;
import github.jiangbyte.io.profile.modules.identity.mapper.RealNameCaseRecordMapper;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseApproveParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseCallbackParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseInitThirdPartyParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseMyPageParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseRejectParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseReviewPageParam;
import github.jiangbyte.io.profile.modules.identity.param.RealNameCaseSubmitParam;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseAttachmentResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseDetailResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseInitResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseOptionsResult;
import github.jiangbyte.io.profile.modules.identity.result.RealNameCaseSummaryResult;
import github.jiangbyte.io.profile.modules.identity.support.IdentityUserViewSupport;
import github.jiangbyte.io.profile.modules.identity.service.IdentityCryptoService;
import github.jiangbyte.io.profile.modules.identity.service.IdentityVerifyProvider;
import github.jiangbyte.io.profile.modules.identity.service.RealNameCaseService;
import github.jiangbyte.io.profile.modules.identity.support.IdentityVerifyProviderRegistry;
import github.jiangbyte.io.profile.modules.identity.support.RealNameBusinessHandlerRegistry;
import github.jiangbyte.io.sys.file.FileApi;
import github.jiangbyte.io.sys.file.FileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link RealNameCaseService} 实现。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class RealNameCaseServiceImpl implements RealNameCaseService {

    private static final List<String> DOCUMENT_TYPES = List.of("ID_CARD", "PASSPORT", "EID");

    private final RealNameCaseMapper realNameCaseMapper;
    private final RealNameCaseRecordMapper realNameCaseRecordMapper;
    private final IdentityCryptoService identityCryptoService;
    private final RealNameBusinessHandlerRegistry handlerRegistry;
    private final IdentityVerifyProviderRegistry providerRegistry;
    private final FileApi fileApi;

    @Override
    @ReadDataSource
    public RealNameCaseOptionsResult options() {
        RealNameCaseOptionsResult result = new RealNameCaseOptionsResult();
        RealNameCaseOptionsResult.RealNameBusinessOptionResult verify = new RealNameCaseOptionsResult.RealNameBusinessOptionResult();
        verify.setBusinessType(RealNameBusinessType.ACCOUNT_VERIFY.name());
        verify.setLabel("账号实名认证");
        verify.setChannels(List.of(VerifyChannel.MANUAL.name(), VerifyChannel.THIRD_PARTY.name()));
        result.getBusinessTypes().add(verify);
        result.setDocumentTypes(new ArrayList<>(DOCUMENT_TYPES));
        return result;
    }

    @Override
    @Transactional
    public void submit(RealNameCaseSubmitParam param) {
        LoginUser user = LoginHelper.requireUser();
        String businessType = normalizeBusinessType(param.getBusinessType());
        var handler = handlerRegistry.require(businessType);
        handler.validateSubmit(user.getAccountId(), param);

        RealNameCase entity = new RealNameCase();
        entity.setBusinessType(businessType);
        entity.setVerifyChannel(VerifyChannel.MANUAL.name());
        entity.setStatus(RealNameCaseStatus.PENDING.name());
        entity.setAccountId(user.getAccountId());
        fillSensitiveFields(entity, param.getDocumentType(), param.getRealName(), param.getDocumentNo());
        List<String> attachments = normalizeAttachmentIds(param.getAttachmentIds());
        if (attachments.isEmpty()) {
            throw new BizException("请上传证件材料");
        }
        entity.setAttachmentIds(attachments);
        if (StringUtils.hasText(param.getApplicantContact())) {
            entity.setApplicantContactCipher(identityCryptoService.encrypt(param.getApplicantContact().trim()));
        }
        entity.setSubmitterId(user.getAccountId());
        realNameCaseMapper.insert(entity);
        appendRecord(entity, "SUBMIT", null, entity.getStatus(), user.getAccountId(), null);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public RealNameCaseInitResult initThirdParty(RealNameCaseInitThirdPartyParam param) {
        LoginUser user = LoginHelper.requireUser();
        String businessType = normalizeBusinessType(param.getBusinessType());
        RealNameCaseSubmitParam validateParam = new RealNameCaseSubmitParam();
        validateParam.setBusinessType(businessType);
        validateParam.setDocumentType(param.getDocumentType());
        validateParam.setRealName(param.getRealName());
        validateParam.setDocumentNo(param.getDocumentNo());
        handlerRegistry.require(businessType).validateSubmit(user.getAccountId(), validateParam);

        RealNameCase entity = new RealNameCase();
        entity.setBusinessType(businessType);
        entity.setVerifyChannel(VerifyChannel.THIRD_PARTY.name());
        entity.setStatus(RealNameCaseStatus.PENDING.name());
        entity.setAccountId(user.getAccountId());
        fillSensitiveFields(entity, param.getDocumentType(), param.getRealName(), param.getDocumentNo());
        entity.setSubmitterId(user.getAccountId());
        realNameCaseMapper.insert(entity);

        IdentityVerifyProvider provider = providerRegistry.resolve(
                VerifyChannel.THIRD_PARTY.name(), param.getDocumentType(), param.getProvider());
        RealNameCaseInitResult initResult = provider.initVerify(entity, param);
        entity.setProvider(initResult.getProvider());
        entity.setProviderOrderNo(initResult.getProviderOrderNo());
        realNameCaseMapper.updateById(entity);
        appendRecord(entity, "INIT_THIRD_PARTY", null, entity.getStatus(), user.getAccountId(), null);
        AuditSnapshots.created(entity);
        return initResult;
    }

    @Override
    @Transactional
    public void callback(RealNameCaseCallbackParam param) {
        RealNameCase entity = requireCase(param.getCaseId());
        if (!RealNameCaseStatus.PENDING.name().equals(entity.getStatus())) {
            throw new BizException("Case is not pending");
        }
        IdentityVerifyProvider provider = providerRegistry.resolve(
                entity.getVerifyChannel(), entity.getDocumentType(), entity.getProvider());
        provider.handleCallback(entity, param);

        boolean success = Boolean.TRUE.equals(param.getSuccess());
        String before = entity.getStatus();
        if (success) {
            entity.setStatus(RealNameCaseStatus.APPROVED.name());
            entity.setReviewedAt(OffsetDateTime.now());
            realNameCaseMapper.updateById(entity);
            handlerRegistry.require(entity.getBusinessType()).onApproved(entity, "SYSTEM");
            appendRecord(entity, "CALLBACK", before, entity.getStatus(), "SYSTEM", param.getMessage());
            AuditSnapshots.after(entity);
            return;
        }
        entity.setStatus(RealNameCaseStatus.REJECTED.name());
        entity.setReviewedAt(OffsetDateTime.now());
        entity.setRejectReason(StringUtils.hasText(param.getMessage()) ? param.getMessage() : "Third-party verification failed");
        realNameCaseMapper.updateById(entity);
        handlerRegistry.require(entity.getBusinessType()).onRejected(entity, "SYSTEM", entity.getRejectReason());
        appendRecord(entity, "CALLBACK", before, entity.getStatus(), "SYSTEM", entity.getRejectReason());
        AuditSnapshots.after(entity);
    }

    @Override
    @ReadDataSource
    public Page<RealNameCaseSummaryResult> myPage(RealNameCaseMyPageParam param) {
        LoginUser user = LoginHelper.requireUser();
        Page<RealNameCase> entityPage = realNameCaseMapper.selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<RealNameCase>lambdaQuery()
                        .eq(RealNameCase::getAccountId, user.getAccountId())
                        .eq(StringUtils.hasText(param.getBusinessType()), RealNameCase::getBusinessType, param.getBusinessType())
                        .eq(StringUtils.hasText(param.getStatus()), RealNameCase::getStatus, param.getStatus())
                        .orderByDesc(RealNameCase::getCreatedAt));
        return toUserSummaryPage(entityPage);
    }

    @Override
    @ReadDataSource
    public Page<RealNameCaseSummaryResult> reviewPage(RealNameCaseReviewPageParam param) {
        String businessType = StringUtils.hasText(param.getBusinessType())
                ? param.getBusinessType()
                : RealNameBusinessType.ACCOUNT_VERIFY.name();
        Page<RealNameCase> entityPage = realNameCaseMapper.selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<RealNameCase>lambdaQuery()
                        .eq(RealNameCase::getBusinessType, businessType)
                        .eq(StringUtils.hasText(param.getStatus()), RealNameCase::getStatus, param.getStatus())
                        .eq(StringUtils.hasText(param.getAccountId()), RealNameCase::getAccountId, param.getAccountId())
                        .orderByDesc(RealNameCase::getCreatedAt));
        return toSummaryPage(entityPage);
    }

    @Override
    @ReadDataSource
    public RealNameCaseDetailResult detail(String caseId) {
        RealNameCase entity = requireCase(caseId);
        RealNameCaseDetailResult result = new RealNameCaseDetailResult();
        copySummary(entity, result);
        result.setProvider(entity.getProvider());
        result.setProviderOrderNo(entity.getProviderOrderNo());
        result.setSubmitterId(entity.getSubmitterId());
        result.setReviewerId(entity.getReviewerId());
        result.setAttachments(resolveAttachments(entity.getAttachmentIds()));
        return result;
    }

    @Override
    @Transactional
    public void approve(RealNameCaseApproveParam param) {
        LoginUser reviewer = LoginHelper.requireUser();
        RealNameCase entity = requireCase(param.getCaseId());
        if (!RealNameCaseStatus.PENDING.name().equals(entity.getStatus())) {
            throw new BizException("Case is not pending");
        }
        AuditSnapshots.before(entity);
        String before = entity.getStatus();
        entity.setStatus(RealNameCaseStatus.APPROVED.name());
        entity.setReviewerId(reviewer.getAccountId());
        entity.setReviewedAt(OffsetDateTime.now());
        realNameCaseMapper.updateById(entity);
        handlerRegistry.require(entity.getBusinessType()).onApproved(entity, reviewer.getAccountId());
        appendRecord(entity, "APPROVE", before, entity.getStatus(), reviewer.getAccountId(), param.getRemark());
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void reject(RealNameCaseRejectParam param) {
        LoginUser reviewer = LoginHelper.requireUser();
        RealNameCase entity = requireCase(param.getCaseId());
        if (!RealNameCaseStatus.PENDING.name().equals(entity.getStatus())) {
            throw new BizException("Case is not pending");
        }
        AuditSnapshots.before(entity);
        String before = entity.getStatus();
        entity.setStatus(RealNameCaseStatus.REJECTED.name());
        entity.setReviewerId(reviewer.getAccountId());
        entity.setReviewedAt(OffsetDateTime.now());
        entity.setRejectReason(param.getRejectReason().trim());
        realNameCaseMapper.updateById(entity);
        handlerRegistry.require(entity.getBusinessType()).onRejected(entity, reviewer.getAccountId(), entity.getRejectReason());
        appendRecord(entity, "REJECT", before, entity.getStatus(), reviewer.getAccountId(), entity.getRejectReason());
        AuditSnapshots.after(entity);
    }

    private RealNameCase requireCase(String caseId) {
        RealNameCase entity = realNameCaseMapper.selectById(caseId);
        if (entity == null) {
            throw new BizException(404, "Real-name case not found");
        }
        return entity;
    }

    private static String normalizeBusinessType(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            return RealNameBusinessType.ACCOUNT_VERIFY.name();
        }
        return businessType.trim().toUpperCase(Locale.ROOT);
    }

    private void fillSensitiveFields(RealNameCase entity, String documentType, String realName, String documentNo) {
        entity.setDocumentType(documentType == null ? null : documentType.trim().toUpperCase(Locale.ROOT));
        entity.setRealNameCipher(identityCryptoService.encrypt(realName));
        entity.setDocumentNoCipher(identityCryptoService.encrypt(documentNo));
        entity.setDocumentNoHash(identityCryptoService.hashDocumentNo(entity.getDocumentType(), documentNo));
    }

    private List<String> normalizeAttachmentIds(List<String> attachmentIds) {
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return new ArrayList<>();
        }
        return attachmentIds.stream()
                .filter(StringUtils::hasText)
                .map(fileApi::normalizeObjectName)
                .distinct()
                .toList();
    }

    private Page<RealNameCaseSummaryResult> toSummaryPage(Page<RealNameCase> entityPage) {
        Page<RealNameCaseSummaryResult> page = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        page.setRecords(entityPage.getRecords().stream().map(this::toSummary).toList());
        return page;
    }

    private Page<RealNameCaseSummaryResult> toUserSummaryPage(Page<RealNameCase> entityPage) {
        Page<RealNameCaseSummaryResult> page = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        page.setRecords(entityPage.getRecords().stream()
                .map(this::toSummary)
                .map(IdentityUserViewSupport::sanitizeSummary)
                .toList());
        return page;
    }

    private RealNameCaseSummaryResult toSummary(RealNameCase entity) {
        RealNameCaseSummaryResult summary = new RealNameCaseSummaryResult();
        copySummary(entity, summary);
        return summary;
    }

    private void copySummary(RealNameCase entity, RealNameCaseSummaryResult summary) {
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
    }

    private List<RealNameCaseAttachmentResult> resolveAttachments(List<String> attachmentIds) {
        List<RealNameCaseAttachmentResult> attachments = new ArrayList<>();
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return attachments;
        }
        List<FileInfo> files = fileApi.listByObjectNames(attachmentIds);
        for (FileInfo file : files) {
            RealNameCaseAttachmentResult item = new RealNameCaseAttachmentResult();
            item.setObjectName(file.getObjectName());
            item.setId(file.getId());
            item.setOriginalName(file.getOriginalName());
            item.setContentType(file.getContentType());
            item.setSize(file.getSize());
            String url = fileApi.resolveUrl(file.getObjectName());
            item.setUrl(StringUtils.hasText(url) ? url : file.getUrl());
            attachments.add(item);
        }
        return attachments;
    }

    private void appendRecord(
            RealNameCase entity,
            String action,
            String statusBefore,
            String statusAfter,
            String operatorId,
            String remark) {
        RealNameCaseRecord record = new RealNameCaseRecord();
        record.setCaseId(entity.getCaseId());
        record.setAccountId(entity.getAccountId());
        record.setBusinessType(entity.getBusinessType());
        record.setAction(action);
        record.setStatusBefore(statusBefore);
        record.setStatusAfter(statusAfter);
        record.setVerifyChannel(entity.getVerifyChannel());
        record.setProvider(entity.getProvider());
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        realNameCaseRecordMapper.insert(record);
    }
}

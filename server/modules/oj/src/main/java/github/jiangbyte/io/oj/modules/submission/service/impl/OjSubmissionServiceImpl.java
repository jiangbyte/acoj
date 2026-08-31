package github.jiangbyte.io.oj.modules.submission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.enums.OjProblemStatus;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.enums.OjVerdict;
import github.jiangbyte.io.oj.modules.submission.mapper.OjSubmissionMapper;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionCreateParam;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionPageParam;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionUpdateNoteParam;
import github.jiangbyte.io.oj.modules.submission.service.OjSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * OJ 提交服务实现：管理端查询与门户创建入队。
 *
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OjSubmissionServiceImpl extends ServiceImpl<OjSubmissionMapper, OjSubmission> implements OjSubmissionService {

    private final OjProblemMapper ojProblemMapper;
    private final OjProblemLanguageLimitService ojProblemLanguageLimitService;
    private final OjJudgePublisher ojJudgePublisher;

    @Override
    @ReadDataSource
    public OjSubmission detail(String id) {
        OjSubmission entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjSubmission not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjSubmission> page(OjSubmissionPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjSubmission>lambdaQuery()
                        .eq(StringUtils.hasText(param.getProblemId()), OjSubmission::getProblemId, param.getProblemId())
                        .eq(StringUtils.hasText(param.getAccountId()), OjSubmission::getAccountId, param.getAccountId())
                        .eq(StringUtils.hasText(param.getLanguage()), OjSubmission::getLanguage, param.getLanguage())
                        .eq(StringUtils.hasText(param.getStatus()), OjSubmission::getStatus, param.getStatus())
                        .orderByDesc(OjSubmission::getCreatedAt));
    }

    /**
     * 门户创建提交：校验发布题与语言 → 落 PENDING → 事务提交后入队。
     * 边界：返回前脱敏；不在此同步判题。
     */
    @Override
    @Transactional
    public OjSubmission createForPortal(OjSubmissionCreateParam param) {
        // 1. 必须登录
        String accountId = LoginHelper.requireUser().getAccountId();
        if (!StringUtils.hasText(accountId)) {
            throw new BizException(401, "未登录");
        }
        // 2. 校验题目已发布，避免草稿被提交
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "题目不存在");
        }
        if (!OjProblemStatus.PUBLISHED.matches(problem.getStatus())) {
            throw new BizException(400, "题目未发布");
        }
        // 3. 按语言查限额；缺失则拒绝（限额行即允许语言）
        if (!StringUtils.hasText(param.getLanguage())) {
            throw new BizException(400, "请选择语言");
        }
        String language = param.getLanguage().trim();
        if (ojProblemLanguageLimitService.findByProblemAndLanguage(problem.getId(), language) == null) {
            throw new BizException(400, "该题目不支持语言: " + language);
        }

        // 4. 落 PENDING 提交，快照当前 caseVersion
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OjSubmission entity = new OjSubmission();
        entity.setProblemId(problem.getId());
        entity.setAccountId(accountId);
        entity.setLanguage(language);
        entity.setSourceCode(param.getSourceCode());
        entity.setCaseVersion(problem.getCaseVersion() == null ? 1 : problem.getCaseVersion());
        entity.setStatus(OjVerdict.PENDING.name());
        entity.setScore(0);
        entity.setCaseResults(new ArrayList<>());
        entity.setSandboxRaw(new HashMap<>());
        entity.setTriedNodeIds(new ArrayList<>());
        entity.setExtra(new HashMap<>());
        entity.setDispatchCount(0);
        entity.setQueuedAt(now);
        this.save(entity);

        // 5. 必须在事务提交后再投递，否则 Consumer 可能读不到未提交行并误丢进 DLQ
        String submissionId = entity.getId();
        String requestId = UUID.randomUUID().toString().replace("-", "");
        enqueueJudgeAfterCommit(submissionId, requestId);
        // 6. 返回前脱敏，避免调度/沙箱字段泄露
        return scrubForPortal(entity);
    }

    private void enqueueJudgeAfterCommit(String submissionId, String requestId) {
        Runnable publish = () -> {
            try {
                ojJudgePublisher.publishWork(
                        OjJudgeMessage.of(submissionId, requestId, OjJudgeMessage.REASON_SUBMIT));
            } catch (Exception ex) {
                log.warn("publish judge work failed submissionId={}: {}", submissionId, ex.toString());
                this.update(Wrappers.<OjSubmission>lambdaUpdate()
                        .set(OjSubmission::getErrorCode, "MQ_PUBLISH_FAILED")
                        .set(OjSubmission::getLastDispatchError, truncate(ex.getMessage(), 512))
                        .set(OjSubmission::getJudgeMessage,
                                "判题入队失败，请检查 RabbitMQ：" + truncate(ex.getMessage(), 200))
                        .eq(OjSubmission::getId, submissionId)
                        .eq(OjSubmission::getStatus, OjVerdict.PENDING.name()));
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }

    @Override
    @ReadDataSource
    public OjSubmission portalDetail(String id) {
        String accountId = LoginHelper.requireUser().getAccountId();
        OjSubmission entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "提交不存在");
        }
        if (!accountId.equals(entity.getAccountId())) {
            throw new BizException(403, "无权查看");
        }
        return scrubForPortal(entity);
    }

    @Override
    @ReadDataSource
    public Page<OjSubmission> portalPage(OjSubmissionPageParam param) {
        String accountId = LoginHelper.requireUser().getAccountId();
        Page<OjSubmission> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjSubmission>lambdaQuery()
                        .eq(OjSubmission::getAccountId, accountId)
                        .eq(StringUtils.hasText(param.getProblemId()), OjSubmission::getProblemId, param.getProblemId())
                        .eq(StringUtils.hasText(param.getLanguage()), OjSubmission::getLanguage, param.getLanguage())
                        .eq(StringUtils.hasText(param.getStatus()), OjSubmission::getStatus, param.getStatus())
                        .orderByDesc(OjSubmission::getCreatedAt));
        for (OjSubmission row : page.getRecords()) {
            scrubForPortal(row);
        }
        return page;
    }

    @Override
    @Transactional
    public OjSubmission updateNoteForPortal(OjSubmissionUpdateNoteParam param) {
        String accountId = LoginHelper.requireUser().getAccountId();
        OjSubmission entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "提交不存在");
        }
        if (!accountId.equals(entity.getAccountId())) {
            throw new BizException(403, "无权修改");
        }
        String note = param.getNote() == null ? "" : param.getNote().trim();
        if (note.length() > 255) {
            throw new BizException(400, "备注过长");
        }
        this.update(Wrappers.<OjSubmission>lambdaUpdate()
                .set(OjSubmission::getNote, note.isEmpty() ? null : note)
                .eq(OjSubmission::getId, entity.getId())
                .eq(OjSubmission::getAccountId, accountId));
        entity.setNote(note.isEmpty() ? null : note);
        return scrubForPortal(entity);
    }

    /**
     * 门户响应脱敏：隐藏账户、调度 / 沙箱内部字段，以及逐测例明细。
     * 边界：原地清空字段后返回同一实体，供列表/详情复用。
     */
    private OjSubmission scrubForPortal(OjSubmission entity) {
        if (entity == null) {
            return null;
        }
        // 1. 隐藏身份与调度/沙箱内部字段，避免门户侧窥探
        entity.setAccountId(null);
        entity.setSandboxRaw(null);
        entity.setCaseResults(null);
        entity.setJudgeToken(null);
        entity.setJudgeLeaseUntil(null);
        entity.setJudgeLeaseOwner(null);
        entity.setTriedNodeIds(null);
        entity.setNextRetryAt(null);
        entity.setJudgeNodeId(null);
        entity.setDispatchCount(null);
        entity.setErrorCode(null);
        entity.setLastDispatchError(null);
        entity.setCaseVersion(null);
        // 2. 判题中不把调度内部文案透出到门户
        String status = entity.getStatus();
        if (OjVerdict.PENDING.matches(status) || OjVerdict.JUDGING.matches(status)) {
            entity.setJudgeMessage(null);
        }
        return entity;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}

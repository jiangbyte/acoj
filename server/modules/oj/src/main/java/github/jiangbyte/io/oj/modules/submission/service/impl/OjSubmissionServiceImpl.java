package github.jiangbyte.io.oj.modules.submission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgeMessage;
import github.jiangbyte.io.oj.modules.judge.mq.OjJudgePublisher;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import github.jiangbyte.io.oj.modules.submission.mapper.OjSubmissionMapper;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionCreateParam;
import github.jiangbyte.io.oj.modules.submission.param.OjSubmissionPageParam;
import github.jiangbyte.io.oj.modules.submission.service.OjSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Service
@RequiredArgsConstructor
public class OjSubmissionServiceImpl extends ServiceImpl<OjSubmissionMapper, OjSubmission> implements OjSubmissionService {

    private final OjProblemMapper ojProblemMapper;
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

    @Override
    @Transactional
    public OjSubmission createForPortal(String accountId, OjSubmissionCreateParam param) {
        if (!StringUtils.hasText(accountId)) {
            throw new BizException(401, "未登录");
        }
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "题目不存在");
        }
        if (!"PUBLISHED".equals(problem.getStatus())) {
            throw new BizException(400, "题目未发布");
        }
        if (!StringUtils.hasText(param.getLanguage())) {
            throw new BizException(400, "请选择语言");
        }
        String language = param.getLanguage().trim();
        List<String> allowed = problem.getAllowedLanguages();
        if (allowed != null && !allowed.isEmpty()
                && allowed.stream().noneMatch(l -> language.equalsIgnoreCase(l))) {
            throw new BizException(400, "该题目不支持语言: " + language);
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OjSubmission entity = new OjSubmission();
        entity.setProblemId(problem.getId());
        entity.setAccountId(accountId);
        entity.setLanguage(language);
        entity.setSourceCode(param.getSourceCode());
        entity.setCaseVersion(problem.getCaseVersion() == null ? 1 : problem.getCaseVersion());
        entity.setStatus("PENDING");
        entity.setScore(0);
        entity.setCaseResults(new ArrayList<>());
        entity.setSandboxRaw(new HashMap<>());
        entity.setTriedNodeIds(new ArrayList<>());
        entity.setExtra(new HashMap<>());
        entity.setDispatchCount(0);
        entity.setQueuedAt(now);
        this.save(entity);

        String requestId = UUID.randomUUID().toString().replace("-", "");
        try {
            ojJudgePublisher.publishWork(
                    OjJudgeMessage.of(entity.getId(), requestId, OjJudgeMessage.REASON_SUBMIT));
        } catch (Exception ex) {
            // confirm 失败依赖对账补偿；提交记录已落库
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public OjSubmission portalDetail(String accountId, String id) {
        OjSubmission entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "提交不存在");
        }
        if (!accountId.equals(entity.getAccountId())) {
            throw new BizException(403, "无权查看");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjSubmission> portalPage(String accountId, OjSubmissionPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjSubmission>lambdaQuery()
                        .eq(OjSubmission::getAccountId, accountId)
                        .eq(StringUtils.hasText(param.getProblemId()), OjSubmission::getProblemId, param.getProblemId())
                        .eq(StringUtils.hasText(param.getLanguage()), OjSubmission::getLanguage, param.getLanguage())
                        .eq(StringUtils.hasText(param.getStatus()), OjSubmission::getStatus, param.getStatus())
                        .orderByDesc(OjSubmission::getCreatedAt));
    }
}

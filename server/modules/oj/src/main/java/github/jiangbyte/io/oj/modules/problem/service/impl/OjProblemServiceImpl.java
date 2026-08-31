package github.jiangbyte.io.oj.modules.problem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.problem.convert.OjProblemConvert;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemAddParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemEditParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPageParam;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.problem.support.OjProblemPublishValidator;
import github.jiangbyte.io.oj.modules.tag.entity.OjProblemTag;
import github.jiangbyte.io.oj.modules.tag.mapper.OjProblemTagMapper;
import github.jiangbyte.io.oj.modules.tag.service.OjTagService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * OJ 题目服务实现：维护与查询。
 *
 * Author: Charlie
 */
@Service
public class OjProblemServiceImpl extends ServiceImpl<OjProblemMapper, OjProblem> implements OjProblemService {

    private final OjProblemConvert ojProblemConvert;
    private final OjProblemPublishValidator ojProblemPublishValidator;
    private final OjProblemTagMapper ojProblemTagMapper;
    private final OjTagService ojTagService;

    public OjProblemServiceImpl(
            OjProblemConvert ojProblemConvert,
            OjProblemPublishValidator ojProblemPublishValidator,
            OjProblemTagMapper ojProblemTagMapper,
            @Lazy OjTagService ojTagService) {
        this.ojProblemConvert = ojProblemConvert;
        this.ojProblemPublishValidator = ojProblemPublishValidator;
        this.ojProblemTagMapper = ojProblemTagMapper;
        this.ojTagService = ojTagService;
    }

    @Override
    @Transactional
    public void create(OjProblemAddParam param) {
        OjProblem entity = ojProblemConvert.toEntity(param);
        applyCreateDefaults(entity);
        this.save(entity);
        AuditSnapshots.created(entity);
        ojTagService.setProblemTags(entity.getId(), param.getTagIds());
    }

    @Override
    @Transactional
    public void update(OjProblemEditParam param) {
        OjProblem entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "OjProblem not found");
        }
        String previousStatus = entity.getStatus();
        AuditSnapshots.before(entity);
        ojProblemConvert.update(param, entity);
        if (entity.getSamples() == null) {
            entity.setSamples(new ArrayList<>());
        }
        if (entity.getAllowedLanguages() == null) {
            entity.setAllowedLanguages(new ArrayList<>());
        }
        if (entity.getExtra() == null) {
            entity.setExtra(new HashMap<>());
        }
        if ("PUBLISHED".equals(entity.getStatus()) && !"PUBLISHED".equals(previousStatus)) {
            ojProblemPublishValidator.assertCanPublish(entity);
        }
        this.updateById(entity);
        AuditSnapshots.after(entity);
        if (param.getTagIds() != null) {
            ojTagService.setProblemTags(entity.getId(), param.getTagIds());
        }
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<OjProblem> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        ojProblemTagMapper.delete(Wrappers.<OjProblemTag>lambdaQuery()
                .in(OjProblemTag::getProblemId, param.getIds()));
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public OjProblem detail(String id) {
        OjProblem entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjProblem not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjProblem> page(OjProblemPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjProblem>lambdaQuery()
                        .like(StringUtils.hasText(param.getProblemKey()), OjProblem::getProblemKey, param.getProblemKey())
                        .like(StringUtils.hasText(param.getTitle()), OjProblem::getTitle, param.getTitle())
                        .eq(StringUtils.hasText(param.getDifficulty()), OjProblem::getDifficulty, param.getDifficulty())
                        .eq(StringUtils.hasText(param.getStatus()), OjProblem::getStatus, param.getStatus())
                        .orderByDesc(OjProblem::getCreatedAt));
    }

    @Override
    @ReadDataSource
    public Page<OjProblem> portalPage(OjProblemPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjProblem>lambdaQuery()
                        .eq(OjProblem::getStatus, "PUBLISHED")
                        .like(StringUtils.hasText(param.getProblemKey()), OjProblem::getProblemKey, param.getProblemKey())
                        .like(StringUtils.hasText(param.getTitle()), OjProblem::getTitle, param.getTitle())
                        .eq(StringUtils.hasText(param.getDifficulty()), OjProblem::getDifficulty, param.getDifficulty())
                        .orderByDesc(OjProblem::getCreatedAt));
    }

    @Override
    @ReadDataSource
    public OjProblem portalDetail(String id) {
        OjProblem entity = this.getById(id);
        if (entity == null || !"PUBLISHED".equals(entity.getStatus())) {
            throw new BizException(404, "题目不存在");
        }
        return entity;
    }

    private void applyCreateDefaults(OjProblem entity) {
        if (!StringUtils.hasText(entity.getJudgeMode())) {
            entity.setJudgeMode("STANDARD");
        }
        if (entity.getCaseVersion() == null) {
            entity.setCaseVersion(1);
        }
        if (entity.getSubmitCount() == null) {
            entity.setSubmitCount(0);
        }
        if (entity.getAcceptCount() == null) {
            entity.setAcceptCount(0);
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("DRAFT");
        }
        if (entity.getSamples() == null) {
            entity.setSamples(new ArrayList<>());
        }
        if (entity.getAllowedLanguages() == null) {
            entity.setAllowedLanguages(new ArrayList<>());
        }
        if (entity.getExtra() == null) {
            entity.setExtra(new HashMap<>());
        }
        if (entity.getTimeLimitMs() == null) {
            entity.setTimeLimitMs(1000);
        }
        if (entity.getMemoryLimitBytes() == null) {
            entity.setMemoryLimitBytes(268435456L);
        }
    }
}

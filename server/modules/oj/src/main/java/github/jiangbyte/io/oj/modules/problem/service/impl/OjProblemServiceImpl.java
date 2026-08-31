package github.jiangbyte.io.oj.modules.problem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.oj.modules.problem.convert.OjProblemConvert;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.enums.OjJudgeMode;
import github.jiangbyte.io.oj.modules.problem.enums.OjProblemStatus;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemAddParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemEditParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemPageParam;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.problem.support.OjProblemPublishValidator;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.service.OjProblemLanguageLimitService;
import github.jiangbyte.io.oj.modules.tag.entity.OjProblemTag;
import github.jiangbyte.io.oj.modules.tag.mapper.OjProblemTagMapper;
import github.jiangbyte.io.oj.modules.tag.service.OjTagService;
import github.jiangbyte.io.oj.modules.stat.entity.OjUserProblemStat;
import github.jiangbyte.io.oj.modules.stat.enums.OjUserProblemStatStatus;
import github.jiangbyte.io.oj.modules.stat.mapper.OjUserProblemStatMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final OjUserProblemStatMapper ojUserProblemStatMapper;
    private final OjProblemLanguageLimitService ojProblemLanguageLimitService;

    public OjProblemServiceImpl(
            OjProblemConvert ojProblemConvert,
            OjProblemPublishValidator ojProblemPublishValidator,
            OjProblemTagMapper ojProblemTagMapper,
            @Lazy OjTagService ojTagService,
            OjUserProblemStatMapper ojUserProblemStatMapper,
            OjProblemLanguageLimitService ojProblemLanguageLimitService) {
        this.ojProblemConvert = ojProblemConvert;
        this.ojProblemPublishValidator = ojProblemPublishValidator;
        this.ojProblemTagMapper = ojProblemTagMapper;
        this.ojTagService = ojTagService;
        this.ojUserProblemStatMapper = ojUserProblemStatMapper;
        this.ojProblemLanguageLimitService = ojProblemLanguageLimitService;
    }

    /**
     * 新建题目：默认草稿 → 落库 → 全量替换语言限额 → 写标签。
     * 边界：不在此做发布门禁（新建默认 DRAFT）。
     */
    @Override
    @Transactional
    public void create(OjProblemAddParam param) {
        // 1. 转换并补默认值（判题模式/测例版本/计数/草稿态）
        OjProblem entity = ojProblemConvert.toEntity(param);
        applyCreateDefaults(entity);
        // 2. 落题干
        this.save(entity);
        // 3. 限额行即允许语言；全量替换
        ojProblemLanguageLimitService.replaceAll(entity.getId(), param.getLanguageLimits());
        AuditSnapshots.created(entity);
        // 4. 绑定标签
        ojTagService.setProblemTags(entity.getId(), param.getTagIds());
    }

    /**
     * 更新题目：合并字段 → 替换限额 → 若首次发布则门禁 → 落库/标签。
     * 边界：仅 DRAFT→PUBLISHED 时跑发布校验；已发布再改不重复门禁。
     */
    @Override
    @Transactional
    public void update(OjProblemEditParam param) {
        // 1. 加载并快照审计前状态
        OjProblem entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "OjProblem not found");
        }
        String previousStatus = entity.getStatus();
        AuditSnapshots.before(entity);
        // 2. MapStruct 合并；空 samples/extra 兜底，避免 null 覆盖坏 JSON
        ojProblemConvert.update(param, entity);
        if (entity.getSamples() == null) {
            entity.setSamples(new ArrayList<>());
        }
        if (entity.getExtra() == null) {
            entity.setExtra(new HashMap<>());
        }
        // 3. 全量替换语言限额（允许语言随限额行变更）
        ojProblemLanguageLimitService.replaceAll(entity.getId(), param.getLanguageLimits());
        // 4. 首次发布（非 PUBLISHED → PUBLISHED）才跑门禁
        if (OjProblemStatus.PUBLISHED.matches(entity.getStatus())
                && !OjProblemStatus.PUBLISHED.matches(previousStatus)) {
            ojProblemPublishValidator.assertCanPublish(entity);
        }
        // 5. 落库；tagIds 非 null 时才改标签（null=不改）
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
        // 标签关联按 500 分批删除，避免超长 IN
        List<String> ids = param.getIds();
        for (int i = 0; i < ids.size(); i += 500) {
            List<String> batch = ids.subList(i, Math.min(i + 500, ids.size()));
            ojProblemTagMapper.delete(Wrappers.<OjProblemTag>lambdaQuery()
                    .in(OjProblemTag::getProblemId, batch));
        }
        ojProblemLanguageLimitService.deleteByProblemIds(param.getIds());
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public OjProblem detail(String id) {
        OjProblem entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjProblem not found");
        }
        ojProblemLanguageLimitService.fillLanguageLimits(entity);
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

    /**
     * 门户题表：仅 PUBLISHED；可选按我的 AC/尝试/未做过滤；批量填 myStatus。
     * 边界：未登录则忽略 myStatus 过滤，myStatus 字段为空。
     */
    @Override
    @ReadDataSource
    public Page<OjProblem> portalPage(OjProblemPageParam param) {
        // 1. 可选登录态，用于 myStatus 过滤与回填
        String accountId = LoginHelper.currentUser()
                .map(u -> u.getAccountId())
                .orElse(null);
        // 2. 只列已发布；支持题号/标题/难度/关键词
        var query = Wrappers.<OjProblem>lambdaQuery()
                .eq(OjProblem::getStatus, OjProblemStatus.PUBLISHED.name())
                .like(StringUtils.hasText(param.getProblemKey()), OjProblem::getProblemKey, param.getProblemKey())
                .like(StringUtils.hasText(param.getTitle()), OjProblem::getTitle, param.getTitle())
                .eq(StringUtils.hasText(param.getDifficulty()), OjProblem::getDifficulty, param.getDifficulty());
        if (StringUtils.hasText(param.getKeyword())) {
            String keyword = param.getKeyword().trim();
            query.and(w -> w.like(OjProblem::getProblemKey, keyword)
                    .or()
                    .like(OjProblem::getTitle, keyword));
        }

        // 3. 按标签收窄：oj_problem_tag → problem_id，与其它条件 AND
        if (StringUtils.hasText(param.getTagId())) {
            applyTagFilter(query, param.getTagId().trim());
        }

        // 4. 已登录且指定 myStatus 时，按用户做题统计收窄题集
        String myStatusFilter = StringUtils.hasText(param.getMyStatus()) ? param.getMyStatus().trim().toUpperCase() : null;
        if (StringUtils.hasText(accountId) && StringUtils.hasText(myStatusFilter)) {
            applyMyStatusFilter(query, accountId, myStatusFilter);
        }

        // 5. 分页后批量填 myStatus，避免 N+1
        Page<OjProblem> page = this.getBaseMapper().selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                query.orderByAsc(OjProblem::getProblemKey));
        fillMyStatus(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public OjProblem portalDetail(String id) {
        OjProblem entity = this.getById(id);
        if (entity == null || !OjProblemStatus.PUBLISHED.matches(entity.getStatus())) {
            throw new BizException(404, "题目不存在");
        }
        entity.setCaseVersion(null);
        ojProblemLanguageLimitService.fillLanguageLimits(entity);
        fillMyStatus(List.of(entity));
        return entity;
    }

    @Override
    @ReadDataSource
    public void fillMyStatus(List<OjProblem> problems) {
        if (problems == null || problems.isEmpty()) {
            return;
        }
        for (OjProblem problem : problems) {
            problem.setMyStatus(null);
        }
        String accountId = LoginHelper.currentUser()
                .map(u -> u.getAccountId())
                .orElse(null);
        if (!StringUtils.hasText(accountId)) {
            return;
        }
        List<String> problemIds = problems.stream()
                .map(OjProblem::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (problemIds.isEmpty()) {
            return;
        }
        Map<String, String> statusByProblem = new HashMap<>();
        for (int i = 0; i < problemIds.size(); i += 500) {
            List<String> batch = problemIds.subList(i, Math.min(i + 500, problemIds.size()));
            List<OjUserProblemStat> stats = ojUserProblemStatMapper.selectList(
                    Wrappers.<OjUserProblemStat>lambdaQuery()
                            .eq(OjUserProblemStat::getAccountId, accountId)
                            .in(OjUserProblemStat::getProblemId, batch));
            for (OjUserProblemStat stat : stats) {
                statusByProblem.put(stat.getProblemId(), stat.getStatus());
            }
        }
        for (OjProblem problem : problems) {
            problem.setMyStatus(statusByProblem.get(problem.getId()));
        }
    }

    /**
     * 按 oj_problem_tag 收窄题集；无关联则空结果。
     */
    private void applyTagFilter(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OjProblem> query,
            String tagId) {
        List<OjProblemTag> links = ojProblemTagMapper.selectList(
                Wrappers.<OjProblemTag>lambdaQuery()
                        .eq(OjProblemTag::getTagId, tagId)
                        .select(OjProblemTag::getProblemId));
        Set<String> problemIds = links.stream()
                .map(OjProblemTag::getProblemId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (problemIds.isEmpty()) {
            query.eq(OjProblem::getId, "__none__");
        } else {
            applyChunkedIn(query, problemIds, false);
        }
    }

    private void applyMyStatusFilter(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OjProblem> query,
            String accountId,
            String myStatusFilter) {
        List<OjUserProblemStat> stats = ojUserProblemStatMapper.selectList(
                Wrappers.<OjUserProblemStat>lambdaQuery()
                        .eq(OjUserProblemStat::getAccountId, accountId)
                        .select(OjUserProblemStat::getProblemId, OjUserProblemStat::getStatus));
        Set<String> acceptedIds = new HashSet<>();
        Set<String> attemptedIds = new HashSet<>();
        for (OjUserProblemStat stat : stats) {
            if (!StringUtils.hasText(stat.getProblemId())) {
                continue;
            }
            attemptedIds.add(stat.getProblemId());
            if (OjUserProblemStatStatus.ACCEPTED.matches(stat.getStatus())) {
                acceptedIds.add(stat.getProblemId());
            }
        }
        if (OjUserProblemStatStatus.ACCEPTED.matches(myStatusFilter)) {
            if (acceptedIds.isEmpty()) {
                query.eq(OjProblem::getId, "__none__");
            } else {
                applyChunkedIn(query, acceptedIds, false);
            }
            return;
        }
        if (OjUserProblemStatStatus.ATTEMPTED.matches(myStatusFilter)) {
            Set<String> onlyAttempted = attemptedIds.stream()
                    .filter(id -> !acceptedIds.contains(id))
                    .collect(Collectors.toSet());
            if (onlyAttempted.isEmpty()) {
                query.eq(OjProblem::getId, "__none__");
            } else {
                applyChunkedIn(query, onlyAttempted, false);
            }
            return;
        }
        if ("UNTRIED".equals(myStatusFilter)) {
            if (!attemptedIds.isEmpty()) {
                applyChunkedIn(query, attemptedIds, true);
            }
        }
    }

    /**
     * 将大量 ID 以 500 为块拼进查询：IN 用 OR 连接各块；NOT IN 用 AND 连接各块。
     */
    private void applyChunkedIn(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OjProblem> query,
            Set<String> ids,
            boolean notIn) {
        List<String> list = new ArrayList<>(ids);
        if (list.size() <= 500) {
            if (notIn) {
                query.notIn(OjProblem::getId, list);
            } else {
                query.in(OjProblem::getId, list);
            }
            return;
        }
        query.and(w -> {
            for (int i = 0; i < list.size(); i += 500) {
                List<String> batch = list.subList(i, Math.min(i + 500, list.size()));
                if (notIn) {
                    w.notIn(OjProblem::getId, batch);
                } else if (i == 0) {
                    w.in(OjProblem::getId, batch);
                } else {
                    w.or().in(OjProblem::getId, batch);
                }
            }
        });
    }

    private void applyCreateDefaults(OjProblem entity) {
        if (!StringUtils.hasText(entity.getJudgeMode())) {
            entity.setJudgeMode(OjJudgeMode.STANDARD.name());
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
            entity.setStatus(OjProblemStatus.DRAFT.name());
        }
        if (entity.getSamples() == null) {
            entity.setSamples(new ArrayList<>());
        }
        if (entity.getExtra() == null) {
            entity.setExtra(new HashMap<>());
        }
    }
}

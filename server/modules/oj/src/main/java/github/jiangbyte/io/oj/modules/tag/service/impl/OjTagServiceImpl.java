package github.jiangbyte.io.oj.modules.tag.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.tag.convert.OjTagConvert;
import github.jiangbyte.io.oj.modules.tag.entity.OjProblemTag;
import github.jiangbyte.io.oj.modules.tag.entity.OjTag;
import github.jiangbyte.io.oj.modules.tag.mapper.OjProblemTagMapper;
import github.jiangbyte.io.oj.modules.tag.mapper.OjTagMapper;
import github.jiangbyte.io.oj.modules.tag.param.OjTagAddParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagEditParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagPageParam;
import github.jiangbyte.io.oj.modules.tag.service.OjTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OJ 标签服务实现：维护、查询与题目打标。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OjTagServiceImpl extends ServiceImpl<OjTagMapper, OjTag> implements OjTagService {

    private final OjTagConvert ojTagConvert;
    private final OjProblemService ojProblemService;
    private final OjProblemTagMapper ojProblemTagMapper;

    @Override
    @Transactional
    public void create(OjTagAddParam param) {
        OjTag entity = ojTagConvert.toEntity(param);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("ENABLED");
        }
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(OjTagEditParam param) {
        OjTag entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "OjTag not found");
        }
        AuditSnapshots.before(entity);
        ojTagConvert.update(param, entity);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<OjTag> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        ojProblemTagMapper.delete(Wrappers.<OjProblemTag>lambdaQuery()
                .in(OjProblemTag::getTagId, param.getIds()));
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public OjTag detail(String id) {
        OjTag entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjTag not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjTag> page(OjTagPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjTag>lambdaQuery()
                        .like(StringUtils.hasText(param.getName()), OjTag::getName, param.getName())
                        .eq(StringUtils.hasText(param.getStatus()), OjTag::getStatus, param.getStatus())
                        .orderByDesc(OjTag::getCreatedAt));
    }

    @Override
    @Transactional
    public void setProblemTags(String problemId, List<String> tagIds) {
        OjProblem problem = ojProblemService.getById(problemId);
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        List<String> normalized = tagIds == null ? List.of() : tagIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (normalized.size() > 2) {
            throw new BizException(400, "每道题最多绑定 2 个标签");
        }
        if (!normalized.isEmpty()) {
            List<OjTag> tags = this.listByIds(normalized);
            Set<String> found = new HashSet<>();
            for (OjTag tag : tags) {
                found.add(tag.getId());
            }
            for (String tagId : normalized) {
                if (!found.contains(tagId)) {
                    throw new BizException(404, "OjTag not found: " + tagId);
                }
            }
        }

        AuditSnapshots.before(Map.of("problemId", problemId));
        ojProblemTagMapper.delete(Wrappers.<OjProblemTag>lambdaQuery()
                .eq(OjProblemTag::getProblemId, problemId));
        if (!normalized.isEmpty()) {
            List<OjProblemTag> relations = new ArrayList<>(normalized.size());
            for (String tagId : normalized) {
                OjProblemTag relation = new OjProblemTag();
                relation.setProblemId(problemId);
                relation.setTagId(tagId);
                relations.add(relation);
            }
            for (OjProblemTag relation : relations) {
                ojProblemTagMapper.insert(relation);
            }
        }
        AuditSnapshots.after(Map.of("problemId", problemId, "tagIds", normalized));
    }

    @Override
    @ReadDataSource
    public List<OjTag> listByProblemId(String problemId) {
        if (!StringUtils.hasText(problemId)) {
            return List.of();
        }
        return mapByProblemIds(List.of(problemId)).getOrDefault(problemId, List.of());
    }

    @Override
    @ReadDataSource
    public Map<String, List<OjTag>> mapByProblemIds(Collection<String> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = problemIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<OjProblemTag> relations = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += 500) {
            List<String> batch = ids.subList(i, Math.min(i + 500, ids.size()));
            relations.addAll(ojProblemTagMapper.selectList(Wrappers.<OjProblemTag>lambdaQuery()
                    .in(OjProblemTag::getProblemId, batch)
                    .orderByAsc(OjProblemTag::getCreatedAt)));
        }
        if (relations.isEmpty()) {
            return Map.of();
        }
        Set<String> tagIds = relations.stream().map(OjProblemTag::getTagId).collect(Collectors.toSet());
        Map<String, OjTag> tagMap = new HashMap<>();
        List<String> tagIdList = new ArrayList<>(tagIds);
        for (int i = 0; i < tagIdList.size(); i += 500) {
            List<String> batch = tagIdList.subList(i, Math.min(i + 500, tagIdList.size()));
            for (OjTag tag : this.listByIds(batch)) {
                tagMap.put(tag.getId(), tag);
            }
        }
        Map<String, List<OjTag>> result = new LinkedHashMap<>();
        for (String problemId : ids) {
            result.put(problemId, new ArrayList<>());
        }
        for (OjProblemTag relation : relations) {
            OjTag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(relation.getProblemId(), key -> new ArrayList<>()).add(tag);
        }
        return result;
    }

    @Override
    @ReadDataSource
    public List<OjTag> listEnabled() {
        return this.list(Wrappers.<OjTag>lambdaQuery()
                .eq(OjTag::getStatus, "ENABLED")
                .orderByAsc(OjTag::getName));
    }

    @Override
    @ReadDataSource
    public void fillProblemTags(OjProblem problem) {
        if (problem == null || !StringUtils.hasText(problem.getId())) {
            return;
        }
        fillProblemTags(List.of(problem));
    }

    @Override
    @ReadDataSource
    public void fillProblemTags(List<OjProblem> problems) {
        if (problems == null || problems.isEmpty()) {
            return;
        }
        List<String> problemIds = problems.stream()
                .map(OjProblem::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, List<OjTag>> mapped = mapByProblemIds(problemIds);
        for (OjProblem problem : problems) {
            List<OjTag> tags = mapped.getOrDefault(problem.getId(), Collections.emptyList());
            List<OjProblem.OjTagBrief> briefs = new ArrayList<>(tags.size());
            List<String> tagIds = new ArrayList<>(tags.size());
            for (OjTag tag : tags) {
                OjProblem.OjTagBrief brief = new OjProblem.OjTagBrief();
                brief.setId(tag.getId());
                brief.setName(tag.getName());
                briefs.add(brief);
                tagIds.add(tag.getId());
            }
            problem.setTags(briefs);
            problem.setTagIds(tagIds);
        }
    }
}

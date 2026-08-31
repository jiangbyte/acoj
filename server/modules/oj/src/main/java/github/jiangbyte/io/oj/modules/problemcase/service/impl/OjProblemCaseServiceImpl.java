package github.jiangbyte.io.oj.modules.problemcase.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.service.OjProblemService;
import github.jiangbyte.io.oj.modules.problemcase.convert.OjProblemCaseConvert;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.mapper.OjProblemCaseMapper;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseAddParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseEditParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseItemParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCasePageParam;
import github.jiangbyte.io.oj.modules.problemcase.service.OjProblemCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OJ 题目测例服务实现：维护、查询与整包升版本。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OjProblemCaseServiceImpl extends ServiceImpl<OjProblemCaseMapper, OjProblemCase> implements OjProblemCaseService {

    private final OjProblemCaseConvert ojProblemCaseConvert;
    private final OjProblemService ojProblemService;

    @Override
    @Transactional
    public void create(OjProblemCaseAddParam param) {
        OjProblem problem = ojProblemService.getById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        OjProblemCase entity = ojProblemCaseConvert.toEntity(param);
        if (entity.getCaseVersion() == null) {
            entity.setCaseVersion(problem.getCaseVersion());
        }
        applyCaseDefaults(entity);
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(OjProblemCaseEditParam param) {
        OjProblemCase entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "OjProblemCase not found");
        }
        AuditSnapshots.before(entity);
        ojProblemCaseConvert.update(param, entity);
        applyCaseDefaults(entity);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<OjProblemCase> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public OjProblemCase detail(String id) {
        OjProblemCase entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjProblemCase not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjProblemCase> page(OjProblemCasePageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjProblemCase>lambdaQuery()
                        .eq(StringUtils.hasText(param.getProblemId()), OjProblemCase::getProblemId, param.getProblemId())
                        .eq(param.getCaseVersion() != null, OjProblemCase::getCaseVersion, param.getCaseVersion())
                        .like(StringUtils.hasText(param.getCaseKey()), OjProblemCase::getCaseKey, param.getCaseKey())
                        .eq(StringUtils.hasText(param.getStatus()), OjProblemCase::getStatus, param.getStatus())
                        .orderByAsc(OjProblemCase::getSortNo)
                        .orderByDesc(OjProblemCase::getCreatedAt));
    }

    @Override
    @Transactional
    public void replaceCasesForNewVersion(String problemId, List<OjProblemCaseItemParam> cases) {
        OjProblem problem = ojProblemService.getById(problemId);
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        AuditSnapshots.before(problem);
        int nextVersion = (problem.getCaseVersion() == null ? 0 : problem.getCaseVersion()) + 1;
        problem.setCaseVersion(nextVersion);
        ojProblemService.updateById(problem);
        AuditSnapshots.after(problem);

        if (cases == null || cases.isEmpty()) {
            return;
        }
        List<OjProblemCase> entities = new ArrayList<>(cases.size());
        for (OjProblemCaseItemParam item : cases) {
            OjProblemCase entity = ojProblemCaseConvert.toEntity(item);
            entity.setProblemId(problemId);
            entity.setCaseVersion(nextVersion);
            applyCaseDefaults(entity);
            entities.add(entity);
        }
        this.saveBatch(entities);
        AuditSnapshots.after(Map.of("problemId", problemId, "caseVersion", nextVersion, "caseCount", entities.size()));
    }

    private void applyCaseDefaults(OjProblemCase entity) {
        if (entity.getSortNo() == null) {
            entity.setSortNo(0);
        }
        if (entity.getIsSample() == null) {
            entity.setIsSample(false);
        }
        if (entity.getScore() == null) {
            entity.setScore(0);
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("ENABLED");
        }
        if (entity.getExtra() == null) {
            entity.setExtra(new HashMap<>());
        }
        normalizeStorageFields(entity);
        if (entity.getInputBytes() == null) {
            entity.setInputBytes(textByteLength(entity.getInputText()));
        }
        if (entity.getOutputBytes() == null) {
            entity.setOutputBytes(textByteLength(entity.getOutputText()));
        }
    }

    private static void normalizeStorageFields(OjProblemCase entity) {
        if ("OBJECT".equalsIgnoreCase(entity.getInputStorage())) {
            if (!StringUtils.hasText(entity.getInputObjectKey())) {
                throw new BizException(400, "OBJECT 输入须指定 input_object_key");
            }
            entity.setInputText(null);
        } else {
            entity.setInputStorage("INLINE");
            entity.setInputObjectKey(null);
        }
        if ("OBJECT".equalsIgnoreCase(entity.getOutputStorage())) {
            if (!StringUtils.hasText(entity.getOutputObjectKey())) {
                throw new BizException(400, "OBJECT 输出须指定 output_object_key");
            }
            entity.setOutputText(null);
        } else {
            entity.setOutputStorage("INLINE");
            entity.setOutputObjectKey(null);
        }
    }

    private static int textByteLength(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        return text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    }
}

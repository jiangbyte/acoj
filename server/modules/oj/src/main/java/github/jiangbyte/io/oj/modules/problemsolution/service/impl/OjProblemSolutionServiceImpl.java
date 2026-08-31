package github.jiangbyte.io.oj.modules.problemsolution.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.mapper.OjProblemMapper;
import github.jiangbyte.io.oj.modules.problemsolution.convert.OjProblemSolutionConvert;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.mapper.OjProblemSolutionMapper;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionAddParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionEditParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionPageParam;
import github.jiangbyte.io.oj.modules.problemsolution.service.OjProblemSolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * OJ 参考答案服务实现。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OjProblemSolutionServiceImpl
        extends ServiceImpl<OjProblemSolutionMapper, OjProblemSolution>
        implements OjProblemSolutionService {

    private final OjProblemSolutionConvert ojProblemSolutionConvert;
    private final OjProblemMapper ojProblemMapper;

    @Override
    @Transactional
    public void create(OjProblemSolutionAddParam param) {
        OjProblem problem = ojProblemMapper.selectById(param.getProblemId());
        if (problem == null) {
            throw new BizException(404, "OjProblem not found");
        }
        long dup = this.count(Wrappers.<OjProblemSolution>lambdaQuery()
                .eq(OjProblemSolution::getProblemId, param.getProblemId())
                .eq(OjProblemSolution::getLanguage, param.getLanguage().trim()));
        if (dup > 0) {
            throw new BizException("该语言参考答案已存在");
        }
        OjProblemSolution entity = ojProblemSolutionConvert.toEntity(param);
        entity.setLanguage(param.getLanguage().trim());
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("ENABLED");
        }
        if (entity.getIsDefault() == null) {
            entity.setIsDefault(false);
        }
        this.save(entity);
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearOtherDefaults(entity.getProblemId(), entity.getId());
        }
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(OjProblemSolutionEditParam param) {
        OjProblemSolution entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "OjProblemSolution not found");
        }
        String newLang = param.getLanguage().trim();
        long dup = this.count(Wrappers.<OjProblemSolution>lambdaQuery()
                .eq(OjProblemSolution::getProblemId, entity.getProblemId())
                .eq(OjProblemSolution::getLanguage, newLang)
                .ne(OjProblemSolution::getId, entity.getId()));
        if (dup > 0) {
            throw new BizException("该语言参考答案已存在");
        }
        AuditSnapshots.before(entity);
        ojProblemSolutionConvert.update(param, entity);
        entity.setLanguage(newLang);
        if (entity.getIsDefault() == null) {
            entity.setIsDefault(false);
        }
        this.updateById(entity);
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearOtherDefaults(entity.getProblemId(), entity.getId());
        }
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<OjProblemSolution> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public OjProblemSolution detail(String id) {
        OjProblemSolution entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjProblemSolution not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjProblemSolution> page(OjProblemSolutionPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjProblemSolution>lambdaQuery()
                        .eq(StringUtils.hasText(param.getProblemId()), OjProblemSolution::getProblemId, param.getProblemId())
                        .eq(StringUtils.hasText(param.getLanguage()), OjProblemSolution::getLanguage, param.getLanguage())
                        .eq(StringUtils.hasText(param.getStatus()), OjProblemSolution::getStatus, param.getStatus())
                        .orderByDesc(OjProblemSolution::getIsDefault)
                        .orderByAsc(OjProblemSolution::getLanguage));
    }

    @Override
    @ReadDataSource
    public List<OjProblemSolution> listByProblemId(String problemId) {
        if (!StringUtils.hasText(problemId)) {
            throw new BizException("problem_id 不能为空");
        }
        return this.list(Wrappers.<OjProblemSolution>lambdaQuery()
                .eq(OjProblemSolution::getProblemId, problemId)
                .orderByDesc(OjProblemSolution::getIsDefault)
                .orderByAsc(OjProblemSolution::getLanguage));
    }

    private void clearOtherDefaults(String problemId, String keepId) {
        this.update(Wrappers.<OjProblemSolution>lambdaUpdate()
                .set(OjProblemSolution::getIsDefault, false)
                .eq(OjProblemSolution::getProblemId, problemId)
                .ne(OjProblemSolution::getId, keepId)
                .eq(OjProblemSolution::getIsDefault, true));
    }
}

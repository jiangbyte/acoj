package github.jiangbyte.io.oj.modules.judge.node.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.judge.enums.OjCircuitState;
import github.jiangbyte.io.oj.modules.judge.enums.OjJudgeAdminStatus;
import github.jiangbyte.io.oj.modules.judge.enums.OjJudgeRuntimeStatus;
import github.jiangbyte.io.oj.modules.judge.node.convert.OjJudgeNodeConvert;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.mapper.OjJudgeNodeMapper;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeAddParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeEditParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodePageParam;
import github.jiangbyte.io.oj.modules.judge.node.result.OjJudgeLanguagesResult;
import github.jiangbyte.io.oj.modules.judge.node.service.OjJudgeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * OJ 执行机服务实现：维护与查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class OjJudgeNodeServiceImpl extends ServiceImpl<OjJudgeNodeMapper, OjJudgeNode> implements OjJudgeNodeService {

    private final OjJudgeNodeConvert ojJudgeNodeConvert;

    @Override
    @Transactional
    public void create(OjJudgeNodeAddParam param) {
        OjJudgeNode entity = ojJudgeNodeConvert.toEntity(param);
        applyCreateDefaults(entity);
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(OjJudgeNodeEditParam param) {
        OjJudgeNode entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "OjJudgeNode not found");
        }
        AuditSnapshots.before(entity);
        // 心跳自注册字段：编码/并发/语言/验签由沙箱心跳维护；base_url 由运维在 Admin 维护
        String code = entity.getCode();
        Integer maxConcurrency = entity.getMaxConcurrency();
        Boolean signingEnabled = entity.getSigningEnabled();
        String signingSecretCipher = entity.getSigningSecretCipher();
        List<String> supportedLanguages = entity.getSupportedLanguages();
        Map<String, Object> extra = entity.getExtra();
        ojJudgeNodeConvert.update(param, entity);
        entity.setCode(code);
        entity.setMaxConcurrency(maxConcurrency);
        entity.setSigningEnabled(signingEnabled == null ? Boolean.TRUE : signingEnabled);
        entity.setSigningSecretCipher(signingSecretCipher);
        entity.setSupportedLanguages(supportedLanguages == null ? new ArrayList<>() : supportedLanguages);
        entity.setExtra(extra == null ? new HashMap<>() : extra);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<OjJudgeNode> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public OjJudgeNode detail(String id) {
        OjJudgeNode entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "OjJudgeNode not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<OjJudgeNode> page(OjJudgeNodePageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjJudgeNode>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), OjJudgeNode::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), OjJudgeNode::getName, param.getName())
                        .eq(StringUtils.hasText(param.getAdminStatus()), OjJudgeNode::getAdminStatus, param.getAdminStatus())
                        .eq(StringUtils.hasText(param.getRuntimeStatus()), OjJudgeNode::getRuntimeStatus, param.getRuntimeStatus())
                        .eq(StringUtils.hasText(param.getCircuitState()), OjJudgeNode::getCircuitState, param.getCircuitState())
                        .orderByAsc(OjJudgeNode::getPriority)
                        .orderByDesc(OjJudgeNode::getCreatedAt));
    }

    @Override
    @ReadDataSource
    public OjJudgeLanguagesResult listAggregatedLanguages() {
        List<OjJudgeNode> nodes = this.list(Wrappers.<OjJudgeNode>lambdaQuery()
                .eq(OjJudgeNode::getAdminStatus, OjJudgeAdminStatus.ENABLED.name())
                .select(OjJudgeNode::getId, OjJudgeNode::getSupportedLanguages));
        TreeSet<String> languages = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (OjJudgeNode node : nodes) {
            List<String> supported = node.getSupportedLanguages();
            if (supported == null || supported.isEmpty()) {
                continue;
            }
            for (String lang : supported) {
                if (StringUtils.hasText(lang)) {
                    languages.add(lang.trim());
                }
            }
        }
        OjJudgeLanguagesResult result = new OjJudgeLanguagesResult();
        result.setLanguages(new ArrayList<>(languages));
        result.setNodeCount(nodes.size());
        return result;
    }

    private void applyCreateDefaults(OjJudgeNode entity) {
        if (entity.getSigningEnabled() == null) {
            entity.setSigningEnabled(true);
        }
        if (!StringUtils.hasText(entity.getAdminStatus())) {
            entity.setAdminStatus(OjJudgeAdminStatus.ENABLED.name());
        }
        if (!StringUtils.hasText(entity.getRuntimeStatus())) {
            entity.setRuntimeStatus(OjJudgeRuntimeStatus.OFFLINE.name());
        }
        if (!StringUtils.hasText(entity.getCircuitState())) {
            entity.setCircuitState(OjCircuitState.CLOSED.name());
        }
        if (entity.getWeight() == null) {
            entity.setWeight(100);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(100);
        }
        if (entity.getMaxConcurrency() == null) {
            entity.setMaxConcurrency(4);
        }
        if (entity.getInflightCount() == null) {
            entity.setInflightCount(0);
        }
        if (entity.getEpoch() == null) {
            entity.setEpoch(0L);
        }
        if (entity.getTotalDispatch() == null) {
            entity.setTotalDispatch(0L);
        }
        if (entity.getTotalSuccess() == null) {
            entity.setTotalSuccess(0L);
        }
        if (entity.getTotalTransportFail() == null) {
            entity.setTotalTransportFail(0L);
        }
        if (entity.getConsecutiveFailCount() == null) {
            entity.setConsecutiveFailCount(0);
        }
        if (entity.getSupportedLanguages() == null) {
            entity.setSupportedLanguages(new ArrayList<>());
        }
        if (entity.getExtra() == null) {
            entity.setExtra(new HashMap<>());
        }
    }
}

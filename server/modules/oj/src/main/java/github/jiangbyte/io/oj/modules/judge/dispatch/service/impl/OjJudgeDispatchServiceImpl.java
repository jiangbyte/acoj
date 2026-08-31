package github.jiangbyte.io.oj.modules.judge.dispatch.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.mapper.OjJudgeDispatchMapper;
import github.jiangbyte.io.oj.modules.judge.dispatch.param.OjJudgeDispatchPageParam;
import github.jiangbyte.io.oj.modules.judge.dispatch.service.OjJudgeDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * OJ 派发审计服务实现：管理端查询。
 *
 * Author: Charlie
 */
@Service
public class OjJudgeDispatchServiceImpl extends ServiceImpl<OjJudgeDispatchMapper, OjJudgeDispatch> implements OjJudgeDispatchService {

    @Override
    @ReadDataSource
    public Page<OjJudgeDispatch> page(OjJudgeDispatchPageParam param) {
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<OjJudgeDispatch>lambdaQuery()
                        .eq(StringUtils.hasText(param.getSubmissionId()), OjJudgeDispatch::getSubmissionId, param.getSubmissionId())
                        .eq(StringUtils.hasText(param.getNodeId()), OjJudgeDispatch::getNodeId, param.getNodeId())
                        .eq(StringUtils.hasText(param.getOutcome()), OjJudgeDispatch::getOutcome, param.getOutcome())
                        .orderByDesc(OjJudgeDispatch::getStartedAt)
                        .orderByDesc(OjJudgeDispatch::getCreatedAt));
    }
}

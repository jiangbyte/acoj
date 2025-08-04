package io.charlie.app.core.modular.analyse.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.charlie.app.core.modular.analyse.entity.TotalProblemSubmitAnalyse;
import io.charlie.app.core.modular.analyse.entity.SubmitTrend;
import io.charlie.app.core.modular.analyse.service.ProblemSubmitAnalyseService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.mapper.ProSubmitMapper;
import io.charlie.galaxy.option.NameOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 提交分析服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProblemSubmitAnalyseServiceImpl implements ProblemSubmitAnalyseService {
    private final ProSubmitMapper proSubmitMapper;
    private final ProSolvedMapper proSolvedMapper;

    @Override
    public TotalProblemSubmitAnalyse getTotalProblemSubmitAnalyse() {
        TotalProblemSubmitAnalyse totalProblemSubmitAnalyse = new TotalProblemSubmitAnalyse();
        // 今日提交，从提交表中获取
        totalProblemSubmitAnalyse.setDailySubmit(proSubmitMapper.selectCount(new QueryWrapper<ProSubmit>().lambda()
                .between(ProSubmit::getUpdateTime, DateUtil.beginOfDay(DateUtil.date()), DateUtil.endOfDay(DateUtil.date()))));

        // 总提交，从提交表中获取
        totalProblemSubmitAnalyse.setTotalSubmit(proSubmitMapper.selectCount(new QueryWrapper<>()));

        // 今日通过，从解决表中获取
        totalProblemSubmitAnalyse.setDailyAC(proSolvedMapper.selectCount(new QueryWrapper<ProSolved>().lambda()
                .eq(ProSolved::getSolved, true).between(ProSolved::getCreateTime, DateUtil.beginOfDay(DateUtil.date()), DateUtil.endOfDay(DateUtil.date()))));

        // 总通过数，从解决表中获取
        totalProblemSubmitAnalyse.setTotalAC(proSolvedMapper.selectCount(new QueryWrapper<ProSolved>().lambda().eq(ProSolved::getSolved, true)));

        return totalProblemSubmitAnalyse;
    }

    @Override
    public List<NameOption<Long>> getLanguageDistribution() {
        // sql 查询
        return proSubmitMapper.getLanguageDistribution();
    }

    @Override
    public List<NameOption<Double>> getProblemRateDistribution() {
        return proSubmitMapper.getProblemRateDistribution();
    }

    @Override
    public List<SubmitTrend> getWeeklySubmitTrend() {
        return proSubmitMapper.getWeeklySubmitTrend();
    }

    @Override
    public List<SubmitTrend> getTodayHourlySubmitTrend() {
        return proSubmitMapper.getTodayHourlySubmitTrend();
    }
}

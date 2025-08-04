package io.charlie.app.core.modular.analyse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.app.core.modular.analyse.entity.TotalProblemAnalyse;
import io.charlie.app.core.modular.analyse.entity.TotalUserAnalyse;
import io.charlie.app.core.modular.analyse.service.ProblemAnalyseService;
import io.charlie.app.core.modular.analyse.service.UserAnalyseService;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 分析服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProblemAnalyseServiceImpl implements ProblemAnalyseService {
    private final ProProblemMapper proProblemMapper;
    private final ProSolvedMapper proSolvedMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public TotalProblemAnalyse getTotalProblemAnalyse() {
        TotalProblemAnalyse totalProblemAnalyse = new TotalProblemAnalyse();

        // 获取总问题数
        Long totalProblems = proProblemMapper.selectCount(new LambdaQueryWrapper<>());
        totalProblemAnalyse.setProblems(totalProblems);

        // 如果问题数为0或null，设置通过率为0%，避免除零异常
        if (totalProblems == null || totalProblems == 0L) {
            totalProblemAnalyse.setPassRate(0.0);
            return totalProblemAnalyse;
        }

        // 获取已解决的问题数
        Long solvedCount = proSolvedMapper.selectCount(
                new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getSolved, true));

        // 计算通过率百分值（保留两位小数）
        double passRate = (double) solvedCount / totalProblems * 100;
        passRate = Math.round(passRate * 100) / 100.0;  // 四舍五入保留两位小数
        totalProblemAnalyse.setPassRate(passRate);

        return totalProblemAnalyse;
    }
}

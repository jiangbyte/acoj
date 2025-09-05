package io.charlie.app.core.modular.problem.problem.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.mapper.ProProblemMapper;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HourlySolvedTask {
    private final ProProblemMapper proProblemMapper;
    private final ProSolvedMapper proSolvedMapper;

    // 每隔2小时执行一次
    @Scheduled(fixedRate = 60 * 60 * 2000) // 60分钟 * 60秒 * 2000毫秒
    public void printHourlyMessage() {
        log.info("开始执行每2小时解决记录任务...");
        int pageSize = 100;
        long totalCount = proProblemMapper.selectCount(null);
        for (int i = 0; i < totalCount; i += pageSize) {
            // 获取当前页的数据
            List<ProProblem> pageData = proProblemMapper.selectPage(new Page<>(i, pageSize), null).getRecords();
            // 遍历当前页的数据
            for (ProProblem problem : pageData) {
                Long proSolvedTotalCount = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getProblemId, problem.getId()));
                if (proSolvedTotalCount == null || proSolvedTotalCount == 0) {
                    problem.setSolved(0L);
                    proProblemMapper.updateById(problem);
                } else {
                    problem.setSolved(proSolvedTotalCount);
                    proProblemMapper.updateById(problem);
                }
            }
        }
    }
}
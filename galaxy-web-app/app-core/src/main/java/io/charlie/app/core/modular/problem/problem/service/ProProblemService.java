package io.charlie.app.core.modular.problem.problem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.problem.param.*;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-07-25
 * @description 题目表 服务类
 */
public interface ProProblemService extends IService<ProProblem> {
    // 分页查询（管理端）
    Page<ProProblem> page(ProProblemPageParam proProblemPageParam);

    // 新增题目
    void add(ProProblemAddParam proProblemAddParam);

    // 编辑题目
    void edit(ProProblemEditParam proProblemEditParam);

    // 删除题目（支持批量）
    void delete(List<ProProblemIdParam> proProblemIdParamList);

    // 获取题目详情（管理端，包含完整信息）
    ProProblem detail(ProProblemIdParam proProblemIdParam);

    // 获取题目详情（应用端，脱敏测试用例和代码模板）
    ProProblem appDetail(ProProblemIdParam proProblemIdParam);

    // 分页查询（应用端）
    Page<ProProblem> appPage(ProProblemPageParam proProblemPageParam);

    // 查询最新的N道题目
    List<ProProblem> latestN(int n);

    // 获得题目的描述
    String getDescription(String problemId);

    // 获得题目的测试用例（随机一例）
    String getTestCase(String problemId);

    // 难度分布
    List<DifficultyDistribution> difficultyDistribution();

    // 获取题目的题目数以及本月相比上个月增加了几个百分比题目
    ProblemCountAndIncreasedPercentage getProblemCountAndPercentage();

    // 获得今日题目增长数量
    TodayProblemCount getTodayProblemCount();

    // 获得某个用户最近解题记录
    Page<ProProblem> userRecentSolvedPage(UserProblemPageParam userProblemPageParam);

}

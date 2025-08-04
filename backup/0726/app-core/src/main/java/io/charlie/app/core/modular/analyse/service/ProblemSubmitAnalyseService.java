package io.charlie.app.core.modular.analyse.service;

import io.charlie.app.core.modular.analyse.entity.TotalProblemSubmitAnalyse;
import io.charlie.app.core.modular.analyse.entity.SubmitTrend;
import io.charlie.galaxy.option.NameOption;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 25/07/2025
 * @description 提交分析服务
 */
public interface ProblemSubmitAnalyseService {
    TotalProblemSubmitAnalyse getTotalProblemSubmitAnalyse();

    List<NameOption<Long>> getLanguageDistribution();

    List<NameOption<Double>> getProblemRateDistribution();

    List<SubmitTrend> getWeeklySubmitTrend();

    List<SubmitTrend> getTodayHourlySubmitTrend();
}

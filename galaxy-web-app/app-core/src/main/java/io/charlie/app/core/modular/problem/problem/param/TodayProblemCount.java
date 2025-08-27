package io.charlie.app.core.modular.problem.problem.param;

import lombok.Data;

import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 27/08/2025
 * @description 今日题目增长数量
 */
@Data
public class TodayProblemCount {
    private String todayProblemCount;
    private Date latestCreateTime;
}

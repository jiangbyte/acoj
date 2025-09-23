package io.charlie.web.oj.modular.data.ranking.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description 排行榜枚举
 */
@Getter
@AllArgsConstructor
public enum RankingEnums implements ILabelEnum<String> {
    // ========== 用户排行榜 (user:) ==========
    USER_TOTAL("rank:user:total", "用户总排行榜"),
    USER_WEEKLY("rank:user:weekly", "用户周排行榜"),
    USER_MONTHLY("rank:user:monthly", "用户月排行榜"),
    USER_DAILY("rank:user:daily", "用户日排行榜"),
    USER_YEARLY("rank:user:yearly", "用户年排行榜"),

    // ========== 题目排行榜 (problem:) ==========
    PROBLEM_TOTAL("rank:problem:total", "题目总排行榜"),
    PROBLEM_SET_TOTAL("rank:problem:set:total", "题目总排行榜"),
    PROBLEM_WEEKLY("rank:problem:weekly", "题目周排行榜"),
    PROBLEM_MONTHLY("rank:problem:monthly", "题目月排行榜"),
    PROBLEM_DAILY("rank:problem:daily", "题目日排行榜"),

    // ========== 题集排行榜 (set:) ==========
    SET_TOTAL("rank:set:total", "题集总排行榜"),
    SET_WEEKLY("rank:set:weekly", "题集周排行榜"),
    SET_MONTHLY("rank:set:monthly", "题集月排行榜"),
    SET_DAILY("rank:set:daily", "题集日排行榜"),

    // ========== 分类排行榜 (cate:) ==========
    CATE_ALGORITHM("rank:cate:algorithm", "算法分类排行榜"),
    CATE_DATABASE("rank:cate:database", "数据库分类排行榜"),
    CATE_SYSTEM("rank:cate:system", "系统设计排行榜"),
    CATE_SHELL("rank:cate:shell", "Shell分类排行榜"),
    CATE_CONCURRENCY("rank:cate:concurrency", "并发分类排行榜"),

    // ========== 难度排行榜 (diff:) ==========
    DIFF_EASY("rank:diff:easy", "简单难度排行榜"),
    DIFF_MEDIUM("rank:diff:medium", "中等难度排行榜"),
    DIFF_HARD("rank:diff:hard", "困难难度排行榜"),

    // ========== 竞赛排行榜 (contest:) ==========
    CONTEST_WEEKLY("rank:contest:weekly", "周赛排行榜"),
    CONTEST_BIWEEKLY("rank:contest:biweekly", "双周赛排行榜"),
    CONTEST_MONTHLY("rank:contest:monthly", "月赛排行榜"),
    CONTEST_SEASONAL("rank:contest:seasonal", "季度赛排行榜"),

    // ========== 成就排行榜 (achv:) ==========
    ACHV_SOLVED("rank:achv:solved", "解题数量排行榜"),
    ACHV_STREAK("rank:achv:streak", "连续打卡排行榜"),
    ACHV_SUBMISSION("rank:achv:submission", "提交量排行榜"),
    ACHV_ACCEPTANCE("rank:achv:acceptance", "通过率排行榜"),

    // ========== 实时排行榜 (realtime:) ==========
    REALTIME_ACTIVE("rank:realtime:active", "实时活跃用户榜"),
    REALTIME_SUBMIT("rank:realtime:submit", "实时提交榜"),
    REALTIME_ONLINE("rank:realtime:online", "实时在线用户榜"),

    REALTIME_SET_ACTIVE("rank:realtime:set:active", "实时活跃用户榜"),
    REALTIME_SET_SUBMIT("rank:realtime:set:submit", "实时提交榜"),
    REALTIME_SET_ONLINE("rank:realtime:set:online", "实时在线用户榜"),

    // ========== 语言排行榜 (lang:) ==========
    LANG("rank:lang", "语言排行榜"),

    // ========== 尝试次数排行榜 (try:) ==========
    TRY("rank:try", "尝试排行榜"),
    SET_TRY("rank:set:try", "尝试排行榜"),

    // ========== 时间范围排行榜 (period:) ==========
    PERIOD_7DAYS("rank:period:7days", "近7天排行榜"),
    PERIOD_30DAYS("rank:period:30days", "近30天排行榜"),
    PERIOD_90DAYS("rank:period:90days", "近90天排行榜"),
    PERIOD_365DAYS("rank:period:365days", "近一年排行榜"),

    // ========== 通过排行榜 (accept:) ==========
    ACCEPT("rank:accept", "通过排行榜"),
    SET_ACCEPT("rank:set:accept", "通过排行榜"),

    HOT_SET_PROBLEM("rank:hot:set:problem", "热门题目排行榜"),
    HOT_PROBLEM("rank:hot:problem", "热门题目排行榜"),
    ;
    private final String value;

    private final String label;
}

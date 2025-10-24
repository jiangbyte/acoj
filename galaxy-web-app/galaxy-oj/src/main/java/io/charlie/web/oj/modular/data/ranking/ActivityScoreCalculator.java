package io.charlie.web.oj.modular.data.ranking;

/**
 * 活跃度计算公式：
 * 登录：+5分
 * 提交代码：+2分
 * 解题成功：+10分
 * 每日上限：50分（防止刷分）
 * 每月自动重置
 */
public class ActivityScoreCalculator {
    public static final int LOGIN_SCORE = 5;
    public static final int SUBMIT_SCORE = 2;
    public static final int SOLVE_SCORE = 10;
    public static final int DAILY_LIMIT = 50;

    public static final String LOGIN = "LOGIN";
    public static final String SUBMIT = "SUBMIT";

    public static int calculateScore(String actionType, boolean isSolved) {
        int score = 0;
        switch (actionType) {
            case LOGIN:
                score = LOGIN_SCORE;
                break;
            case SUBMIT:
                score = SUBMIT_SCORE + (isSolved ? SOLVE_SCORE : 0);
                break;
            default:
                score = 0;
        }
        return Math.min(score, DAILY_LIMIT);
    }
}
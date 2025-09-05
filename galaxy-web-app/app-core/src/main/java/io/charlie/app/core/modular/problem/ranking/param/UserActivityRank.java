package io.charlie.app.core.modular.problem.ranking.param;

import lombok.Data;

// 排名结果类
@Data
public class UserActivityRank {
    private String userId;
    private Long rank;
    private Double score;
}
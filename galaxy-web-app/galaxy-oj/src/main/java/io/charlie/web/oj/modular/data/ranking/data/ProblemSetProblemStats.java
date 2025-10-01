package io.charlie.web.oj.modular.data.ranking.data;

import lombok.Data;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 01/10/2025
 * @description TODO
 */
@Data
public class ProblemSetProblemStats {
    private Long submitCount;
    private Long acceptCount;
    private Long participantCount;
    private Long attemptCount;
    private Double acceptRate;
}

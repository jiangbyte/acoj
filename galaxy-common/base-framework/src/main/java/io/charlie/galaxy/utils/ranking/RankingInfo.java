package io.charlie.galaxy.utils.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description 排行榜数据信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingInfo {
    private String rankingKey;
    private String entityId;
    private Double score;
    private Long rank;
    private Date updateTime;
}
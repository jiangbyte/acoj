package io.charlie.web.oj.modular.data.ranking.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 排行榜项
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankItem {
    private Long rank;      // 排行序号
    private Double score;   // 分值
    private String id;      // ID
}
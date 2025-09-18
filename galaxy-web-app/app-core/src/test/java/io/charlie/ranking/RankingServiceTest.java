package io.charlie.ranking;

import io.charlie.app.core.CoreApplication;
import io.charlie.galaxy.utils.ranking.RankingInfo;
import io.charlie.galaxy.utils.ranking.RankingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description TODO
 */
@SpringBootTest(classes = CoreApplication.class)
public class RankingServiceTest {

    @Autowired
    private RankingUtil rankingUtil;

    @Test
    public void testRanking() {
        // 添加分数（使用当前时间）
        rankingUtil.addOrUpdate("daily_ranking", "user123", 100.0);
        rankingUtil.addOrUpdate("daily_ranking", "user124", 101.0);
        rankingUtil.addOrUpdate("daily_ranking", "user125", 102.0);
        rankingUtil.addOrUpdate("daily_ranking", "user126", 90.0);

        // 查询个人排名
        RankingInfo info = rankingUtil.getRanking("daily_ranking", "user123");

        System.out.println(info);
        // 自增分数
        rankingUtil.addOrUpdateAutoScore("daily_ranking", "user126", 90.0);
        // 查询个人排名
        RankingInfo info1 = rankingUtil.getRanking("daily_ranking", "user126");
        System.out.println(info1);

    }
}

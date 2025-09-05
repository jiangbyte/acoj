package io.charlie.ranking;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.CoreApplication;
import io.charlie.app.core.modular.problem.ranking.entity.ProblemUserRanking;
import io.charlie.app.core.modular.problem.ranking.mapper.ProblemUserRankingMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 02/08/2025
 * @description Mapper测试类
 */
@SpringBootTest(classes = CoreApplication.class)
public class MapperTest {
    @Autowired
    private ProblemUserRankingMapper problemRankingMapper;

    @Test
    public void test() {
        QueryWrapper<ProblemUserRanking> queryWrapper = new QueryWrapper<ProblemUserRanking>().checkSqlInjection();
        queryWrapper.lambda().eq(ProblemUserRanking::getUserId, "1");
        System.out.println(problemRankingMapper.selectTotalRankingPage(Page.of(1, 10), queryWrapper));
    }
}

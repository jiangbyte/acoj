package io.charlie.web.oj.modular.data.ranking.config;

import io.charlie.galaxy.utils.ranking.RankingUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description TODO
 */
@Configuration
public class RankingUtilConfig {
    @Bean
    public RankingUtil rankingUtil() {
        return new RankingUtil();
    }
}

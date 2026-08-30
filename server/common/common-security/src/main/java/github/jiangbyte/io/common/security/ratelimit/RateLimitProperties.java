package github.jiangbyte.io.common.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 限流配置。
 *
 * Author: Charlie
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "hei.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private int defaultPermits = 60;

    private int defaultWindowSeconds = 60;

    /** 可选 URL 规则（Ant 风格 path）。 */
    private List<Rule> rules = new ArrayList<>();

    @Getter
    @Setter
    public static class Rule {
        private String path = "";
        private int permits = 60;
        private int windowSeconds = 60;
    }
}

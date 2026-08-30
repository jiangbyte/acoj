package github.jiangbyte.io.sys.modules.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务调度配置属性：含执行日志保留策略。
 *
 * Author: Charlie
 */
@Data
@ConfigurationProperties(prefix = "hei.job")
public class HeiJobProperties {

    private Log log = new Log();

    @Data
    public static class Log {
        /** 执行日志保留天数；小于等于 0 表示跳过清理。 */
        private int retentionDays = 30;

        /** 单次删除上限。 */
        private int batchSize = 1000;
    }
}

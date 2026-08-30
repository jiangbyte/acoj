package github.jiangbyte.io.common.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志模块配置属性：包含审计配置。
 *
 * Author: Charlie
 */
@Data
@ConfigurationProperties(prefix = "hei.log")
public class HeiLogProperties {

    private Audit audit = new Audit();

    @Data
    public static class Audit {
        /** 是否启用异步审计（Redis Stream） */
        private boolean asyncEnabled = true;

        /** Redis Stream 的 Key */
        private String streamKey = "hei:audit:stream";

        /** 消费者组名称 */
        private String groupName = "hei-audit-group";

        /**
         * Stream 近似长度上限（XADD MAXLEN ~）。
         * 默认提高以避免裁剪未 ACK 消息；主要依赖 publisher 侧 MAXLEN，而非定时 XTRIM。
         */
        private int maxLen = 100000;

        /** 是否启用消费（处理消息落库） */
        private boolean consumeEnabled = true;

        /**
         * 是否启用消费者定时 XTRIM。
         * 默认关闭：避免误伤未 ACK 消息；长度控制交给 XADD MAXLEN ~。
         */
        private boolean trimEnabled = false;

        /** 定时修剪间隔（毫秒）；仅当 trimEnabled=true 时生效 */
        private long trimIntervalMs = 60000;

        /** Pending 消息空闲超过该毫秒数后 XCLAIM 重投（默认 60s） */
        private long reclaimIdleMs = 60000;

        /** Pending reclaim 扫描间隔（毫秒） */
        private long reclaimIntervalMs = 30000;

        /** Outbox 单次 relay 批量大小 */
        private int outboxRelayBatchSize = 50;

        /** Outbox 最大投递次数，超出进死信 */
        private int outboxMaxAttempts = 10;

        /** Outbox relay 间隔（毫秒） */
        private long outboxRelayIntervalMs = 2000;

        /** Outbox CLAIMED 超时后重新认领（毫秒） */
        private long outboxClaimStaleMs = 120000;

        /** 登录/登出审计保留天数；小于等于 0 表示跳过清理。 */
        private int loginRetentionDays = 180;

        /** 操作审计保留天数（不含 login/logout）；小于等于 0 表示跳过清理。 */
        private int operationRetentionDays = 365;

        /** 审计日志清理单次删除上限。 */
        private int cleanupBatchSize = 1000;
    }
}

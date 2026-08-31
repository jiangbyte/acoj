package github.jiangbyte.io.oj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OJ 判题运行时配置（对应 application 中 hei.oj.*）。
 *
 * Author: Charlie
 */
@Data
@ConfigurationProperties(prefix = "hei.oj")
public class OjProperties {

    private final Judge judge = new Judge();

    @Data
    public static class Judge {
        private int workerConcurrency = 4;
        private int caseParallelism = 4;
        private boolean stopOnFirstError = true;
        private int realTimeFactor = 3;
        private int leaseSeconds = 180;
        private int maxDispatchPerSubmission = 3;
        private long maxWaitMs = 600_000L;
        /** 兼容旧配置；主动心跳模式下改为 stale 扫描间隔参考，见 heartbeatStaleScanMs。 */
        private long heartbeatIntervalMs = 5_000L;
        private int heartbeatFailThreshold = 3;
        /** 被动心跳超时：超过该间隔未收到心跳则 OFFLINE（默认 90s）。 */
        private long heartbeatStaleMs = 90_000L;
        /** 扫描 stale 节点的调度间隔。 */
        private long heartbeatStaleScanMs = 15_000L;
        /** 入站心跳时间戳允许偏差（秒）。 */
        private long authSkewSeconds = 300L;
        private int circuitFailThreshold = 5;
        private long circuitOpenMs = 30_000L;
        private int inlineCaseMaxBytes = 262_144;
        private int sandboxRawMaxBytes = 65_536;
        /** 与 SparkSandbox signing_secret 相同的明文密钥（P0）。 */
        private String defaultSigningSecret = "";
        /** 试跑建议时限系数：suggested = max(1000, ceil(max_time * factor))。 */
        private double dryRunTimeFactor = 3.0;
        /** 试跑建议内存系数：suggested = max(题面内存, ceil(max_mem * factor))，再对齐 1MiB。 */
        private double dryRunMemoryFactor = 2.0;
        /** 宽松试跑 CPU 时限（毫秒）。 */
        private int dryRunRelaxedCpuTimeMs = 30_000;
        /** 宽松试跑内存限额（字节），默认 1GiB。 */
        private long dryRunRelaxedMemoryBytes = 1_073_741_824L;
        private final Mq mq = new Mq();
    }

    @Data
    public static class Mq {
        private String exchange = "oj.judge";
        private String workQueue = "oj.judge.work";
        private String retryQueue = "oj.judge.retry";
        private String dlq = "oj.judge.dlq";
        private int prefetch = 4;
    }
}

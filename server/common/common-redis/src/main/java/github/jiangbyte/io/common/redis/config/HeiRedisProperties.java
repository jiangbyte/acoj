package github.jiangbyte.io.common.redis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 模块配置属性：Redisson 连接池与超时等。
 *
 * Author: Charlie
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "hei.redis")
public class HeiRedisProperties {

    private int threads = 16;

    private int nettyThreads = 32;

    /** Redis 命令超时（毫秒）。 */
    private int timeout = 3000;

    /** TCP 连接超时（毫秒）。 */
    private int connectTimeout = 10000;

    /** 空闲连接回收超时（毫秒）。 */
    private int idleConnectionTimeout = 10000;

    private int connectionPoolSize = 64;

    private int connectionMinimumIdleSize = 24;

    private int subscriptionConnectionPoolSize = 50;

    private int subscriptionConnectionMinimumIdleSize = 1;

    /** 保活 Ping 间隔（毫秒）；0 表示关闭。 */
    private int pingConnectionInterval = 30_000;

    private boolean keepAlive = true;

    private boolean tcpNoDelay = true;

}

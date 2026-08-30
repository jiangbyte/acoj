package github.jiangbyte.io.common.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 结构化日志配置属性：JSON/键值格式开关、service 与脱敏键。
 *
 * Author: Charlie
 */
@Data
@ConfigurationProperties(prefix = "hei.logging")
public class HeiLoggingProperties {

    /**
     * true：控制台/文件使用 Logstash JSON；false：键值结构化格式。
     * 绑定自 {@code hei.logging.json} / 环境变量 {@code LOG_JSON}。
     */
    private boolean json = true;

    /** service 字段（为空时默认 spring.application.name）。 */
    private String service = "";

    /** service_version 字段（见 {@code hei.logging.service-version}）。 */
    private String serviceVersion = "";

    /** 日志 MDC/字段脱敏键名（小写匹配，含连字符/下划线变体）。 */
    private List<String> redactKeys = new ArrayList<>(List.of(
            "password", "secret", "token", "cryptoKey", "crypto-key",
            "accessKey", "access-key", "privateKey", "private-key"));
}

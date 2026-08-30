package github.jiangbyte.io.common.log.config;

import github.jiangbyte.io.common.log.format.HeiConsoleKeyValueStructuredLogFormatter;
import github.jiangbyte.io.common.log.format.HeiKeyValueStructuredLogFormatter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 日志 ApplicationContext 初始化器：根据 hei.logging 将结构化日志格式写入 Environment。
 * <p>
 * 通过 {@code META-INF/spring/org.springframework.context.ApplicationContextInitializer.imports} 注册，
 * 不是 {@code EnvironmentPostProcessor}。
 *
 * Author: Charlie
 */
public class HeiLoggingApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    public static final String PROPERTY_SOURCE = "heiLoggingStructuredFormat";
    private static final String CONSOLE_KEY_VALUE_FORMATTER =
            HeiConsoleKeyValueStructuredLogFormatter.class.getName();
    private static final String FILE_KEY_VALUE_FORMATTER = HeiKeyValueStructuredLogFormatter.class.getName();

    /** 启动早期根据配置调整日志相关 Environment 属性。 */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        boolean json = resolveJson(environment);
        // 仅在用户要求键值控制台时覆盖；JSON 为 application.yml 默认。
        if (json) {
            return;
        }
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("logging.structured.format.console", CONSOLE_KEY_VALUE_FORMATTER);
        props.put("logging.structured.format.file", FILE_KEY_VALUE_FORMATTER);
        props.put("logging.structured.json.context.include", "true");
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, props));
        // LoggingSystem 也会读取这些早期系统属性。
        System.setProperty("CONSOLE_LOG_STRUCTURED_FORMAT", CONSOLE_KEY_VALUE_FORMATTER);
        System.setProperty("FILE_LOG_STRUCTURED_FORMAT", FILE_KEY_VALUE_FORMATTER);
    }

    static boolean resolveJson(ConfigurableEnvironment environment) {
        Boolean hei = environment.getProperty("hei.logging.json", Boolean.class);
        if (hei != null) {
            return hei;
        }
        Boolean logJson = environment.getProperty("LOG_JSON", Boolean.class);
        if (logJson != null) {
            return logJson;
        }
        return true;
    }

    /** 返回初始化器优先级。 */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}

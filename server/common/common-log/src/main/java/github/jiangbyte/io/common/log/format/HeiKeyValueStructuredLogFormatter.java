package github.jiangbyte.io.common.log.format;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import github.jiangbyte.io.common.core.sensitive.SensitiveKeys;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.logging.structured.StructuredLogFormatter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * 键值结构化日志格式化器：输出与 fastapi 对齐的可读键值行，并对敏感 MDC 打码。
 * <p>
 * 控制台请用 {@link HeiConsoleKeyValueStructuredLogFormatter}（带 ANSI 级别色）；
 * 文件保持本类无色，避免写入转义码。
 *
 * Author: Charlie
 */
public class HeiKeyValueStructuredLogFormatter implements StructuredLogFormatter<ILoggingEvent> {

    private static final DateTimeFormatter ISO_UTC =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").withZone(ZoneOffset.UTC);

    private static final Set<String> REDACT_KEYS = SensitiveKeys.DEFAULT;

    private final boolean colorLevel;

    public HeiKeyValueStructuredLogFormatter() {
        this(false);
    }

    protected HeiKeyValueStructuredLogFormatter(boolean colorLevel) {
        this.colorLevel = colorLevel;
    }

    /** 将日志事件格式化为键值结构化文本。 */
    @Override
    public String format(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(ISO_UTC.format(Instant.ofEpochMilli(event.getTimeStamp())));
        sb.append(" [").append(formatLevel(event.getLevel())).append("] ");
        sb.append(event.getFormattedMessage());
        sb.append(" [").append(shortLogger(event.getLoggerName())).append(']');

        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null) {
            mdc.entrySet().stream()
                    .filter(e -> hasText(e.getValue()))
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> appendKv(sb, e.getKey(), redact(e.getKey(), e.getValue())));
        }
        if (event.getKeyValuePairs() != null) {
            event.getKeyValuePairs().forEach(pair -> {
                if (pair != null && hasText(String.valueOf(pair.value))) {
                    appendKv(sb, pair.key, redact(pair.key, String.valueOf(pair.value)));
                }
            });
        }
        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            sb.append('\n').append(ThrowableProxyUtil.asString(throwable));
        }
        sb.append('\n');
        return sb.toString();
    }

    private String formatLevel(Level level) {
        String name = level != null ? level.toString().toLowerCase() : "info";
        if (!colorLevel || level == null) {
            return name;
        }
        AnsiColor color = switch (level.toInt()) {
            case Level.ERROR_INT -> AnsiColor.RED;
            case Level.WARN_INT -> AnsiColor.YELLOW;
            case Level.INFO_INT -> AnsiColor.GREEN;
            case Level.DEBUG_INT -> AnsiColor.BLUE;
            case Level.TRACE_INT -> AnsiColor.CYAN;
            default -> AnsiColor.DEFAULT;
        };
        return AnsiOutput.toString(color, name);
    }

    private static String redact(String key, String value) {
        return SensitiveKeys.matches(key, REDACT_KEYS) ? "***" : value;
    }

    private static String shortLogger(String name) {
        if (name == null || name.isBlank()) {
            return "-";
        }
        int dot = name.lastIndexOf('.');
        return dot >= 0 && dot < name.length() - 1 ? name.substring(dot + 1) : name;
    }

    private static void appendKv(StringBuilder sb, String key, String value) {
        sb.append(' ').append(key).append('=');
        if (value.indexOf(' ') >= 0 || value.indexOf('=') >= 0) {
            sb.append('\'').append(value.replace("'", "\\'")).append('\'');
        } else {
            sb.append(value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank() && !"-".equals(value);
    }
}

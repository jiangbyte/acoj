package github.jiangbyte.io.common.log.format;

/**
 * 控制台键值日志：在级别上输出 ANSI 颜色（依赖 {@code spring.output.ansi.enabled}）。
 *
 * Author: Charlie
 */
public class HeiConsoleKeyValueStructuredLogFormatter extends HeiKeyValueStructuredLogFormatter {

    public HeiConsoleKeyValueStructuredLogFormatter() {
        super(true);
    }
}

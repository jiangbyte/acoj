package github.jiangbyte.io.common.core.security;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 对存储型 HTML 做轻量消毒（剥离 script / 事件属性 / 危险 URL）。
 *
 * Author: Charlie
 */
public final class HtmlSanitizer {

    private static final Pattern SCRIPT = Pattern.compile("(?is)<script\\b[^>]*>.*?</script\\s*>");
    private static final Pattern STYLE = Pattern.compile("(?is)<style\\b[^>]*>.*?</style\\s*>");
    private static final Pattern IFRAME = Pattern.compile("(?is)<iframe\\b[^>]*>.*?</iframe\\s*>");
    private static final Pattern OBJECT = Pattern.compile("(?is)<object\\b[^>]*>.*?</object\\s*>");
    private static final Pattern EMBED = Pattern.compile("(?is)<embed\\b[^>]*/?>");
    private static final Pattern EVENT_ATTR =
            Pattern.compile("(?i)\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");
    private static final Pattern DANGER_HREF = Pattern.compile(
            "(?i)\\s(href|src|xlink:href|action|formaction)\\s*=\\s*(\"|')\\s*(javascript|data|vbscript)\\s*:");

    private HtmlSanitizer() {
    }

    public static String sanitize(String contentType, String content) {
        if (content == null) {
            return null;
        }
        if (contentType == null || !"HTML".equalsIgnoreCase(contentType.trim())) {
            return content;
        }
        String out = content;
        out = SCRIPT.matcher(out).replaceAll("");
        out = STYLE.matcher(out).replaceAll("");
        out = IFRAME.matcher(out).replaceAll("");
        out = OBJECT.matcher(out).replaceAll("");
        out = EMBED.matcher(out).replaceAll("");
        out = EVENT_ATTR.matcher(out).replaceAll("");
        out = DANGER_HREF.matcher(out).replaceAll(" $1=$2#blocked");
        return out;
    }
}

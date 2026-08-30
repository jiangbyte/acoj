package github.jiangbyte.io.sys.modules.file.support;

import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Content-Disposition 构建（ASCII fallback + RFC5987 filename*）。
 *
 * Author: Charlie
 */
public final class ContentDispositions {

    private ContentDispositions() {
    }

    public static String attachment(String originalName) {
        String name = StringUtils.hasText(originalName) ? originalName.trim() : "download";
        String ascii = name.replaceAll("[^\\x20-\\x7E]", "_").replace("\"", "");
        if (!StringUtils.hasText(ascii)) {
            ascii = "download";
        }
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded;
    }
}

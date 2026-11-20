package io.charlie.web.modular.sys.file.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 判断 / 解析存储对象名（库中存对象名，接口输出访问 URL）。
 */
public final class StorageObjectNames {

    private static final Pattern FILE_NAME = Pattern.compile(
            "(?i)^[\\w.-]+\\.(jpg|jpeg|png|gif|webp|svg|ico|bmp|pdf|zip|rar|mp4|mp3|wav|doc|docx|xls|xlsx|txt|md)$"
    );

    private StorageObjectNames() {
    }

    public static String stripQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        int q = value.indexOf('?');
        return q >= 0 ? value.substring(0, q) : value;
    }

    public static boolean isHttpUrl(String value) {
        return StringUtils.hasText(value)
                && (value.startsWith("http://") || value.startsWith("https://"));
    }

    /**
     * 是否像存储对象名或带文件名的路径（含历史 /content/xxx.jpg）
     */
    public static boolean looksLikeStoredFile(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return FILE_NAME.matcher(extractFileName(stripQuery(value))).matches();
    }

    /**
     * 取路径最后一段文件名
     */
    public static String extractFileName(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String cleaned = stripQuery(value);
        // 去掉末尾斜杠
        while (cleaned.endsWith("/") || cleaned.endsWith("\\")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        int slash = Math.max(cleaned.lastIndexOf('/'), cleaned.lastIndexOf('\\'));
        return slash >= 0 ? cleaned.substring(slash + 1) : cleaned;
    }
}

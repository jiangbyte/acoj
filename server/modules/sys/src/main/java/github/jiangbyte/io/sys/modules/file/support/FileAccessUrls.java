package github.jiangbyte.io.sys.modules.file.support;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Locale;

/**
 * 文件对象名规范化与访问 URL 解析（统一走 FileService.resolveAccessUrl）。
 *
 * Author: Charlie
 */
@Component
public class FileAccessUrls {

    private final ObjectProvider<github.jiangbyte.io.sys.modules.file.service.FileService> fileServiceProvider;

    public FileAccessUrls(
            ObjectProvider<github.jiangbyte.io.sys.modules.file.service.FileService> fileServiceProvider) {
        this.fileServiceProvider = fileServiceProvider;
    }

    public boolean isExternalUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            String scheme = URI.create(value.trim()).getScheme();
            if (scheme == null) {
                return false;
            }
            String lower = scheme.toLowerCase(Locale.ROOT);
            return "http".equals(lower) || "https".equals(lower) || "data".equals(lower) || "blob".equals(lower);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    /** 是否疑似预签名 / 临时存储 URL（不可当永久地址透传）。 */
    public boolean looksLikePresignedUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.contains("x-amz-")
                || lower.contains("x-oss-")
                || lower.contains("signature=")
                || lower.contains("x-goog-signature");
    }

    /** 规范化对象名（纯 object key）；外部 URL 原样返回。 */
    public String normalizeObjectName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String raw = value.trim();
        if (isExternalUrl(raw)) {
            return raw;
        }
        return stripToObjectKey(raw);
    }

    /**
     * 把任意形式的对象引用转成纯 object key（用于存储引擎删除/加载）。
     */
    public String toObjectKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (isExternalUrl(value)) {
            try {
                URI uri = URI.create(value.trim());
                return stripToObjectKey(uri.getPath());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return stripToObjectKey(value);
    }

    /**
     * path-style URL 常为 /{bucket}/{key}：若第二段起像业务 key（uploads/），去掉首段 bucket。
     */
    public String stripToObjectKey(String pathOrKey) {
        if (!StringUtils.hasText(pathOrKey)) {
            return null;
        }
        String normalized = pathOrKey.replace('\\', '/').replaceAll("^/+", "");
        if (normalized.startsWith("api/v1/files/")) {
            normalized = normalized.substring("api/v1/files/".length());
        }
        int slash = normalized.indexOf('/');
        if (slash > 0) {
            String rest = normalized.substring(slash + 1);
            if (rest.startsWith("uploads/")) {
                normalized = rest;
            }
        }
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    /** 解析可浏览器访问的 URL（公开直连或预签名）。 */
    public String resolveFileUrl(String value) {
        github.jiangbyte.io.sys.modules.file.service.FileService fileService = fileServiceProvider.getIfAvailable();
        if (fileService != null) {
            return fileService.resolveAccessUrl(value);
        }
        return null;
    }
}

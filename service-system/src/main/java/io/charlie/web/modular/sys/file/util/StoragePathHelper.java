package io.charlie.web.modular.sys.file.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.charlie.web.modular.sys.file.config.StorageServiceFactory;
import io.charlie.web.modular.sys.file.service.StorageService;

/**
 * 业务侧规范化存储路径：写入对象名，读出访问 URL。
 */
public final class StoragePathHelper {

    private StoragePathHelper() {
    }

    public static String toObjectName(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        try {
            return storage().toObjectName(value);
        } catch (Exception e) {
            return StorageObjectNames.looksLikeStoredFile(value)
                    ? StorageObjectNames.extractFileName(value)
                    : value;
        }
    }

    public static String toAccessUrl(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        try {
            return storage().toAccessUrl(value);
        } catch (Exception e) {
            return value;
        }
    }

    private static StorageService storage() {
        return SpringUtil.getBean(StorageServiceFactory.class).getStorageService();
    }
}

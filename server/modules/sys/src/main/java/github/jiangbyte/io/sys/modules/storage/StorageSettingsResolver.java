package github.jiangbyte.io.sys.modules.storage;

/**
 * 存储配置解析接口：从 sys_config / RuntimeSettings 解析当前存储。
 *
 * Author: Charlie
 */
public interface StorageSettingsResolver {

    /** 解析默认存储配置。 */
    ResolvedStorageConfig resolveDefault();

    /** 按存储提供方解析配置。 */
    ResolvedStorageConfig resolve(String storageProvider);
}

package github.jiangbyte.io.sys.config;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 进程级可热更新 {@link RuntimeSettings} 持有者：绑定加载器、读取当前快照、触发 reload。
 * 由 sys 模块在启动时装配。
 *
 * Author: Charlie
 */
public final class RuntimeSettingsHolder {

    private static final AtomicReference<RuntimeSettings> CURRENT =
            new AtomicReference<>(new RuntimeSettings(Map.of(), 0L));
    private static volatile Supplier<Map<String, String>> LOADER;

    private RuntimeSettingsHolder() {
    }

    /** 绑定配置加载器并立即 reload。 */
    public static void bindLoader(Supplier<Map<String, String>> loader) {
        LOADER = loader;
        reload();
    }

    /** 获取当前运行时配置快照。 */
    public static RuntimeSettings get() {
        return CURRENT.get();
    }

    /** 从已绑定加载器刷新快照并递增版本号。 */
    public static void reload() {
        Supplier<Map<String, String>> loader = LOADER;
        if (loader == null) {
            return;
        }
        Map<String, String> snap = loader.get();
        long next = CURRENT.get().version() + 1;
        CURRENT.set(new RuntimeSettings(snap == null ? Map.of() : snap, next));
    }
}

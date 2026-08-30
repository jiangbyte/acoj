package github.jiangbyte.io.sys.modules.config.support;

import github.jiangbyte.io.common.notify.NotifyConfigSource;
import github.jiangbyte.io.sys.config.RuntimeSettingsHolder;
import org.springframework.stereotype.Component;

/**
 * RuntimeSettings 通知型配置源：监听配置变更并刷新。
 *
 * Author: Charlie
 */
@Component
public class RuntimeSettingsNotifyConfigSource implements NotifyConfigSource {

    @Override
    public String get(String key) {
        return RuntimeSettingsHolder.get().get(key);
    }

    @Override
    public String get(String key, String def) {
        return RuntimeSettingsHolder.get().get(key, def);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return RuntimeSettingsHolder.get().getBoolean(key, def);
    }

    @Override
    public int getInt(String key, int def) {
        return RuntimeSettingsHolder.get().getInt(key, def);
    }
}

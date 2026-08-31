package github.jiangbyte.io.sys.modules.storage;

import github.jiangbyte.io.sys.modules.config.support.ConfigChangeNotifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置热重载后刷新存储引擎缓存，避免 DEFAULT_FILE_ENGINE 切换仍走旧引擎。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class StorageEngineRefreshSupport {

    private final StorageEngineFactory storageEngineFactory;
    private final ConfigChangeNotifier configChangeNotifier;

    @PostConstruct
    void init() {
        configChangeNotifier.onAfterReload(ignored -> storageEngineFactory.refresh());
    }
}

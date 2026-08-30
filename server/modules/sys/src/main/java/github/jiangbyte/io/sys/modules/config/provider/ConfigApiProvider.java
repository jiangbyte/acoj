package github.jiangbyte.io.sys.modules.config.provider;

import github.jiangbyte.io.sys.config.ConfigApi;
import github.jiangbyte.io.sys.modules.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 跨模块 ConfigApi 适配器：对外暴露配置读写能力。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ConfigApiProvider implements ConfigApi {

    private final ConfigService configService;

    @Override
    public String getValue(String key) {
        return configService.getValue(key);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return configService.getValue(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return configService.getBoolean(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return configService.getInt(key, defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return configService.getLong(key, defaultValue);
    }

    @Override
    public Map<String, String> snapshot() {
        return configService.snapshot();
    }
}

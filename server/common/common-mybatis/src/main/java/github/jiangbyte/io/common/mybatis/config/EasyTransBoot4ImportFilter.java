package github.jiangbyte.io.common.mybatis.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

/**
 * Easy-Trans Boot4 导入过滤器：剔除与当前 Boot 版本不兼容的自动配置类。
 *
 * Author: Charlie
 */
public class EasyTransBoot4ImportFilter implements AutoConfigurationImportFilter {

    /**
     * easy-trans 3.1.4 官方自动配置类（与 Boot 4 不兼容，见类注释）。
     */
    static final String LEGACY_TRANS_SERVICE_CONFIG = "org.dromara.trans.config.TransServiceConfig";

    /** 判断是否保留该自动配置导入。 */
    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String candidate = autoConfigurationClasses[i];
            // null 槽位保持 true，交给后续过滤器处理；仅剔除官方 TransServiceConfig。
            matches[i] = candidate == null || !LEGACY_TRANS_SERVICE_CONFIG.equals(candidate);
        }
        return matches;
    }
}

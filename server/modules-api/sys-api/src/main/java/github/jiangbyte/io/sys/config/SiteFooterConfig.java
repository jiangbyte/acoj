package github.jiangbyte.io.sys.config;

import org.springframework.util.StringUtils;

/**
 * 从系统配置解析站点页脚（版权 + 备案）。
 *
 * Author: Charlie
 */
public final class SiteFooterConfig {

    private SiteFooterConfig() {
    }

    public static SiteFooterResult resolve(ConfigApi config) {
        SiteFooterResult result = new SiteFooterResult();
        if (config == null) {
            return result;
        }
        result.setCopyrightText(trim(config.getValue("COPYRIGHT_TEXT", "")));
        result.setCopyrightUrl(trim(config.getValue("COPYRIGHT_URL", "")));
        result.setIcpNumber(trim(config.getValue("SITE_ICP_NUMBER", "")));
        result.setIcpUrl(trim(config.getValue("SITE_ICP_URL", "")));
        result.setPsbNumber(trim(config.getValue("SITE_PSB_NUMBER", "")));
        result.setPsbUrl(trim(config.getValue("SITE_PSB_URL", "")));
        return result;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /** 是否有任意可展示的页脚文案。 */
    public static boolean hasContent(SiteFooterResult footer) {
        if (footer == null) {
            return false;
        }
        return StringUtils.hasText(footer.getCopyrightText())
                || StringUtils.hasText(footer.getIcpNumber())
                || StringUtils.hasText(footer.getPsbNumber());
    }
}

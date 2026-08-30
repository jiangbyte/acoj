package github.jiangbyte.io.sys.config;

import java.util.Map;

/**
 * {@code sys_config} 跨模块只读 API：按 key 读取字符串/布尔/数值配置，或导出全量快照。
 * 业务配置的唯一运行时来源。
 *
 * Author: Charlie
 */
public interface ConfigApi {

    /** 读取配置值；不存在时返回 null。 */
    String getValue(String key);

    /** 读取配置值；不存在或空白时返回默认值。 */
    String getValue(String key, String defaultValue);

    /** 读取布尔配置；无法解析时返回默认值。 */
    boolean getBoolean(String key, boolean defaultValue);

    /** 读取 int 配置；无法解析时返回默认值。 */
    int getInt(String key, int defaultValue);

    /** 读取 long 配置；无法解析时返回默认值。 */
    long getLong(String key, long defaultValue);

    /** 全部非 null 配置值的快照（key → value）。 */
    Map<String, String> snapshot();
}

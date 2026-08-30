package github.jiangbyte.io.common.notify;

/**
 * 通知通道配置源：按键读取短信/邮件/推送等厂商配置。
 *
 * Author: Charlie
 */
public interface NotifyConfigSource {

    /** 按键读取配置值。 */
    String get(String key);

    /** 按键读取配置值。 */
    String get(String key, String def);

    boolean getBoolean(String key, boolean def);

    /** 按键读取整型配置。 */
    int getInt(String key, int def);
}

package io.charlie.galaxy.utils.str;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 31/07/2025
 * @description 增强版字符串工具类
 */
public class GaStringUtil {

    /**
     * 判断字符串是否为空（包括 null、""、"null"）
     *
     * @param str 输入字符串
     * @return true: 空/null/"null", false: 非空
     */
    public static boolean isEmpty(String str) {
        if (str == null) return true;
        String trimmed = str.trim();
        return trimmed.isEmpty() ||
                "null".equalsIgnoreCase(trimmed);
    }

    /**
     * 判断字符串是否非空（与 isEmpty 相反）
     *
     * @param str 输入字符串
     * @return true: 非空, false: 空/null/"null"
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 安全转换为字符串（避免 NPE，null → ""）
     *
     * @param obj 输入对象
     * @return 非空字符串或 ""
     */
    public static String toStringSafe(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * 去除首尾空格，并处理 "null" → null
     *
     * @param str 输入字符串
     * @return 修剪后的字符串或 null
     */
    public static String trimToNull(String str) {
        if (str == null) return null;
        String trimmed = str.trim();
        return trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) ? null : trimmed;
    }

    /**
     * 去除首尾空格，并处理 null → ""
     *
     * @param str 输入字符串
     * @return 修剪后的字符串或 ""
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }
}
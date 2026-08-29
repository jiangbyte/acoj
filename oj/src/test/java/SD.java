import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 29/10/2025
 * @description TODO
 */
public class SD {
    public static String convertToSemicolonSeparated(String input) {
        if (StrUtil.isBlank(input)) {
            return "";
        }

        try {
            // 清理字符串：移除外层多余的引号
            String cleaned = StrUtil.removeAll(input, "\"\"");
            cleaned = StrUtil.trim(cleaned);
            cleaned = StrUtil.removePrefix(cleaned, "\"");
            cleaned = StrUtil.removeSuffix(cleaned, "\"");

            // 如果是空数组，返回空字符串
            if (StrUtil.isBlank(cleaned) || "[]".equals(cleaned)) {
                return "";
            }

            // 解析JSON数组
            List<String> items = JSONUtil.parseArray(cleaned).toList(String.class);

            // 过滤空值并用分号连接
            return StrUtil.join(";", items.stream()
                    .filter(StrUtil::isNotBlank)
                    .toArray(String[]::new));

        } catch (Exception e) {
            // 解析失败时返回空字符串或根据业务需求处理
            return "";
        }
    }

    public static void main(String[] args) {
        // 测试各种情况
        String[] testCases = {
                "\"[\"\"522601950001473870\"\"]\"",
                "\"[\"\"299\"\",\"\"209\"\",\"\"207\"\"]\"",
                "",  // 空字符串
                null,  // null
                "\"[]\"",  // 空数组
                "[]",  // 空数组
                "invalid string"  // 无效字符串
        };

        for (String testCase : testCases) {
            String result = convertToSemicolonSeparated(testCase);
            System.out.println("输入: " + testCase + " -> 输出: " + result);
        }
    }
}

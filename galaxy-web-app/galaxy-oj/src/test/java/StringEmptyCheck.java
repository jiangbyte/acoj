import org.apache.commons.lang3.StringUtils;

public class StringEmptyCheck {
    public static void main(String[] args) {
        String str1 = null;
        String str2 = "null";
        String str3 = "NULL";
        String str4 = "   ";
        String str5 = "";
        String str6 = "abc";
        
        // 检查是否为null、空字符串或纯空格
        System.out.println(StringUtils.isBlank(str1)); // true
        System.out.println(StringUtils.isBlank(str2)); // false
        System.out.println(StringUtils.isBlank(str3)); // false
        System.out.println(StringUtils.isBlank(str4)); // true
        System.out.println(StringUtils.isBlank(str5)); // true
        System.out.println(StringUtils.isBlank(str6)); // false
        
        // 检查是否为null或空字符串（不包含空格检查）
        System.out.println(StringUtils.isEmpty(str1)); // true
        System.out.println(StringUtils.isEmpty(str4)); // false
    }
}
public class JavaStringUtils {
    
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    // 处理"null"字符串的特殊情况
    public static boolean isEffectivelyEmpty(String str) {
        if (str == null) return true;
        String trimmed = str.trim();
        return trimmed.isEmpty() || 
               "null".equalsIgnoreCase(trimmed);
    }
    
    public static void main(String[] args) {
        String str1 = null;
        String str2 = "null";
        String str3 = "NULL";
        String str4 = "   ";
        String str5 = "";
        String str6 = "abc";
        
        System.out.println(isEffectivelyEmpty(str1)); // true
        System.out.println(isEffectivelyEmpty(str2)); // true
        System.out.println(isEffectivelyEmpty(str3)); // true
        System.out.println(isEffectivelyEmpty(str4)); // true
        System.out.println(isEffectivelyEmpty(str5)); // true
        System.out.println(isEffectivelyEmpty(str6)); // false
    }
}
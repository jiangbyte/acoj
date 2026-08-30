package github.jiangbyte.io.sys.modules.codegen.support;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 代码生成命名工具：表名/实体名/包路径转换。
 *
 * Author: Charlie
 */
public final class CodegenNaming {

    private CodegenNaming() {
    }

    public static String snakeToCamel(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String[] parts = value.toLowerCase(Locale.ROOT).split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    public static String snakeToPascal(String value) {
        String camel = snakeToCamel(value);
        if (camel == null || camel.isEmpty()) {
            return camel;
        }
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    public static String packageFromModulePath(String modulePath) {
        String cleaned = modulePath == null ? "" : modulePath.trim().replace('\\', '/');
        List<String> parts = Arrays.stream(cleaned.split("/"))
                .filter(part -> !part.isBlank() && !".".equals(part))
                .map(part -> part.replace('-', '_').toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (parts.isEmpty()) {
            return "github.jiangbyte.io.biz.modules.biz";
        }
        String module = parts.get(0);
        String feature = parts.size() == 1 ? module : String.join(".", parts.subList(1, parts.size()));
        return "github.jiangbyte.io." + module + ".modules." + feature;
    }

    public static String apiPackageFromModulePath(String modulePath) {
        String cleaned = modulePath == null ? "" : modulePath.trim().replace('\\', '/');
        List<String> parts = Arrays.stream(cleaned.split("/"))
                .filter(part -> !part.isBlank() && !".".equals(part))
                .map(part -> part.replace('-', '_').toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (parts.isEmpty()) {
            return "github.jiangbyte.io.biz.biz";
        }
        String module = parts.get(0);
        String feature = parts.size() == 1 ? module : String.join(".", parts.subList(1, parts.size()));
        return "github.jiangbyte.io." + module + "." + feature;
    }

    public static String featureFromModulePath(String modulePath) {
        String cleaned = modulePath == null ? "" : modulePath.trim().replace('\\', '/');
        List<String> parts = Arrays.stream(cleaned.split("/"))
                .filter(part -> !part.isBlank() && !".".equals(part))
                .map(part -> part.replace('-', '_').toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (parts.isEmpty()) {
            return "biz";
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return String.join(".", parts.subList(1, parts.size()));
    }

    public static String dtoPackageFromModulePath(String modulePath) {
        return apiPackageFromModulePath(modulePath) + ".dto";
    }

    public static String pathFromPackage(String packageName) {
        return packageName.replace('.', '/');
    }

    public static String apiPathSegment(String businessName) {
        if (businessName == null || businessName.isBlank()) {
            return "resources";
        }
        return businessName.trim()
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replace('_', '-')
                .toLowerCase(Locale.ROOT);
    }
}

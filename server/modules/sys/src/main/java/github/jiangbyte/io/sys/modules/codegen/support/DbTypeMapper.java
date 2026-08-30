package github.jiangbyte.io.sys.modules.codegen.support;

/**
 * 数据库类型到语义类型 / 前端类型 / Java 类型映射工具。
 *
 * Author: Charlie
 */
public final class DbTypeMapper {

    private DbTypeMapper() {
    }

    /**
     * @return [dataType, frontendType]
     */
    public static String[] toDataAndFrontendType(String dataType, String udtName) {
        String raw = (udtName == null || udtName.isBlank() ? dataType : udtName).toLowerCase().trim();
        // MySQL COLUMN_TYPE 可能为 tinyint(1)、varchar(64) 等
        String type = raw;
        int paren = raw.indexOf('(');
        String base = paren > 0 ? raw.substring(0, paren) : raw;
        String args = paren > 0 ? raw.substring(paren) : "";

        if ("tinyint".equals(base) && args.startsWith("(1)")) {
            return new String[]{"bool", "boolean"};
        }

        return switch (base) {
            case "int2", "int4", "int8", "integer", "bigint", "smallint", "serial", "bigserial",
                 "int", "mediumint", "tinyint" -> new String[]{"int", "number"};
            case "numeric", "decimal", "float4", "float8", "double precision", "real", "money",
                 "double", "float" -> new String[]{"float", "number"};
            case "bool", "boolean" -> new String[]{"bool", "boolean"};
            case "json", "jsonb" -> new String[]{"dict", "Record<string, any>"};
            case "timestamp", "timestamptz", "timestamp without time zone", "timestamp with time zone",
                 "date", "time", "timetz", "datetime" -> new String[]{"datetime", "string"};
            default -> {
                // information_schema.data_type 常见值
                if (type.contains("timestamp") || type.equals("datetime") || type.equals("date") || type.equals("time")) {
                    yield new String[]{"datetime", "string"};
                }
                if (type.equals("json") || type.equals("jsonb")) {
                    yield new String[]{"dict", "Record<string, any>"};
                }
                yield new String[]{"str", "string"};
            }
        };
    }

    public static String toJavaType(String dataType) {
        if (dataType == null) {
            return "String";
        }
        return switch (dataType) {
            case "int" -> "Integer";
            case "float" -> "java.math.BigDecimal";
            case "bool" -> "Boolean";
            case "datetime" -> "java.time.OffsetDateTime";
            case "dict" -> "java.util.Map<String, Object>";
            default -> "String";
        };
    }

    public static String toSimpleJavaName(String javaType) {
        if (javaType == null) {
            return "String";
        }
        int idx = javaType.lastIndexOf('.');
        if (idx < 0) {
            return javaType;
        }
        // 保留泛型，如 Map<String, Object>
        if (javaType.contains("<")) {
            return javaType;
        }
        return javaType.substring(idx + 1);
    }
}

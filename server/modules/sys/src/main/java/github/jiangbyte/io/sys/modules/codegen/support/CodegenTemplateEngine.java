package github.jiangbyte.io.sys.modules.codegen.support;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;
import github.jiangbyte.io.common.mybatis.dialect.DbVendor;
import github.jiangbyte.io.common.mybatis.dialect.PostgreSqlDialect;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenField;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenPlan;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenPreviewFileResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 代码生成模板引擎封装：加载与渲染 Freemarker。
 *
 * Author: Charlie
 */
@Component
public class CodegenTemplateEngine {

    private static final Set<String> AUDIT = Set.of("created_at", "created_by", "updated_at", "updated_by");
    private static final Set<String> TREE_TYPES = Set.of("TREE", "LEFT_TREE_TABLE");
    private static final Set<String> SUB_TYPES = Set.of("LEFT_TREE_TABLE", "MASTER_DETAIL");

    private final Configuration configuration;
    private final DbDialect dbDialect;

    public CodegenTemplateEngine(DbDialect dbDialect) {
        this.dbDialect = dbDialect == null ? new PostgreSqlDialect() : dbDialect;
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "codegen/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        // 缺失字段按空/false 处理，避免 <#if field.dict_code> 一类判断因 null 失败
        cfg.setClassicCompatible(true);
        this.configuration = cfg;
    }

    public List<SysCodegenPreviewFileResult> render(
            SysCodegenPlan plan,
            List<SysCodegenField> mainFields,
            List<SysCodegenField> subFields) {
        Map<String, Object> ctx = buildContext(plan, mainFields, subFields == null ? List.of() : subFields);
        List<SysCodegenPreviewFileResult> files = new ArrayList<>();

        String moduleRoot = "module/" + firstModuleSegment(plan.getModulePath());
        String basePackage = (String) ctx.get("basePackage");
        String packagePath = CodegenNaming.pathFromPackage(basePackage);
        String paramPackage = (String) ctx.get("paramPackage");
        String paramPath = CodegenNaming.pathFromPackage(paramPackage);
        String entityName = plan.getEntityName();

        // 对齐 iam/dept：module 内 entity/mapper/convert/param/service(+impl)/controller
        files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/entity/" + entityName + ".java",
                "java", "Entity.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/mapper/" + entityName + "Mapper.java",
                "java", "Mapper.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/convert/" + entityName + "Convert.java",
                "java", "Convert.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + paramPath + "/" + entityName + "AddParam.java",
                "java", "AddParam.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + paramPath + "/" + entityName + "EditParam.java",
                "java", "EditParam.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + paramPath + "/" + entityName + "PageParam.java",
                "java", "PageParam.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/service/" + entityName + "Service.java",
                "java", "Service.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/service/impl/" + entityName + "ServiceImpl.java",
                "java", "ServiceImpl.java.ftl", javaModel(ctx, false)));
        files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/controller/Admin" + entityName + "Controller.java",
                "java", "Controller.java.ftl", javaModel(ctx, false)));

        boolean hasSub = Boolean.TRUE.equals(ctx.get("hasSub"));
        if (hasSub && StringUtils.hasText(plan.getSubEntityName())) {
            String subEntity = plan.getSubEntityName();
            Map<String, Object> subJava = javaModel(ctx, true);
            files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/entity/" + subEntity + ".java",
                    "java", "Entity.java.ftl", subJava));
            files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/mapper/" + subEntity + "Mapper.java",
                    "java", "Mapper.java.ftl", subJava));
            files.add(file(moduleRoot + "/src/main/java/" + packagePath + "/convert/" + subEntity + "Convert.java",
                    "java", "Convert.java.ftl", subJava));
            files.add(file(moduleRoot + "/src/main/java/" + paramPath + "/" + subEntity + "AddParam.java",
                    "java", "AddParam.java.ftl", subJava));
            files.add(file(moduleRoot + "/src/main/java/" + paramPath + "/" + subEntity + "EditParam.java",
                    "java", "EditParam.java.ftl", subJava));
            files.add(file(moduleRoot + "/src/main/java/" + paramPath + "/" + subEntity + "PageParam.java",
                    "java", "PageParam.java.ftl", subJava));
        }

        // 前端
        files.add(file((String) ctx.get("api_file"), "typescript", "api.ts.ftl", ctx));
        files.add(file("hei-admin/src/api/index.ts.append", "typescript", "api_index_export.ts.ftl", ctx));
        files.add(file((String) ctx.get("view_path"), "vue", "index.vue.ftl", ctx));
        files.add(file(ctx.get("view_component_dir") + "/ModalForm.vue", "vue", "ModalForm.vue.ftl", withTarget(ctx, false)));
        files.add(file(ctx.get("view_component_dir") + "/ModalDetail.vue", "vue", "ModalDetail.vue.ftl", withTarget(ctx, false)));
        if (hasSub) {
            files.add(file(ctx.get("child_view_component_dir") + "/ChildModalForm.vue", "vue", "ChildModalForm.vue.ftl", withTarget(ctx, true)));
            files.add(file(ctx.get("child_view_component_dir") + "/ChildModalDetail.vue", "vue", "ChildModalDetail.vue.ftl", withTarget(ctx, true)));
        }

        // 菜单权限 SQL
        files.add(file("scripts/" + toSnake(entityName) + "_menu_permission.sql",
                "sql", "menu_permission.sql.ftl", ctx));
        return files;
    }

    private Map<String, Object> withTarget(Map<String, Object> ctx, boolean child) {
        Map<String, Object> copy = new HashMap<>(ctx);
        copy.put("is_child_template", child);
        copy.put("target", child ? ctx.get("sub") : ctx.get("main"));
        copy.put("has_tree_parent_form", !child && Boolean.TRUE.equals(ctx.get("has_tree_parent_form")));
        return copy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> javaModel(Map<String, Object> ctx, boolean sub) {
        Map<String, Object> model = new HashMap<>();
        model.put("author", ctx.get("author"));
        model.put("generatedAt", ctx.get("generatedAt"));
        model.put("basePackage", ctx.get("basePackage"));
        model.put("paramPackage", ctx.get("paramPackage"));
        model.put("permissionPrefix", ctx.get("permissionPrefix"));
        model.put("apiPrefix", ctx.get("apiPrefix"));
        model.put("auditResourceType", ctx.get("auditResourceType"));
        model.put("hasTree", ctx.get("hasTree"));
        model.put("hasSub", ctx.get("hasSub"));
        model.put("genType", ctx.get("genType"));
        model.put("treeParentProperty", ctx.get("treeParentProperty"));
        model.put("treeLabelProperty", ctx.get("treeLabelProperty"));
        model.put("subForeignProperty", ctx.get("subForeignProperty"));
        model.put("subEntityName", ctx.get("subEntityName"));
        model.put("subVarName", ctx.get("subVarName"));
        model.put("entityName", ctx.get("entityName"));
        model.put("mainVarName", ctx.get("mainVarName"));

        Map<String, Object> entity = sub
                ? (Map<String, Object>) ctx.get("sub")
                : (Map<String, Object>) ctx.get("main");
        String entityName = (String) entity.get("entity_name");
        model.put("entityName", entityName);
        model.put("tableName", entity.get("table_name"));
        model.put("varName", entity.get("var_name"));
        model.put("businessName", sub ? ctx.get("subBusinessName") : ctx.get("businessName"));
        model.put("pkProperty", CodegenNaming.snakeToCamel(String.valueOf(entity.get("pk_name"))));

        List<Map<String, Object>> javaFields = (List<Map<String, Object>>) entity.get("java_fields");
        List<Map<String, Object>> queryFields = (List<Map<String, Object>>) entity.get("java_query_fields");
        model.put("fields", javaFields);
        model.put("queryFields", queryFields);
        Object formFields = entity.get("java_form_fields");
        model.put("formFields", formFields == null ? List.of() : formFields);
        List<String> javaImports = (List<String>) entity.get("java_imports");
        Set<String> imports = new HashSet<>(javaImports == null ? List.of() : javaImports);
        model.put("imports", imports.stream().sorted().toList());
        model.put("hasJson", Boolean.TRUE.equals(entity.get("has_java_json")));
        model.put("isSubEntity", sub);
        return model;
    }

    private SysCodegenPreviewFileResult file(String path, String language, String template, Map<String, Object> model) {
        return new SysCodegenPreviewFileResult(path, language, process(template, model));
    }

    private String process(String templateName, Map<String, Object> model) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            String content = writer.toString();
            if (!content.endsWith("\n")) {
                content = content + "\n";
            }
            return content;
        } catch (Exception ex) {
            throw new BizException("代码生成模板失败: " + templateName + " - " + ex.getMessage());
        }
    }

    private Map<String, Object> buildContext(
            SysCodegenPlan plan,
            List<SysCodegenField> mainFields,
            List<SysCodegenField> subFields) {
        String genType = plan.getGenType() == null ? "TABLE" : plan.getGenType();
        boolean hasTree = TREE_TYPES.contains(genType);
        boolean hasSub = SUB_TYPES.contains(genType)
                && StringUtils.hasText(plan.getSubEntityName())
                && StringUtils.hasText(plan.getSubTable())
                && StringUtils.hasText(plan.getSubPk());

        String apiPrefix = plan.getApiPrefix();
        if (apiPrefix != null && apiPrefix.startsWith("/api/v1/admin")) {
            apiPrefix = apiPrefix.substring("/api/v1/admin".length());
        }
        if (apiPrefix == null || apiPrefix.isBlank()) {
            apiPrefix = "/biz/" + CodegenNaming.apiPathSegment(plan.getBusinessName());
        }
        if (!apiPrefix.startsWith("/")) {
            apiPrefix = "/" + apiPrefix;
        }

        Set<String> mainTableExclude = new HashSet<>();
        if ("TREE".equals(genType) && StringUtils.hasText(plan.getTreeParentField())) {
            mainTableExclude.add(plan.getTreeParentField());
        }

        Map<String, Object> main = entityContext(
                plan.getEntityName(), plan.getTableName(), plan.getPkColumn(), mainFields, mainTableExclude);
        Map<String, Object> sub = hasSub
                ? entityContext(plan.getSubEntityName(), plan.getSubTable(), plan.getSubPk(), subFields, Set.of())
                : Map.of();

        boolean hasTreeParentForm = hasTree
                && StringUtils.hasText(plan.getTreeParentField())
                && ((List<?>) main.get("form_fields")).stream()
                .anyMatch(f -> plan.getTreeParentField().equals(((Map<?, ?>) f).get("name")));

        String componentPath = plan.getComponentPath() == null ? "" : plan.getComponentPath().trim();
        String viewPath = "hei-admin/src/views/" + componentPath.replaceAll("^/+", "");
        String viewDir = viewPath.contains("/") ? viewPath.substring(0, viewPath.lastIndexOf('/')) : "hei-admin/src/views";
        String viewComponentDir = viewDir + "/components";
        String apiFile = resolveApiFile(plan, componentPath);
        String apiExportName = CodegenNaming.snakeToCamel(toSnake(plan.getEntityName())) + "Api";
        if (apiExportName.length() > 0) {
            apiExportName = Character.toLowerCase(apiExportName.charAt(0)) + apiExportName.substring(1);
            // 保持 cgTestActivityApi 风格：实体 Pascal → camel + Api
            apiExportName = Character.toLowerCase(plan.getEntityName().charAt(0))
                    + plan.getEntityName().substring(1) + "Api";
        }

        Map<String, Object> planMap = new LinkedHashMap<>();
        planMap.put("author", plan.getAuthor());
        planMap.put("gen_type", genType);
        planMap.put("description", plan.getDescription());
        planMap.put("table_name", plan.getTableName());
        planMap.put("pk_column", plan.getPkColumn());
        planMap.put("entity_name", plan.getEntityName());
        planMap.put("module_path", plan.getModulePath());
        planMap.put("business_name", plan.getBusinessName());
        planMap.put("api_prefix", apiPrefix);
        planMap.put("permission_prefix", plan.getPermissionPrefix());
        planMap.put("resource_module_id", nullToEmpty(plan.getResourceModuleId()));
        planMap.put("parent_resource_id", nullToEmpty(plan.getParentResourceId()));
        planMap.put("menu_name", plan.getMenuName());
        planMap.put("menu_path", plan.getMenuPath());
        planMap.put("component_path", componentPath);
        planMap.put("icon", plan.getIcon());
        planMap.put("sort", plan.getSort() == null ? 99 : plan.getSort());
        planMap.put("tree_parent_field", plan.getTreeParentField());
        planMap.put("tree_label_field", plan.getTreeLabelField());
        planMap.put("sub_table", plan.getSubTable());
        planMap.put("sub_pk", plan.getSubPk());
        planMap.put("sub_foreign_key", plan.getSubForeignKey());
        planMap.put("sub_entity_name", plan.getSubEntityName());
        planMap.put("sub_business_name", plan.getSubBusinessName());

        Map<String, Object> ctx = new HashMap<>();
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ctx.put("author", plan.getAuthor());
        ctx.put("generatedAt", generatedAt);
        ctx.put("generated_at", generatedAt);
        ctx.put("genType", genType);
        ctx.put("hasTree", hasTree);
        ctx.put("has_tree", hasTree);
        ctx.put("hasSub", hasSub);
        ctx.put("has_sub", hasSub);
        ctx.put("has_tree_parent_form", hasTreeParentForm);
        ctx.put("needs_list_permission", hasTree);
        ctx.put("plan", planMap);
        ctx.put("main", main);
        ctx.put("sub", hasSub ? sub : null);
        ctx.put("target", main);
        ctx.put("is_child_template", false);
        ctx.put("menu_permission", menuPermissionContext(hasTree));
        ctx.put("api_file", apiFile);
        ctx.put("api_export", "export * as " + apiExportName + " from './"
                + apiFile.replace("hei-admin/src/api/", "").replace(".ts", "") + "'");
        ctx.put("api_export_name", apiExportName);
        ctx.put("view_path", viewPath);
        ctx.put("view_component_dir", viewComponentDir);
        ctx.put("child_view_component_dir", viewComponentDir + "/children");

        String basePackage = CodegenNaming.packageFromModulePath(plan.getModulePath());
        ctx.put("basePackage", basePackage);
        ctx.put("paramPackage", basePackage + ".param");
        // 三段式权限码段内不允许 - / _，统一清洗后再下发，保证生成代码与运行期权限校验一致。
        String permissionPrefix = plan.getPermissionPrefix() == null
                ? "" : plan.getPermissionPrefix().replaceAll("[-_]", "");
        ctx.put("permissionPrefix", permissionPrefix);
        ctx.put("apiPrefix", apiPrefix);
        String auditResourceType = plan.getPermissionPrefix() == null
                ? toSnake(plan.getEntityName())
                : permissionPrefix.replace(':', '_');
        ctx.put("auditResourceType", auditResourceType);
        ctx.put("entityName", plan.getEntityName());
        ctx.put("mainVarName", Character.toLowerCase(plan.getEntityName().charAt(0))
                + plan.getEntityName().substring(1));
        ctx.put("businessName", plan.getBusinessName());
        ctx.put("subBusinessName", plan.getSubBusinessName());
        ctx.put("subEntityName", plan.getSubEntityName());
        ctx.put("subVarName", StringUtils.hasText(plan.getSubEntityName())
                ? Character.toLowerCase(plan.getSubEntityName().charAt(0)) + plan.getSubEntityName().substring(1)
                : null);
        ctx.put("treeParentProperty", CodegenNaming.snakeToCamel(plan.getTreeParentField()));
        ctx.put("treeLabelProperty", CodegenNaming.snakeToCamel(plan.getTreeLabelField()));
        ctx.put("subForeignProperty", CodegenNaming.snakeToCamel(plan.getSubForeignKey()));
        String vendor = resolveDbVendor();
        ctx.put("db_vendor", vendor);
        ctx.put("dbVendor", vendor);
        return ctx;
    }

    private String resolveDbVendor() {
        if (dbDialect != null) {
            return dbDialect.vendor().code();
        }
        if (DbDialectHolder.isReady()) {
            return DbDialectHolder.get().vendor().code();
        }
        return DbVendor.POSTGRESQL.code();
    }

    private Map<String, Object> entityContext(
            String entityName,
            String tableName,
            String pkName,
            List<SysCodegenField> fields,
            Set<String> tableExclude) {
        List<Map<String, Object>> modelFields = new ArrayList<>();
        List<Map<String, Object>> formFields = new ArrayList<>();
        List<Map<String, Object>> queryFields = new ArrayList<>();
        List<Map<String, Object>> tableFields = new ArrayList<>();
        List<Map<String, Object>> detailFields = new ArrayList<>();
        List<Map<String, Object>> javaFields = new ArrayList<>();
        List<Map<String, Object>> javaFormFields = new ArrayList<>();
        List<Map<String, Object>> javaQueryFields = new ArrayList<>();
        Set<String> javaImports = new HashSet<>();
        boolean hasJavaJson = false;

        for (SysCodegenField field : fields) {
            Map<String, Object> fc = fieldContext(field);
            if (!AUDIT.contains(field.getColumnName())) {
                modelFields.add(fc);
            }
            if (isFormField(field)) {
                formFields.add(fc);
            }
            if (Boolean.TRUE.equals(field.getInQuery()) && !Boolean.TRUE.equals(field.getPrimaryKey())) {
                queryFields.add(fc);
            }
            if (Boolean.TRUE.equals(field.getInTable())
                    && !AUDIT.contains(field.getColumnName())
                    && !tableExclude.contains(field.getColumnName())) {
                tableFields.add(fc);
            }
            if (Boolean.TRUE.equals(field.getInDetail()) && !AUDIT.contains(field.getColumnName())) {
                detailFields.add(fc);
            }

            if ("id".equals(field.getColumnName()) || AUDIT.contains(field.getColumnName())) {
                continue;
            }
            String javaType = DbTypeMapper.toJavaType(field.getValueType());
            String simpleType = DbTypeMapper.toSimpleJavaName(javaType);
            if (javaType.startsWith("java.") && !javaType.startsWith("java.lang.") && !javaType.contains("<")) {
                javaImports.add(javaType);
            }
            if ("java.util.Map<String, Object>".equals(javaType)) {
                javaImports.add("java.util.Map");
                hasJavaJson = true;
            }
            Map<String, Object> jf = new HashMap<>();
            jf.put("columnName", field.getColumnName());
            jf.put("propertyName", CodegenNaming.snakeToCamel(field.getColumnName()));
            jf.put("comment", field.getLabel() == null ? "" : field.getLabel());
            jf.put("javaType", simpleType);
            jf.put("fullJavaType", javaType);
            jf.put("isJson", "dict".equals(field.getValueType()) || (field.getDbType() != null && field.getDbType().toLowerCase(Locale.ROOT).contains("json")));
            jf.put("inQuery", Boolean.TRUE.equals(field.getInQuery()));
            jf.put("inForm", Boolean.TRUE.equals(field.getInForm()));
            jf.put("queryOperator", field.getQueryOperator());
            jf.put("required", Boolean.TRUE.equals(field.getRequired()));
            jf.put("maxLength", field.getMaxLength());
            javaFields.add(jf);
            if (Boolean.TRUE.equals(field.getInForm()) && !Boolean.TRUE.equals(field.getPrimaryKey())) {
                javaFormFields.add(jf);
            }
            if (Boolean.TRUE.equals(field.getInQuery())) {
                javaQueryFields.add(jf);
            }
        }

        String varName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
        Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("entity_name", entityName);
        entity.put("var_name", varName);
        entity.put("table_name", tableName);
        entity.put("pk_name", pkName);
        entity.put("model_fields", modelFields);
        entity.put("form_fields", formFields);
        entity.put("query_fields", queryFields);
        entity.put("table_fields", tableFields);
        entity.put("detail_fields", detailFields);
        entity.put("has_form_datetime", formFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.get("is_datetime"))));
        entity.put("has_form_json", formFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.get("is_json"))));
        entity.put("has_form_bool", formFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.get("is_bool"))));
        entity.put("has_form_richtext", formFields.stream().anyMatch(f -> "richtext".equals(f.get("widget"))));
        entity.put("has_form_markdown", formFields.stream().anyMatch(f -> "markdown".equals(f.get("widget"))));
        entity.put("has_form_code", formFields.stream().anyMatch(f -> "code".equals(f.get("widget"))));
        entity.put("has_form_icon", formFields.stream().anyMatch(f -> "icon".equals(f.get("widget"))));
        entity.put("has_form_editor", formFields.stream().anyMatch(f ->
                Set.of("richtext", "markdown", "code").contains(String.valueOf(f.get("widget")))));
        entity.put("has_form_int", formFields.stream().anyMatch(f -> "int".equals(f.get("value_type"))));
        entity.put("has_form_float", formFields.stream().anyMatch(f -> "float".equals(f.get("value_type"))));
        entity.put("has_detail_json", detailFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.get("is_json"))));
        entity.put("has_detail_richtext", detailFields.stream().anyMatch(f -> "richtext".equals(f.get("widget"))));
        entity.put("has_detail_markdown", detailFields.stream().anyMatch(f -> "markdown".equals(f.get("widget"))));
        entity.put("has_detail_code", detailFields.stream().anyMatch(f -> "code".equals(f.get("widget"))));
        entity.put("has_detail_icon", detailFields.stream().anyMatch(f -> "icon".equals(f.get("widget"))));
        entity.put("has_detail_editor", detailFields.stream().anyMatch(f ->
                Set.of("richtext", "markdown", "code").contains(String.valueOf(f.get("widget")))));
        entity.put("has_query_dict", queryFields.stream().anyMatch(f -> f.get("dict_code") != null && !String.valueOf(f.get("dict_code")).isBlank()));
        entity.put("has_table_dict", tableFields.stream().anyMatch(f -> f.get("dict_code") != null && !String.valueOf(f.get("dict_code")).isBlank()));
        entity.put("has_table_bool", tableFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.get("is_bool"))));
        entity.put("has_table_tag", tableFields.stream().anyMatch(f ->
                (f.get("dict_code") != null && !String.valueOf(f.get("dict_code")).isBlank())
                        || Boolean.TRUE.equals(f.get("is_bool"))));
        entity.put("has_detail_dict", detailFields.stream().anyMatch(f -> f.get("dict_code") != null && !String.valueOf(f.get("dict_code")).isBlank()));
        entity.put("has_detail_bool", detailFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.get("is_bool"))));
        boolean needsNormalize = formFields.stream().anyMatch(f ->
                Boolean.TRUE.equals(f.get("is_datetime")) || Boolean.TRUE.equals(f.get("is_json")));
        entity.put("needs_form_normalize", needsNormalize);
        entity.put("needs_submit_normalize", needsNormalize);
        entity.put("java_fields", javaFields);
        entity.put("java_form_fields", javaFormFields);
        entity.put("java_query_fields", javaQueryFields);
        entity.put("java_imports", new ArrayList<>(javaImports));
        entity.put("has_java_json", hasJavaJson);
        return entity;
    }

    private Map<String, Object> fieldContext(SysCodegenField field) {
        String valueType = field.getValueType() == null ? "str" : field.getValueType();
        boolean isDatetime = "datetime".equals(field.getWidget()) || "datetime".equals(valueType);
        boolean isJson = "dict".equals(valueType)
                || (field.getDbType() != null && field.getDbType().toLowerCase(Locale.ROOT).contains("json"));
        boolean isBool = "bool".equals(valueType);
        Map<String, Object> fc = new LinkedHashMap<>();
        fc.put("name", field.getColumnName());
        fc.put("label", StringUtils.hasText(field.getLabel()) ? field.getLabel() : field.getColumnName());
        fc.put("comment", field.getLabel());
        fc.put("db_type", field.getDbType());
        fc.put("value_type", valueType);
        fc.put("ui_type", field.getUiType());
        fc.put("widget", field.getWidget());
        fc.put("dict_code", field.getDictCode());
        fc.put("code_language", codeLanguage(field));
        fc.put("query_operator", field.getQueryOperator() == null ? "LIKE" : field.getQueryOperator());
        fc.put("in_table", field.getInTable());
        fc.put("in_form", field.getInForm());
        fc.put("in_detail", field.getInDetail());
        fc.put("in_query", field.getInQuery());
        fc.put("primary_key", field.getPrimaryKey());
        fc.put("required", field.getRequired());
        fc.put("nullable", field.getNullable());
        fc.put("max_length", field.getMaxLength());
        fc.put("vue_default", vueDefault(field, isDatetime, isJson, isBool, valueType));
        fc.put("is_datetime", isDatetime);
        fc.put("is_json", isJson);
        fc.put("is_bool", isBool);
        return fc;
    }

    private static String vueDefault(
            SysCodegenField field, boolean isDatetime, boolean isJson, boolean isBool, String valueType) {
        if (isDatetime || "datetime".equals(field.getWidget())) {
            return "null";
        }
        if ("int".equals(valueType) || "float".equals(valueType)) {
            return "0";
        }
        if (isBool) {
            return "false";
        }
        if (isJson) {
            return "'{}'";
        }
        return "''";
    }

    private static String codeLanguage(SysCodegenField field) {
        if ("dict".equals(field.getValueType())
                || (field.getDbType() != null && field.getDbType().toLowerCase(Locale.ROOT).contains("json"))) {
            return "json";
        }
        String columnName = field.getColumnName() == null ? "" : field.getColumnName().toLowerCase(Locale.ROOT);
        if (columnName.contains("sql")) {
            return "sql";
        }
        if (columnName.contains("script") || columnName.contains("code") || columnName.contains("template")) {
            return "typescript";
        }
        return "plaintext";
    }

    private static boolean isFormField(SysCodegenField field) {
        return Boolean.TRUE.equals(field.getInForm())
                && !Boolean.TRUE.equals(field.getPrimaryKey())
                && !AUDIT.contains(field.getColumnName());
    }

    private Map<String, Object> menuPermissionContext(boolean needsList) {
        List<Map<String, Object>> actions = new ArrayList<>();
        actions.add(action("page", "分页", 10));
        actions.add(action("create", "新增", 20));
        actions.add(action("detail", "详情", 30));
        actions.add(action("update", "编辑", 40));
        actions.add(action("delete", "删除", 50));
        if (needsList) {
            actions.add(action("tree", "树查询", 90));
        }
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("menu_id", snowflakeLikeId());
        menu.put("actions", actions);
        return menu;
    }

    private static Map<String, Object> action(String key, String label, int sort) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("key", key);
        a.put("label", label);
        a.put("sort", sort);
        a.put("resource_id", snowflakeLikeId());
        a.put("relation_id", snowflakeLikeId());
        return a;
    }

    private static String snowflakeLikeId() {
        long ts = System.currentTimeMillis();
        long rand = ThreadLocalRandom.current().nextLong(1000, 9999);
        return String.valueOf(ts * 10000 + rand);
    }

    private static String resolveApiFile(SysCodegenPlan plan, String componentPath) {
        String cleaned = componentPath.replaceAll("^/+", "");
        String[] parts = cleaned.split("/");
        if (parts.length >= 2 && "index.vue".equals(parts[parts.length - 1])) {
            StringBuilder sb = new StringBuilder("hei-admin/src/api");
            for (int i = 0; i < parts.length - 1; i++) {
                sb.append('/').append(parts[i]);
            }
            return sb + ".ts";
        }
        return "hei-admin/src/api/" + toSnake(plan.getEntityName()) + ".ts";
    }

    private static String toSnake(String value) {
        if (value == null || value.isBlank()) {
            return "entity";
        }
        String s = value.replaceAll("([a-z])([A-Z])", "$1_$2").replace('-', '_').replace(' ', '_');
        return s.toLowerCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String firstModuleSegment(String modulePath) {
        if (modulePath == null || modulePath.isBlank()) {
            return "biz";
        }
        String cleaned = modulePath.trim().replace('\\', '/');
        for (String part : cleaned.split("/")) {
            if (!part.isBlank() && !".".equals(part)) {
                return part.toLowerCase(Locale.ROOT).replace('-', '_');
            }
        }
        return "biz";
    }
}
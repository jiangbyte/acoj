package github.jiangbyte.io.sys.modules.codegen.support;

/** Author: Charlie **/

import github.jiangbyte.io.common.mybatis.dialect.MysqlDialect;
import github.jiangbyte.io.common.mybatis.dialect.PostgreSqlDialect;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenField;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenPlan;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenPreviewFileResult;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenTemplateEngineTest {

    private final CodegenTemplateEngine engine = new CodegenTemplateEngine(new PostgreSqlDialect());

    @Test
    void tableRenderContainsFullStackWithoutTreeOrChild() {
        SysCodegenPlan plan = basePlan("TABLE", "cg_test_activity", "CgTestActivity", "activity");
        List<SysCodegenPreviewFileResult> files = engine.render(plan, sampleFields("MAIN"), List.of());
        Set<String> paths = paths(files);
        assertTrue(paths.stream().anyMatch(p -> p.endsWith("/entity/CgTestActivity.java")));
        assertTrue(paths.stream().anyMatch(p -> p.contains("hei-admin/src/views/")));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith(".ts") && p.contains("hei-admin/src/api/")));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith("_menu_permission.sql")));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith("/param/CgTestActivityAddParam.java")));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith("/param/CgTestActivityEditParam.java")));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith("/service/impl/CgTestActivityServiceImpl.java")));
        assertTrue(paths.stream().anyMatch(p -> p.contains("/controller/AdminCgTestActivityController.java")));
        assertFalse(paths.stream().anyMatch(p -> p.contains("/controller/admin/")));
        assertFalse(paths.stream().anyMatch(p -> p.startsWith("module-api/")));
        assertFalse(paths.stream().anyMatch(p -> p.endsWith("Api.java") || p.contains("DTO.java")));
        assertFalse(paths.stream().anyMatch(p -> p.contains("ChildModal")));
        assertFalse(contentContaining(files, "AdminCgTestActivityController.java").contains("/tree"));
        assertTrue(contentContaining(files, "AdminCgTestActivityController.java").contains("Page<"));
        // 规范对齐：写操作带审计注解
        assertTrue(contentContaining(files, "AdminCgTestActivityController.java").contains("@OperationAudit(resourceType = \"biz_activity\", action = \"create\")"));
        assertTrue(contentContaining(files, "AdminCgTestActivityController.java").contains("@OperationAudit(resourceType = \"biz_activity\", action = \"update\")"));
        assertTrue(contentContaining(files, "AdminCgTestActivityController.java").contains("@OperationAudit(resourceType = \"biz_activity\", action = \"delete\")"));
        assertTrue(contentContaining(files, "CgTestActivityAddParam.java").contains("@Schema(description = \"创建"));
        assertTrue(contentContaining(files, "CgTestActivity.java").contains("@Schema(description = "));
        assertTrue(contentContaining(files, "AdminCgTestActivityController.java").contains("@Tag(name = "));
        assertTrue(contentContaining(files, "AdminCgTestActivityController.java").contains("@Operation(summary = "));
        // 规范对齐：分页走 getBaseMapper().selectPage
        assertTrue(contentContaining(files, "CgTestActivityServiceImpl.java").contains("getBaseMapper().selectPage"));
        // 前端 api 使用 API_PREFIX 常量（保持 any 约定）
        String api = contentContaining(files, "hei-admin/src/api/");
        assertTrue(api.contains("API_PREFIX"));
        assertTrue(api.contains("export function page(params: any)"));
        assertTrue(api.contains("http.get<any>"));
        // 前端搜索列：有 dict_code 的查询字段生成 select + dictList（见 dictSearchColumnRender 用例）
    }

    @Test
    void dictQueryFieldRendersSelectSearchColumn() {
        SysCodegenPlan plan = basePlan("TABLE", "cg_test_dict_search", "CgTestDictSearch", "dict-search");
        List<SysCodegenField> fields = sampleFields("MAIN");
        for (SysCodegenField field : fields) {
            if ("status".equals(field.getColumnName())) {
                field.setDictCode("COMMON_STATUS");
                field.setWidget("dict");
                field.setQueryOperator("EQ");
            }
        }
        List<SysCodegenPreviewFileResult> files = engine.render(plan, fields, List.of());
        String view = contentContaining(files, "hei-admin/src/views/");
        assertTrue(view.contains("dictList('COMMON_STATUS')"));
        assertTrue(view.contains("field: 'select'"));
        assertTrue(view.contains("import { dictList,"));
    }

    @Test
    void treeRenderIncludesTreeApiAndListPermission() {
        SysCodegenPlan plan = basePlan("TREE", "cg_test_catalog", "CgTestCatalog", "catalog");
        plan.setTreeParentField("parent_id");
        plan.setTreeLabelField("name");
        List<SysCodegenField> fields = sampleFields("MAIN");
        fields.add(field("MAIN", "parent_id", "str", true, true));
        List<SysCodegenPreviewFileResult> files = engine.render(plan, fields, List.of());
        assertTrue(contentContaining(files, "AdminCgTestCatalogController.java").contains("/tree"));
        assertTrue(contentContaining(files, "CgTestCatalogService.java").contains("tree("));
        assertTrue(contentContaining(files, "CgTestCatalogServiceImpl.java").contains("TreeUtil"));
        assertTrue(contentContaining(files, "CgTestCatalog.java").contains("children"));
        assertTrue(contentContaining(files, "hei-admin/src/api/").contains("export function tree"));
        assertTrue(contentContaining(files, "_menu_permission.sql").contains(":tree"));
        assertTrue(contentContaining(files, "AdminCgTestCatalogController.java").contains(":tree"));
        assertFalse(paths(files).stream().anyMatch(p -> p.startsWith("module-api/")));
        assertFalse(paths(files).stream().anyMatch(p -> p.contains("ChildModal")));
    }

    @Test
    void leftTreeTableRenderIncludesChildVueAndChildrenApi() {
        SysCodegenPlan plan = basePlan("LEFT_TREE_TABLE", "cg_test_knowledge_category", "CgTestKnowledgeCategory", "knowledge-category");
        plan.setTreeParentField("parent_id");
        plan.setTreeLabelField("name");
        plan.setSubTable("cg_test_knowledge_doc");
        plan.setSubPk("id");
        plan.setSubForeignKey("category_id");
        plan.setSubEntityName("CgTestKnowledgeDoc");
        plan.setSubBusinessName("知识文档");
        List<SysCodegenField> main = sampleFields("MAIN");
        main.add(field("MAIN", "parent_id", "str", true, true));
        List<SysCodegenField> sub = sampleFields("SUB");
        sub.add(field("SUB", "category_id", "str", true, true));
        List<SysCodegenPreviewFileResult> files = engine.render(plan, main, sub);
        Set<String> paths = paths(files);
        assertTrue(paths.stream().anyMatch(p -> p.contains("ChildModalForm.vue")));
        assertTrue(paths.stream().anyMatch(p -> p.endsWith("/entity/CgTestKnowledgeDoc.java")));
        assertTrue(contentContaining(files, "AdminCgTestKnowledgeCategoryController.java").contains("/children/page"));
        assertTrue(contentContaining(files, "hei-admin/src/api/").contains("childPage"));
    }

    @Test
    void masterDetailRenderIncludesChildrenWithoutTree() {
        SysCodegenPlan plan = basePlan("MASTER_DETAIL", "cg_test_order", "CgTestOrder", "order");
        plan.setSubTable("cg_test_order_item");
        plan.setSubPk("id");
        plan.setSubForeignKey("order_id");
        plan.setSubEntityName("CgTestOrderItem");
        plan.setSubBusinessName("订单明细");
        List<SysCodegenField> sub = sampleFields("SUB");
        sub.add(field("SUB", "order_id", "str", true, true));
        List<SysCodegenPreviewFileResult> files = engine.render(plan, sampleFields("MAIN"), sub);
        String controller = contentContaining(files, "AdminCgTestOrderController.java");
        assertTrue(controller.contains("/children/"));
        assertFalse(controller.contains("/tree"));
        assertTrue(paths(files).stream().anyMatch(p -> p.contains("ChildModalDetail.vue")));
        assertFalse(contentContaining(files, "_menu_permission.sql").contains(":tree"));
    }

    @Test
    void menuPermissionSqlUsesPostgresqlUpsertByDefault() {
        SysCodegenPlan plan = basePlan("TABLE", "cg_test_activity", "CgTestActivity", "activity");
        List<SysCodegenPreviewFileResult> files = engine.render(plan, sampleFields("MAIN"), List.of());
        String sql = contentContaining(files, "_menu_permission.sql");
        assertTrue(sql.contains("ON CONFLICT"));
        assertFalse(sql.contains("ON DUPLICATE KEY"));
        assertTrue(sql.contains("db_vendor: postgresql"));
    }

    @Test
    void menuPermissionSqlUsesMysqlUpsertWhenMysqlDialect() {
        CodegenTemplateEngine mysqlEngine = new CodegenTemplateEngine(new MysqlDialect());
        SysCodegenPlan plan = basePlan("TABLE", "cg_test_activity", "CgTestActivity", "activity");
        List<SysCodegenPreviewFileResult> files = mysqlEngine.render(plan, sampleFields("MAIN"), List.of());
        String sql = contentContaining(files, "_menu_permission.sql");
        assertTrue(sql.contains("ON DUPLICATE KEY"));
        assertFalse(sql.contains("ON CONFLICT"));
        assertTrue(sql.contains("db_vendor: mysql"));
    }

    @Test
    void writeCgTestFixturesWhenEnabled() throws Exception {
        if (!"true".equalsIgnoreCase(System.getProperty("codegen.writeFixtures"))) {
            return;
        }
        Path root = Path.of("").toAbsolutePath();
        while (root != null && !Files.exists(root.resolve("module/sys"))) {
            root = root.getParent();
        }
        if (root == null) {
            root = Path.of(".").toAbsolutePath().normalize();
        }
        writePlan(root, activityPlan(), sampleActivityFields(), List.of());
        writePlan(root, catalogPlan(), sampleCatalogFields(), List.of());
        writePlan(root, orderPlan(), sampleOrderFields(), sampleOrderItemFields());
        writePlan(root, knowledgePlan(), sampleKnowledgeCategoryFields(), sampleKnowledgeDocFields());
    }

    private void writePlan(
            Path root,
            SysCodegenPlan plan,
            List<SysCodegenField> main,
            List<SysCodegenField> sub) throws Exception {
        for (SysCodegenPreviewFileResult file : engine.render(plan, main, sub)) {
            // 演示模块只检入 Java / 前端源码，SQL 仍随 ZIP 下载
            if (file.getPath().endsWith(".sql") || file.getPath().endsWith(".append")) {
                continue;
            }
            Path target = root.resolve(file.getPath());
            Files.createDirectories(target.getParent());
            Files.writeString(target, file.getContent(), StandardCharsets.UTF_8);
        }
    }

    private static SysCodegenPlan activityPlan() {
        return basePlan("TABLE", "cg_test_activity", "CgTestActivity", "cg-test-activity");
    }

    private static SysCodegenPlan catalogPlan() {
        SysCodegenPlan plan = basePlan("TREE", "cg_test_catalog", "CgTestCatalog", "cg-test-catalog");
        plan.setTreeParentField("parent_id");
        plan.setTreeLabelField("name");
        return plan;
    }

    private static SysCodegenPlan orderPlan() {
        SysCodegenPlan plan = basePlan("MASTER_DETAIL", "cg_test_order", "CgTestOrder", "cg-test-order");
        plan.setSubTable("cg_test_order_item");
        plan.setSubPk("id");
        plan.setSubForeignKey("order_id");
        plan.setSubEntityName("CgTestOrderItem");
        plan.setSubBusinessName("订单明细");
        return plan;
    }

    private static SysCodegenPlan knowledgePlan() {
        SysCodegenPlan plan = basePlan(
                "LEFT_TREE_TABLE", "cg_test_knowledge_category", "CgTestKnowledgeCategory", "cg-test-knowledge-category");
        plan.setTreeParentField("parent_id");
        plan.setTreeLabelField("name");
        plan.setSubTable("cg_test_knowledge_doc");
        plan.setSubPk("id");
        plan.setSubForeignKey("category_id");
        plan.setSubEntityName("CgTestKnowledgeDoc");
        plan.setSubBusinessName("知识文档");
        return plan;
    }

    private static SysCodegenPlan basePlan(String genType, String table, String entity, String kebab) {
        SysCodegenPlan plan = new SysCodegenPlan();
        plan.setName(entity);
        plan.setGenType(genType);
        plan.setAuthor("Charlie");
        plan.setTableName(table);
        plan.setPkColumn("id");
        plan.setEntityName(entity);
        plan.setModulePath("biz/" + table);
        plan.setBusinessName(entity.replace("CgTest", ""));
        plan.setApiPrefix("/biz/" + kebab);
        plan.setPermissionPrefix("biz:" + kebab.replace("-", ""));
        plan.setMenuName(entity);
        plan.setMenuPath("/biz/" + kebab);
        plan.setComponentPath("biz/" + kebab + "/index.vue");
        plan.setIcon("icon-park-outline:table");
        plan.setSort(99);
        return plan;
    }

    private static List<SysCodegenField> sampleFields(String role) {
        List<SysCodegenField> fields = new ArrayList<>();
        fields.add(field(role, "id", "str", false, false));
        fields.get(0).setPrimaryKey(true);
        fields.add(field(role, "name", "str", true, true));
        fields.add(field(role, "status", "str", true, true));
        fields.add(field(role, "created_at", "datetime", false, false));
        return fields;
    }

    private static List<SysCodegenField> sampleActivityFields() {
        return fieldsOf(
                col("id", "str", true, false, false, false),
                col("code", "str", false, true, true, true),
                col("name", "str", false, true, true, true),
                col("category", "str", false, true, true, true),
                col("type", "str", false, true, true, true),
                col("status", "str", false, true, true, true),
                col("cover_url", "str", false, true, true, false),
                col("description", "str", false, true, true, false),
                col("start_at", "datetime", false, true, true, false),
                col("end_at", "datetime", false, true, true, false),
                col("max_participants", "int", false, true, true, false),
                col("price", "float", false, true, true, false),
                col("is_public", "bool", false, true, true, false),
                col("need_approval", "bool", false, true, true, false),
                col("rule_config", "dict", false, true, true, false),
                col("extra", "dict", false, true, true, false),
                col("owner_dept_id", "str", false, false, false, false),
                col("created_at", "datetime", false, false, false, false),
                col("created_by", "str", false, false, false, false),
                col("updated_at", "datetime", false, false, false, false),
                col("updated_by", "str", false, false, false, false)
        );
    }

    private static List<SysCodegenField> sampleCatalogFields() {
        return fieldsOf(
                col("id", "str", true, false, false, false),
                col("parent_id", "str", false, true, true, false),
                col("code", "str", false, true, true, true),
                col("name", "str", false, true, true, true),
                col("category", "str", false, true, true, true),
                col("status", "str", false, true, true, true),
                col("sort", "int", false, true, true, false),
                col("is_visible", "bool", false, true, true, false),
                col("icon", "str", false, true, true, false),
                col("description", "str", false, true, true, false),
                col("extra", "dict", false, true, true, false),
                col("owner_dept_id", "str", false, false, false, false),
                col("created_at", "datetime", false, false, false, false),
                col("created_by", "str", false, false, false, false),
                col("updated_at", "datetime", false, false, false, false),
                col("updated_by", "str", false, false, false, false)
        );
    }

    private static List<SysCodegenField> sampleOrderFields() {
        return fieldsOf(
                col("id", "str", true, false, false, false),
                col("order_no", "str", false, true, true, true),
                col("name", "str", false, true, true, true),
                col("customer_name", "str", false, true, true, true),
                col("customer_phone", "str", false, true, true, false),
                col("status", "str", false, true, true, true),
                col("type", "str", false, true, true, true),
                col("ordered_at", "datetime", false, true, true, false),
                col("paid_at", "datetime", false, true, true, false),
                col("total_amount", "float", false, true, true, false),
                col("item_count", "int", false, true, true, false),
                col("need_invoice", "bool", false, true, true, false),
                col("invoice_config", "dict", false, true, true, false),
                col("remark", "str", false, true, true, false),
                col("extra", "dict", false, true, true, false),
                col("owner_dept_id", "str", false, false, false, false),
                col("created_at", "datetime", false, false, false, false),
                col("created_by", "str", false, false, false, false),
                col("updated_at", "datetime", false, false, false, false),
                col("updated_by", "str", false, false, false, false)
        );
    }

    private static List<SysCodegenField> sampleOrderItemFields() {
        return fieldsOf(
                col("id", "str", true, false, false, false),
                col("order_id", "str", false, true, true, true),
                col("sku_code", "str", false, true, true, true),
                col("name", "str", false, true, true, true),
                col("category", "str", false, true, true, false),
                col("status", "str", false, true, true, true),
                col("quantity", "int", false, true, true, false),
                col("unit_price", "float", false, true, true, false),
                col("shipped_at", "datetime", false, true, true, false),
                col("is_gift", "bool", false, true, true, false),
                col("item_config", "dict", false, true, true, false),
                col("remark", "str", false, true, true, false),
                col("extra", "dict", false, true, true, false),
                col("created_at", "datetime", false, false, false, false),
                col("created_by", "str", false, false, false, false),
                col("updated_at", "datetime", false, false, false, false),
                col("updated_by", "str", false, false, false, false)
        );
    }

    private static List<SysCodegenField> sampleKnowledgeCategoryFields() {
        // 与 V1 cg_test_knowledge_category 一致：无 category/icon
        return fieldsOf(
                col("id", "str", true, false, false, false),
                col("parent_id", "str", false, true, true, false),
                col("code", "str", false, true, true, true),
                col("name", "str", false, true, true, true),
                col("status", "str", false, true, true, true),
                col("sort", "int", false, true, true, false),
                col("is_visible", "bool", false, true, true, false),
                col("description", "str", false, true, true, false),
                col("extra", "dict", false, true, true, false),
                col("owner_dept_id", "str", false, false, false, false),
                col("created_at", "datetime", false, false, false, false),
                col("created_by", "str", false, false, false, false),
                col("updated_at", "datetime", false, false, false, false),
                col("updated_by", "str", false, false, false, false)
        );
    }

    private static List<SysCodegenField> sampleKnowledgeDocFields() {
        return fieldsOf(
                col("id", "str", true, false, false, false),
                col("category_id", "str", false, true, true, true),
                col("code", "str", false, true, true, true),
                col("title", "str", false, true, true, true),
                col("type", "str", false, true, true, true),
                col("status", "str", false, true, true, true),
                col("summary", "str", false, true, true, false),
                col("content", "str", false, true, true, false),
                col("author", "str", false, true, true, false),
                col("published_at", "datetime", false, true, true, false),
                col("view_count", "int", false, true, true, false),
                col("sort", "int", false, true, true, false),
                col("is_top", "bool", false, true, true, false),
                col("settings", "dict", false, true, true, false),
                col("extra", "dict", false, true, true, false),
                col("created_at", "datetime", false, false, false, false),
                col("created_by", "str", false, false, false, false),
                col("updated_at", "datetime", false, false, false, false),
                col("updated_by", "str", false, false, false, false)
        );
    }

    private static List<SysCodegenField> fieldsOf(SysCodegenField... fields) {
        return new ArrayList<>(List.of(fields));
    }

    private static SysCodegenField col(
            String name, String valueType, boolean pk, boolean form, boolean table, boolean query) {
        SysCodegenField field = field("MAIN", name, valueType, form, query);
        field.setPrimaryKey(pk);
        field.setInTable(table);
        field.setInDetail(true);
        if ("status".equals(name)) {
            field.setWidget("dict");
            field.setDictCode("COMMON_STATUS");
            field.setQueryOperator("EQ");
        } else if ("bool".equals(valueType)) {
            field.setWidget("switch");
            field.setQueryOperator("EQ");
        } else if ("datetime".equals(valueType)) {
            field.setWidget("datetime");
        } else if ("int".equals(valueType) || "float".equals(valueType)) {
            field.setWidget("number");
            field.setQueryOperator("EQ");
        } else if (name.contains("description") || name.contains("content") || name.contains("remark") || name.contains("summary")) {
            field.setWidget("textarea");
        } else {
            field.setWidget("input");
            if (query) {
                field.setQueryOperator("LIKE");
            }
        }
        if ("dict".equals(valueType)) {
            field.setDbType("json");
        }
        return field;
    }

    private static SysCodegenField field(String role, String name, String valueType, boolean form, boolean query) {
        SysCodegenField field = new SysCodegenField();
        field.setTableRole(role);
        field.setColumnName(name);
        field.setLabel(name);
        field.setValueType(valueType);
        field.setUiType(toUiType(valueType));
        field.setInForm(form);
        field.setInTable(true);
        field.setInDetail(true);
        field.setInQuery(query);
        field.setPrimaryKey("id".equals(name));
        field.setRequired(form && !"id".equals(name));
        field.setNullable(!Boolean.TRUE.equals(field.getRequired()));
        field.setWidget("input");
        field.setSort(10);
        return field;
    }

    private static String toUiType(String valueType) {
        return switch (valueType) {
            case "int", "float" -> "number";
            case "bool" -> "boolean";
            case "dict" -> "Record<string, any>";
            default -> "string";
        };
    }

    private static Set<String> paths(List<SysCodegenPreviewFileResult> files) {
        return files.stream().map(SysCodegenPreviewFileResult::getPath).collect(Collectors.toSet());
    }

    private static String contentContaining(List<SysCodegenPreviewFileResult> files, String pathPart) {
        return files.stream()
                .filter(f -> f.getPath().contains(pathPart))
                .map(SysCodegenPreviewFileResult::getContent)
                .findFirst()
                .orElse("");
    }
}
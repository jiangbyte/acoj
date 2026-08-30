package github.jiangbyte.io.sys.modules.codegen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.mybatis.dialect.SqlSafe;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenField;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenPlan;
import github.jiangbyte.io.iam.resource.ResourceMenuApi;
import github.jiangbyte.io.iam.resource.ResourceMenuNode;
import github.jiangbyte.io.sys.modules.codegen.mapper.CodegenSchemaMapper;
import github.jiangbyte.io.sys.modules.codegen.mapper.SysCodegenFieldMapper;
import github.jiangbyte.io.sys.modules.codegen.mapper.SysCodegenPlanMapper;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenFieldUpdateItemParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenFieldsUpdateBatchParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenPlanPageParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenPlanSaveParam;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenDatabaseColumnResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenDatabaseTableResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenPreviewFileResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenPreviewResult;
import github.jiangbyte.io.sys.modules.codegen.service.CodegenService;
import github.jiangbyte.io.sys.modules.codegen.support.CodegenTemplateEngine;
import github.jiangbyte.io.sys.modules.codegen.support.DbTypeMapper;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现：基于 Freemarker 渲染模板。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class CodegenServiceImpl extends ServiceImpl<SysCodegenPlanMapper, SysCodegenPlan> implements CodegenService {

    private static final Set<String> AUDIT = Set.of("created_at", "created_by", "updated_at", "updated_by");
    private static final Set<String> RELATION_TYPES = Set.of("LEFT_TREE_TABLE", "MASTER_DETAIL");
    private static final Set<String> TREE_TYPES = Set.of("TREE", "LEFT_TREE_TABLE");

    private final SysCodegenFieldMapper fieldMapper;
    private final CodegenSchemaMapper schemaMapper;
    private final CodegenTemplateEngine templateEngine;
    private final ResourceMenuApi resourceMenuApi;

    @Override
    @Transactional
    public void create(SysCodegenPlanSaveParam request) {
        validatePlan(request);
        Long nameCount = getBaseMapper().selectCount(Wrappers.<SysCodegenPlan>lambdaQuery()
                .eq(SysCodegenPlan::getName, request.getName()));
        if (nameCount != null && nameCount > 0) {
            throw new BizException("Codegen plan name already exists");
        }
        SysCodegenPlan plan = new SysCodegenPlan();
        BeanUtil.copyProperties(request, plan);
        plan.setId(null);
        this.save(plan);
        AuditSnapshots.created(plan);
        syncReflectedFields(plan);
    }

    @Override
    @Transactional
    public void update(SysCodegenPlanSaveParam request) {
        if (!StringUtils.hasText(request.getId())) {
            throw new BizException("id is required");
        }
        validatePlan(request);
        Long nameCount = getBaseMapper().selectCount(Wrappers.<SysCodegenPlan>lambdaQuery()
                .eq(SysCodegenPlan::getName, request.getName())
                .ne(SysCodegenPlan::getId, request.getId()));
        if (nameCount != null && nameCount > 0) {
            throw new BizException("Codegen plan name already exists");
        }
        SysCodegenPlan plan = getBaseMapper().selectById(request.getId());
        if (plan == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Codegen plan not found");
        }
        AuditSnapshots.before(plan);
        BeanUtil.copyProperties(request, plan);
        this.updateById(plan);
        AuditSnapshots.after(plan);
        syncReflectedFields(plan);
    }

    @Override
    @Transactional
    public void delete(IdsParam request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return;
        }
        List<SysCodegenPlan> plans = this.listByIds(request.getIds());
        AuditSnapshots.deletedAll(plans);
        fieldMapper.delete(Wrappers.<SysCodegenField>lambdaQuery().in(SysCodegenField::getPlanId, request.getIds()));
        this.removeByIds(request.getIds());
    }

    @Override
    @ReadDataSource
    public SysCodegenPlan detail(String id) {
        // 按主键加载
        SysCodegenPlan plan = getBaseMapper().selectById(id);
        if (plan == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Codegen plan not found");
        }
        return plan;
    }

    @Override
    @ReadDataSource
    public Page<SysCodegenPlan> page(SysCodegenPlanPageParam param) {
        // 分页查询
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysCodegenPlan>lambdaQuery()
                        .like(StringUtils.hasText(param.getName()), SysCodegenPlan::getName, param.getName())
                        .like(StringUtils.hasText(param.getTableName()), SysCodegenPlan::getTableName, param.getTableName())
                        .eq(StringUtils.hasText(param.getGenType()), SysCodegenPlan::getGenType, param.getGenType())
                        .orderByDesc(SysCodegenPlan::getUpdatedAt));
    }

    @Override
    public List<SysCodegenDatabaseTableResult> tables() {
        return schemaMapper.listTables().stream()
                .map(row -> new SysCodegenDatabaseTableResult(
                        String.valueOf(row.get("table_name")),
                        blankToNull(row.get("table_comment"))))
                .toList();
    }

    @Override
    public List<SysCodegenDatabaseColumnResult> tableColumns(String tableName) {
        SqlSafe.requireIdent(tableName);
        return loadColumns(tableName).stream().map(this::toColumnResult).toList();
    }

    @Override
    public List<SysCodegenField> fields(String planId, String tableRole) {
        // 按主键加载
        if (getBaseMapper().selectById(planId) == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Codegen plan not found");
        }
        return fieldMapper.selectList(Wrappers.<SysCodegenField>lambdaQuery()
                .eq(SysCodegenField::getPlanId, planId)
                .eq(StringUtils.hasText(tableRole), SysCodegenField::getTableRole, tableRole)
                .orderByAsc(SysCodegenField::getTableRole)
                .orderByAsc(SysCodegenField::getSort)
                .orderByAsc(SysCodegenField::getId));
    }

    @Override
    @Transactional
    public void updateFieldsBatch(SysCodegenFieldsUpdateBatchParam request) {
        // 按主键加载
        SysCodegenPlan plan = getBaseMapper().selectById(request.getPlanId());
        if (plan == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Codegen plan not found");
        }
        AuditSnapshots.before(plan);
        fieldMapper.delete(Wrappers.<SysCodegenField>lambdaQuery()
                .eq(SysCodegenField::getPlanId, request.getPlanId()));
        OffsetDateTime now = OffsetDateTime.now();
        List<SysCodegenField> fields = new ArrayList<>();
        for (SysCodegenFieldUpdateItemParam item : request.getFields()) {
            SysCodegenField field = new SysCodegenField();
            BeanUtil.copyProperties(item, field);
            field.setId(null);
            field.setPlanId(request.getPlanId());
            field.setCreatedAt(now);
            field.setUpdatedAt(now);
            fields.add(field);
        }
        int size = BatchPartition.DEFAULT_SIZE;
        for (int i = 0; i < fields.size(); i += size) {
            Db.saveBatch(fields.subList(i, Math.min(i + size, fields.size())));
        }
        AuditSnapshots.after(plan);
    }

    @Override
    public List<Tree<String>> parentResources(String moduleId) {
        List<ResourceMenuNode> resources = resourceMenuApi.listParentMenus("ADMIN", moduleId);
        if (resources.isEmpty()) {
            return List.of();
        }

        Set<String> ids = resources.stream().map(ResourceMenuNode::getId).collect(Collectors.toSet());
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("id");
        config.setParentIdKey("parent_id");
        config.setNameKey("name");
        config.setWeightKey("weight");
        config.setChildrenKey("children");
        return TreeUtil.build(resources, null, config, (resource, tree) -> {
            String parentId = resource.getParentId();
            if (!StringUtils.hasText(parentId) || !ids.contains(parentId)) {
                parentId = null;
            }
            BeanUtil.beanToMap(resource, false, true).forEach((key, value) -> {
                if (!"children".equals(key)) {
                    tree.putExtra(StrUtil.toUnderlineCase(key), value);
                }
            });
            tree.setId(resource.getId());
            tree.setParentId(parentId);
            tree.setName(resource.getName());
            tree.setWeight(resource.getSort() == null ? 0 : resource.getSort());
        });
    }

    @Override
    public SysCodegenPreviewResult preview(String id) {
        // 按主键加载方案
        SysCodegenPlan plan = getBaseMapper().selectById(id);
        if (plan == null) {
            throw new BizException(404, "Codegen plan not found");
        }
        // 主表字段为空则反射同步
        List<SysCodegenField> mainFields = listFieldsEntity(plan.getId(), "MAIN");
        if (mainFields.isEmpty()) {
            syncReflectedFields(plan);
            mainFields = listFieldsEntity(plan.getId(), "MAIN");
        }
        // 关联类型：子表字段为空则同步
        List<SysCodegenField> subFields = List.of();
        if (RELATION_TYPES.contains(plan.getGenType()) && StringUtils.hasText(plan.getSubTable())) {
            subFields = listFieldsEntity(plan.getId(), "SUB");
            if (subFields.isEmpty()) {
                syncReflectedFields(plan);
                subFields = listFieldsEntity(plan.getId(), "SUB");
            }
        }
        // 渲染预览文件
        SysCodegenPreviewResult preview = new SysCodegenPreviewResult();
        preview.setFiles(templateEngine.render(plan, mainFields, subFields));
        return preview;
    }

    @Override
    public byte[] download(String id) {
        SysCodegenPreviewResult preview = preview(id);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (SysCodegenPreviewFileResult file : preview.getFiles()) {
                zos.putNextEntry(new ZipEntry(file.getPath()));
                zos.write(file.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            zos.finish();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new BizException("Failed to build codegen zip: " + ex.getMessage());
        }
    }

    private void validatePlan(SysCodegenPlanSaveParam request) {
        // 加载主表列并校验主键存在
        List<SysCodegenDatabaseColumnResult> mainColumns = loadColumns(request.getTableName()).stream()
                .map(this::toColumnResult).toList();
        Set<String> mainNames = new HashSet<>();
        for (SysCodegenDatabaseColumnResult column : mainColumns) {
            mainNames.add(column.getColumnName());
        }
        if (!mainNames.contains(request.getPkColumn())) {
            throw new BizException("Primary key field does not exist");
        }
        // 树类型：校验父字段与标签字段
        if (TREE_TYPES.contains(request.getGenType())) {
            if (!mainNames.contains(request.getTreeParentField())) {
                throw new BizException("Tree parent field does not exist");
            }
            if (!mainNames.contains(request.getTreeLabelField())) {
                throw new BizException("Tree label field does not exist");
            }
        }
        // 关联类型：校验子表配置与主键/外键列
        if (RELATION_TYPES.contains(request.getGenType())) {
            if (!StringUtils.hasText(request.getSubTable())
                    || !StringUtils.hasText(request.getSubPk())
                    || !StringUtils.hasText(request.getSubForeignKey())) {
                throw new BizException("Sub table configuration is incomplete");
            }
            Set<String> subNames = new HashSet<>();
            for (Map<String, Object> column : loadColumns(request.getSubTable())) {
                subNames.add(String.valueOf(column.get("column_name")));
            }
            if (!subNames.contains(request.getSubPk())) {
                throw new BizException("Sub primary key field does not exist");
            }
            if (!subNames.contains(request.getSubForeignKey())) {
                throw new BizException("Sub foreign key field does not exist");
            }
        }
    }

    private void syncReflectedFields(SysCodegenPlan plan) {
        // 同步主表反射字段
        upsertReflected(plan.getId(), "MAIN", loadColumns(plan.getTableName()));
        // 关联类型再同步子表
        if (RELATION_TYPES.contains(plan.getGenType()) && StringUtils.hasText(plan.getSubTable())) {
            upsertReflected(plan.getId(), "SUB", loadColumns(plan.getSubTable()));
        }
    }

    private void upsertReflected(String planId, String tableRole, List<Map<String, Object>> columns) {
        // 加载已有字段索引
        Map<String, SysCodegenField> existing = new HashMap<>();
        for (SysCodegenField field : listFieldsEntity(planId, tableRole)) {
            existing.put(field.getColumnName(), field);
        }
        OffsetDateTime now = OffsetDateTime.now();
        int index = 1;
        List<SysCodegenField> toCreate = new ArrayList<>();
        List<SysCodegenField> toUpdate = new ArrayList<>();
        // 按列合并新建/更新字段
        for (Map<String, Object> column : columns) {
            String columnName = String.valueOf(column.get("column_name"));
            boolean isPk = Boolean.TRUE.equals(column.get("is_primary_key"));
            boolean isAudit = AUDIT.contains(columnName);
            boolean nullable = "YES".equalsIgnoreCase(String.valueOf(column.get("is_nullable")));
            String[] types = DbTypeMapper.toDataAndFrontendType(
                    String.valueOf(column.get("data_type")),
                    String.valueOf(column.get("udt_name")));
            String widget = defaultWidget(columnName, types[0]);

            SysCodegenField field = existing.get(columnName);
            if (field == null) {
                field = new SysCodegenField();
                field.setPlanId(planId);
                field.setTableRole(tableRole);
                field.setColumnName(columnName);
                field.setInTable(!isAudit);
                field.setInForm(!isPk && !isAudit);
                field.setInDetail(true);
                field.setInQuery(Set.of("name", "title", "code", "status", "category", "type").contains(columnName));
                field.setWidget(widget);
                field.setDictCode("status".equals(columnName) ? "COMMON_STATUS" : null);
                field.setQueryOperator(defaultQueryOperator(columnName, types[0]));
                field.setCreatedAt(now);
            }
            field.setLabel(blankToNull(column.get("column_comment")));
            field.setDbType(String.valueOf(column.get("udt_name")));
            field.setValueType(types[0]);
            field.setUiType(types[1]);
            field.setPrimaryKey(isPk);
            field.setRequired(!nullable && !isPk && !isAudit);
            field.setUniqueFlag(false);
            field.setNullable(nullable);
            field.setMaxLength(asInteger(column.get("max_length")));
            Integer sort = asInteger(column.get("sort"));
            field.setSort(sort == null ? index : sort);
            field.setUpdatedAt(now);
            if (field.getId() == null) {
                toCreate.add(field);
            } else {
                toUpdate.add(field);
            }
            index++;
        }
        // 分批落库
        int size = BatchPartition.DEFAULT_SIZE;
        for (int i = 0; i < toCreate.size(); i += size) {
            Db.saveBatch(toCreate.subList(i, Math.min(i + size, toCreate.size())));
        }
        for (int i = 0; i < toUpdate.size(); i += size) {
            Db.updateBatchById(toUpdate.subList(i, Math.min(i + size, toUpdate.size())));
        }
    }

    private List<Map<String, Object>> loadColumns(String tableName) {
        SqlSafe.requireIdent(tableName);
        List<Map<String, Object>> columns = schemaMapper.listColumns(tableName);
        if (columns == null || columns.isEmpty()) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Database table not found");
        }
        Set<String> pks = new HashSet<>(schemaMapper.listPrimaryKeys(tableName));
        for (Map<String, Object> column : columns) {
            column.put("is_primary_key", pks.contains(String.valueOf(column.get("column_name"))));
        }
        return columns;
    }

    private List<SysCodegenField> listFieldsEntity(String planId, String tableRole) {
        // 组装查询条件
        return fieldMapper.selectList(Wrappers.<SysCodegenField>lambdaQuery()
                .eq(SysCodegenField::getPlanId, planId)
                .eq(SysCodegenField::getTableRole, tableRole)
                .orderByAsc(SysCodegenField::getSort));
    }

    private SysCodegenDatabaseColumnResult toColumnResult(Map<String, Object> column) {
        String[] types = DbTypeMapper.toDataAndFrontendType(
                String.valueOf(column.get("data_type")),
                String.valueOf(column.get("udt_name")));
        SysCodegenDatabaseColumnResult dto = new SysCodegenDatabaseColumnResult();
        dto.setColumnName(String.valueOf(column.get("column_name")));
        dto.setLabel(blankToNull(column.get("column_comment")));
        dto.setDbType(String.valueOf(column.get("udt_name")));
        dto.setValueType(types[0]);
        dto.setUiType(types[1]);
        dto.setPrimaryKey(Boolean.TRUE.equals(column.get("is_primary_key")));
        dto.setNullable("YES".equalsIgnoreCase(String.valueOf(column.get("is_nullable"))));
        dto.setMaxLength(asInteger(column.get("max_length")));
        return dto;
    }

    private static String defaultWidget(String columnName, String valueType) {
        if ("status".equals(columnName)) {
            return "dict";
        }
        if ("int".equals(valueType) || "float".equals(valueType)) {
            return "number";
        }
        if ("bool".equals(valueType)) {
            return "switch";
        }
        if ("datetime".equals(valueType)) {
            return "datetime";
        }
        if (columnName.contains("content") || columnName.contains("description") || columnName.contains("remark")) {
            return "textarea";
        }
        return "input";
    }

    private static String defaultQueryOperator(String columnName, String valueType) {
        if ("status".equals(columnName) || "int".equals(valueType) || "bool".equals(valueType)) {
            return "EQ";
        }
        if (Set.of("name", "title", "code", "category", "type").contains(columnName)) {
            return "LIKE";
        }
        return null;
    }

    private static String blankToNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? null : text;
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString();
        if (text.isBlank()) {
            return null;
        }
        return Integer.parseInt(text);
    }
}

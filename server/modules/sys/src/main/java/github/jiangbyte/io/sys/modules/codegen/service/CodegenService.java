package github.jiangbyte.io.sys.modules.codegen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenField;
import github.jiangbyte.io.sys.modules.codegen.entity.SysCodegenPlan;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenFieldsUpdateBatchParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenPlanPageParam;
import github.jiangbyte.io.sys.modules.codegen.param.SysCodegenPlanSaveParam;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenDatabaseColumnResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenDatabaseTableResult;
import github.jiangbyte.io.sys.modules.codegen.result.SysCodegenPreviewResult;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 代码生成服务接口：方案、字段、预览与导出。
 *
 * Author: Charlie
 */
public interface CodegenService extends IService<SysCodegenPlan> {

    /** 创建。 */
    void create(SysCodegenPlanSaveParam request);

    /** 更新。 */
    void update(SysCodegenPlanSaveParam request);

    /** 批量删除。 */
    void delete(IdsParam request);

    /** 查询详情。 */
    SysCodegenPlan detail(String id);

    /** 分页查询。 */
    Page<SysCodegenPlan> page(SysCodegenPlanPageParam param);

    /** 查询数据库表列表。 */
    List<SysCodegenDatabaseTableResult> tables();

    /** 查询表列元数据。 */
    List<SysCodegenDatabaseColumnResult> tableColumns(String tableName);

    /** 查询字段配置。 */
    List<SysCodegenField> fields(String planId, String tableRole);

    /** 批量更新字段配置。 */
    void updateFieldsBatch(SysCodegenFieldsUpdateBatchParam request);

    /** 查询可选父级资源树。 */
    List<Tree<String>> parentResources(String moduleId);

    /** 预览生成代码。 */
    SysCodegenPreviewResult preview(String id);

    /** 下载生成代码包。 */
    byte[] download(String id);
}

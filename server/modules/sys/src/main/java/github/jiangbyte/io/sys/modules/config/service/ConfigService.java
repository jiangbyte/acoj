package github.jiangbyte.io.sys.modules.config.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.config.entity.SysConfig;
import github.jiangbyte.io.sys.modules.config.param.SysConfigAddParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigBatchSaveParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigEditParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigPageParam;
import github.jiangbyte.io.sys.modules.config.result.SysConfigResult;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口：CRUD、批量保存与通知。
 *
 * Author: Charlie
 */
public interface ConfigService extends IService<SysConfig> {

    /** 创建。 */
    void create(SysConfigAddParam param);

    /** 更新。 */
    void update(SysConfigEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    SysConfigResult detail(String id);

    /** 分页查询。 */
    Page<SysConfigResult> page(SysConfigPageParam param);

    /** 按分类列表查询。 */
    List<SysConfigResult> list(String category);

    /** 批量保存。 */
    void batchSave(SysConfigBatchSaveParam param);

    /** 按键获取配置值。 */
    String getValue(String key);

    /** 按键获取配置值。 */
    String getValue(String key, String defaultValue);

    /** 按键获取布尔值。 */
    boolean getBoolean(String key, boolean defaultValue);

    /** 按键获取整型值。 */
    int getInt(String key, int defaultValue);

    /** 按键获取长整型值。 */
    long getLong(String key, long defaultValue);

    /** 获取全量配置快照。 */
    Map<String, String> snapshot();

    /** 失效本地配置缓存。 */
    void invalidateLocalCache();
}

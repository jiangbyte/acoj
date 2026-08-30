package github.jiangbyte.io.sys.modules.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.dict.entity.SysDict;
import github.jiangbyte.io.sys.modules.dict.param.SysDictAddParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictEditParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictPageParam;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 数据字典服务接口：CRUD 与按类型查询。
 *
 * Author: Charlie
 */
public interface DictService extends IService<SysDict> {

    /** 创建。 */
    void create(SysDictAddParam param);

    /** 更新。 */
    void update(SysDictEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    SysDict detail(String id);

    /** 分页查询。 */
    Page<SysDict> page(SysDictPageParam param);

    /** 按分类查询字典树。 */
    List<Tree<String>> tree(String category);

    /** 按字典类型查询。 */
    List<SysDict> listByType(String dictType);
}

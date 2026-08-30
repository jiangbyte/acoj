package github.jiangbyte.io.iam.modules.dept.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptAddParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptEditParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptPageParam;
import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 部门服务接口：CRUD、分页与部门树。
 *
 * Author: Charlie
 */
public interface DeptService extends IService<SysDept> {

    /** 创建部门。 */
    void create(SysDeptAddParam param);

    /** 更新部门。 */
    void update(SysDeptEditParam param);

    /** 批量删除部门。 */
    void delete(IdsParam param);

    /** 查询部门详情。 */
    SysDept detail(String id);

    /** 分页查询部门。 */
    Page<SysDept> page(SysDeptPageParam param);

    /** 构建部门组织树。 */
    List<Tree<String>> tree();
}

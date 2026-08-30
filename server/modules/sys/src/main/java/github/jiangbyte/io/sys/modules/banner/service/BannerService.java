package github.jiangbyte.io.sys.modules.banner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerAddParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerEditParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerPageParam;

import java.util.List;

/**
 * Banner 服务接口：CRUD 与门户查询。
 *
 * Author: Charlie
 */
public interface BannerService extends IService<SysBanner> {

    /** 创建。 */
    void create(SysBannerAddParam param);

    /** 更新。 */
    void update(SysBannerEditParam param);

    /** 批量删除。 */
    void delete(IdsParam param);

    /** 查询详情。 */
    SysBanner detail(String id);

    /** 分页查询。 */
    Page<SysBanner> page(SysBannerPageParam param);

    /** 门户列表查询。 */
    List<SysBanner> portalList(String position, String category, String type);

    /** 管理端列表查询。 */
    List<SysBanner> adminList(String position, String category, String type);

    /** Banner 互动上报。 */
    void interaction(String id);
}

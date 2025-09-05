package io.charlie.app.core.modular.sys.banner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.sys.banner.entity.SysBanner;
import io.charlie.app.core.modular.sys.banner.param.SysBannerAddParam;
import io.charlie.app.core.modular.sys.banner.param.SysBannerEditParam;
import io.charlie.app.core.modular.sys.banner.param.SysBannerIdParam;
import io.charlie.app.core.modular.sys.banner.param.SysBannerPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 横幅表 服务类
*/
public interface SysBannerService extends IService<SysBanner> {
    Page<SysBanner> page(SysBannerPageParam sysBannerPageParam);

    void add(SysBannerAddParam sysBannerAddParam);

    void edit(SysBannerEditParam sysBannerEditParam);

    void delete(List<SysBannerIdParam> sysBannerIdParamList);

    SysBanner detail(SysBannerIdParam sysBannerIdParam);

    List<SysBanner> latestN(int n);
}
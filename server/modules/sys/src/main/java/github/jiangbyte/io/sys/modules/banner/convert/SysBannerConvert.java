package github.jiangbyte.io.sys.modules.banner.convert;

import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerAddParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * Banner 模块 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysBannerConvert {

    /** 新增入参转实体。 */
    SysBanner toEntity(SysBannerAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysBannerEditParam param, @MappingTarget SysBanner entity);

}

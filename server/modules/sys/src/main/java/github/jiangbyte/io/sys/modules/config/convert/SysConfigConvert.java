package github.jiangbyte.io.sys.modules.config.convert;

import github.jiangbyte.io.sys.modules.config.entity.SysConfig;
import github.jiangbyte.io.sys.modules.config.param.SysConfigAddParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigEditParam;
import github.jiangbyte.io.sys.modules.config.result.SysConfigResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 系统配置 MapStruct 转换：入参/实体/结果映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysConfigConvert {

    /** 新增入参转实体。 */
    SysConfig toEntity(SysConfigAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysConfigEditParam param, @MappingTarget SysConfig entity);

    /** 实体转配置结果。 */
    SysConfigResult toResult(SysConfig entity);

}

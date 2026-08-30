package github.jiangbyte.io.iam.modules.resource.convert;

import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.entity.SysResourceModule;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceEditParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleAddParam;
import github.jiangbyte.io.iam.modules.resource.param.SysResourceModuleEditParam;
import github.jiangbyte.io.iam.modules.resource.result.SysResourceButtonResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * 资源/模块/按钮 MapStruct 转换。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysResourceConvert {

    /** 新增入参转资源实体。 */
    SysResource toEntity(SysResourceAddParam param);

    /** 编辑入参更新到资源实体。 */
    void update(SysResourceEditParam param, @MappingTarget SysResource entity);

    /** 新增入参转资源模块实体。 */
    SysResourceModule toModuleEntity(SysResourceModuleAddParam param);

    /** 编辑入参更新到资源模块实体。 */
    void updateModule(SysResourceModuleEditParam param, @MappingTarget SysResourceModule entity);

    /** 按钮资源转结果 DTO。 */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
    SysResourceButtonResult toButtonResult(SysResource button);
}

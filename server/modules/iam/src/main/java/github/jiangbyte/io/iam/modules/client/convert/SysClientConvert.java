package github.jiangbyte.io.iam.modules.client.convert;

import github.jiangbyte.io.iam.modules.client.entity.SysClientModule;
import github.jiangbyte.io.iam.modules.client.entity.SysClientResource;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientResourceEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 客户端模块/资源 MapStruct 转换：新增/编辑入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysClientConvert {

    /** 入参转客户端模块/资源实体。 */
    SysClientModule toEntity(SysClientModuleAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysClientModuleEditParam param, @MappingTarget SysClientModule entity);

    /** 入参转客户端模块/资源实体。 */
    SysClientResource toEntity(SysClientResourceAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysClientResourceEditParam param, @MappingTarget SysClientResource entity);
}

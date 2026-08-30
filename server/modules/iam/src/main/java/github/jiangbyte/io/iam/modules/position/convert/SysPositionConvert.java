package github.jiangbyte.io.iam.modules.position.convert;

import github.jiangbyte.io.iam.modules.position.entity.SysPosition;
import github.jiangbyte.io.iam.modules.position.param.SysPositionAddParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 岗位 MapStruct 转换：新增/编辑入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysPositionConvert {

    /** 新增入参转岗位实体。 */
    SysPosition toEntity(SysPositionAddParam param);

    /** 编辑入参更新到岗位实体。 */
    void update(SysPositionEditParam param, @MappingTarget SysPosition entity);

}

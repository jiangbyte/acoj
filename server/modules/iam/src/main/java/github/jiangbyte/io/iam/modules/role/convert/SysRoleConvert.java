package github.jiangbyte.io.iam.modules.role.convert;

import github.jiangbyte.io.iam.modules.role.entity.SysRole;
import github.jiangbyte.io.iam.modules.role.param.SysRoleAddParam;
import github.jiangbyte.io.iam.modules.role.param.SysRoleEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 角色 MapStruct 转换：新增/编辑入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysRoleConvert {

    /** 新增入参转角色实体。 */
    SysRole toEntity(SysRoleAddParam param);

    /** 编辑入参更新到角色实体。 */
    void update(SysRoleEditParam param, @MappingTarget SysRole entity);

}

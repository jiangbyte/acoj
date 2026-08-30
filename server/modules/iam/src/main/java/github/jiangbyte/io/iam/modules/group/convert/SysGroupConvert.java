package github.jiangbyte.io.iam.modules.group.convert;

import github.jiangbyte.io.iam.modules.group.entity.SysGroup;
import github.jiangbyte.io.iam.modules.group.param.SysGroupAddParam;
import github.jiangbyte.io.iam.modules.group.param.SysGroupEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 用户组 MapStruct 转换：新增/编辑入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysGroupConvert {

    /** 新增入参转用户组实体。 */
    SysGroup toEntity(SysGroupAddParam param);

    /** 编辑入参更新到用户组实体。 */
    void update(SysGroupEditParam param, @MappingTarget SysGroup entity);

}

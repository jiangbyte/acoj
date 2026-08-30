package github.jiangbyte.io.iam.modules.dept.convert;

import github.jiangbyte.io.iam.modules.dept.entity.SysDept;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptAddParam;
import github.jiangbyte.io.iam.modules.dept.param.SysDeptEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 部门 MapStruct 转换：新增/编辑入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysDeptConvert {

    /** 新增入参转部门实体。 */
    SysDept toEntity(SysDeptAddParam param);

    /** 编辑入参更新到部门实体。 */
    void update(SysDeptEditParam param, @MappingTarget SysDept entity);

}

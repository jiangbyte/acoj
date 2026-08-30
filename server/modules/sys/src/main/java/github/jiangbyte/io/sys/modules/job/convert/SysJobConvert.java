package github.jiangbyte.io.sys.modules.job.convert;

import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import github.jiangbyte.io.sys.modules.job.param.SysJobAddParam;
import github.jiangbyte.io.sys.modules.job.param.SysJobEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * 任务模块 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysJobConvert {

    /** 新增入参转实体。 */
    SysJob toEntity(SysJobAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysJobEditParam param, @MappingTarget SysJob entity);

}

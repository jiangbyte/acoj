package github.jiangbyte.io.sys.modules.file.convert;

import github.jiangbyte.io.sys.file.FileInfo;
import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import github.jiangbyte.io.sys.modules.file.param.SysFileEditParam;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * 文件模块 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysFileConvert {

    /** 编辑入参更新到实体。 */
    void update(SysFileEditParam param, @MappingTarget SysFile entity);

    /** 实体转跨模块 Info。 */
    @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
    FileInfo toInfo(SysFile file);
}

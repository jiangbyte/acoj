package github.jiangbyte.io.sys.modules.dict.convert;

import github.jiangbyte.io.sys.dict.DictItem;
import github.jiangbyte.io.sys.modules.dict.entity.SysDict;
import github.jiangbyte.io.sys.modules.dict.param.SysDictAddParam;
import github.jiangbyte.io.sys.modules.dict.param.SysDictEditParam;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * 数据字典 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysDictConvert {

    /** 新增入参转实体。 */
    SysDict toEntity(SysDictAddParam param);

    /** 编辑入参更新到实体。 */
    void update(SysDictEditParam param, @MappingTarget SysDict entity);

    /** 实体转跨模块 DictItem。 */
    @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
    DictItem toItem(SysDict dict);
}

package github.jiangbyte.io.oj.modules.tag.convert;

/**
 * OJ 标签 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.oj.modules.tag.entity.OjTag;
import github.jiangbyte.io.oj.modules.tag.param.OjTagAddParam;
import github.jiangbyte.io.oj.modules.tag.param.OjTagEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OjTagConvert {

    /** 新增入参转实体。 */
    OjTag toEntity(OjTagAddParam param);

    /** 编辑入参更新到实体。 */
    void update(OjTagEditParam param, @MappingTarget OjTag entity);
}

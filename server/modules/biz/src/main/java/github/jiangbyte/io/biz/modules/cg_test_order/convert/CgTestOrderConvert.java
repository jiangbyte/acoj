package github.jiangbyte.io.biz.modules.cg_test_order.convert;

/**
 * Order MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrder;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CgTestOrderConvert {

    /** 新增入参转实体。 */
    CgTestOrder toEntity(CgTestOrderAddParam param);

    /** 编辑入参更新到实体。 */
    void update(CgTestOrderEditParam param, @MappingTarget CgTestOrder entity);
}

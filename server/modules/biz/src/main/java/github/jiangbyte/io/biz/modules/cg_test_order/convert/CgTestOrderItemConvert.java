package github.jiangbyte.io.biz.modules.cg_test_order.convert;

/**
 * 订单明细 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrderItem;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CgTestOrderItemConvert {

    /** 新增入参转实体。 */
    CgTestOrderItem toEntity(CgTestOrderItemAddParam param);

    /** 编辑入参更新到实体。 */
    void update(CgTestOrderItemEditParam param, @MappingTarget CgTestOrderItem entity);
}

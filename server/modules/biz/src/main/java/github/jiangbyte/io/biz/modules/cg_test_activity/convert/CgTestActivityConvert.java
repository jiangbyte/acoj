package github.jiangbyte.io.biz.modules.cg_test_activity.convert;

/**
 * Activity MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.biz.modules.cg_test_activity.entity.CgTestActivity;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityAddParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CgTestActivityConvert {

    /** 新增入参转实体。 */
    CgTestActivity toEntity(CgTestActivityAddParam param);

    /** 编辑入参更新到实体。 */
    void update(CgTestActivityEditParam param, @MappingTarget CgTestActivity entity);
}

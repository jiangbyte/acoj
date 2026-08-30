package github.jiangbyte.io.sys.modules.feedback.convert;

import github.jiangbyte.io.sys.modules.feedback.entity.SysFeedback;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackAddParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * 反馈对象转换：新增参数到 {@link github.jiangbyte.io.sys.modules.feedback.entity.SysFeedback} 实体的 MapStruct 映射。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysFeedbackConvert {

    /**
     * 将新增参数映射为反馈实体。
     */
    SysFeedback toEntity(SysFeedbackAddParam param);

}

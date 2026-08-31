package github.jiangbyte.io.oj.modules.problem.convert;

/**
 * OJ 题目 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemAddParam;
import github.jiangbyte.io.oj.modules.problem.param.OjProblemEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OjProblemConvert {

    /** 新增入参转实体。 */
    OjProblem toEntity(OjProblemAddParam param);

    /** 编辑入参更新到实体。 */
    void update(OjProblemEditParam param, @MappingTarget OjProblem entity);
}

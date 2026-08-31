package github.jiangbyte.io.oj.modules.problemsolution.convert;

import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionAddParam;
import github.jiangbyte.io.oj.modules.problemsolution.param.OjProblemSolutionEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * OJ 参考答案 MapStruct 转换。
 *
 * Author: Charlie
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OjProblemSolutionConvert {

    /** 新增入参转实体。 */
    OjProblemSolution toEntity(OjProblemSolutionAddParam param);

    /** 编辑入参更新到实体。 */
    void update(OjProblemSolutionEditParam param, @MappingTarget OjProblemSolution entity);
}

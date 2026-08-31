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
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OjProblemConvert {

    /** 新增入参转实体。 */
    @Mapping(target = "languageLimits", ignore = true)
    @Mapping(target = "caseVersion", ignore = true)
    @Mapping(target = "submitCount", ignore = true)
    @Mapping(target = "acceptCount", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "tagIds", ignore = true)
    @Mapping(target = "myStatus", ignore = true)
    OjProblem toEntity(OjProblemAddParam param);

    /** 编辑入参更新到实体。 */
    @Mapping(target = "languageLimits", ignore = true)
    @Mapping(target = "caseVersion", ignore = true)
    @Mapping(target = "submitCount", ignore = true)
    @Mapping(target = "acceptCount", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "tagIds", ignore = true)
    @Mapping(target = "myStatus", ignore = true)
    void update(OjProblemEditParam param, @MappingTarget OjProblem entity);
}

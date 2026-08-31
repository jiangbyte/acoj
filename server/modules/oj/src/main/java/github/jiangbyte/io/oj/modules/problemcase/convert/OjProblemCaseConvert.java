package github.jiangbyte.io.oj.modules.problemcase.convert;

/**
 * OJ 题目测例 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseAddParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseEditParam;
import github.jiangbyte.io.oj.modules.problemcase.param.OjProblemCaseItemParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OjProblemCaseConvert {

    /** 新增入参转实体。 */
    OjProblemCase toEntity(OjProblemCaseAddParam param);

    /** 测例条目转实体。 */
    OjProblemCase toEntity(OjProblemCaseItemParam param);

    /** 编辑入参更新到实体。 */
    void update(OjProblemCaseEditParam param, @MappingTarget OjProblemCase entity);
}

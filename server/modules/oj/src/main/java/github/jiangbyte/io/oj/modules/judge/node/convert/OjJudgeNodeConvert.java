package github.jiangbyte.io.oj.modules.judge.node.convert;

/**
 * OJ 执行机 MapStruct 转换：入参与实体映射。
 *
 * Author: Charlie
 */

import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeAddParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeEditParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OjJudgeNodeConvert {

    /** 新增入参转实体。 */
    OjJudgeNode toEntity(OjJudgeNodeAddParam param);

    /** 编辑入参更新到实体。 */
    void update(OjJudgeNodeEditParam param, @MappingTarget OjJudgeNode entity);
}

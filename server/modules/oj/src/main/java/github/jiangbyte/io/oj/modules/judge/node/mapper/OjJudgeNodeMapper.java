package github.jiangbyte.io.oj.modules.judge.node.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 执行机 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjJudgeNodeMapper extends BaseMapper<OjJudgeNode> {
}

package github.jiangbyte.io.oj.modules.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.problem.entity.OjProblem;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 题目 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjProblemMapper extends BaseMapper<OjProblem> {
}

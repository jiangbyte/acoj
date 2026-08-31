package github.jiangbyte.io.oj.modules.problemsolution.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.problemsolution.entity.OjProblemSolution;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 题目参考答案 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjProblemSolutionMapper extends BaseMapper<OjProblemSolution> {
}

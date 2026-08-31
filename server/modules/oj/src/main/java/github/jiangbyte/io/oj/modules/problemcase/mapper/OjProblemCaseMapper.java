package github.jiangbyte.io.oj.modules.problemcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 题目测例 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjProblemCaseMapper extends BaseMapper<OjProblemCase> {
}

package github.jiangbyte.io.oj.modules.problemlanguagelimit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.problemlanguagelimit.entity.OjProblemLanguageLimit;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 题目语言限额 Mapper。
 * <p>
 * Author: Charlie
 */
@Mapper
public interface OjProblemLanguageLimitMapper extends BaseMapper<OjProblemLanguageLimit> {
}

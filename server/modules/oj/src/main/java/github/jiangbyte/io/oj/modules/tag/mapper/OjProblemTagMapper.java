package github.jiangbyte.io.oj.modules.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.tag.entity.OjProblemTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 题目-标签关联 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjProblemTagMapper extends BaseMapper<OjProblemTag> {
}

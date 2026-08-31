package github.jiangbyte.io.oj.modules.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.tag.entity.OjTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 题目标签 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjTagMapper extends BaseMapper<OjTag> {
}

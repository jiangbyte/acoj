package github.jiangbyte.io.oj.modules.stat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.stat.entity.OjUserProblemStat;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 用户题目统计 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjUserProblemStatMapper extends BaseMapper<OjUserProblemStat> {
}

package github.jiangbyte.io.oj.modules.problemdryrun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.problemdryrun.entity.OjProblemDryRun;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 试跑历史 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjProblemDryRunMapper extends BaseMapper<OjProblemDryRun> {
}

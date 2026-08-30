package github.jiangbyte.io.sys.modules.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.job.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务定义表 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
}

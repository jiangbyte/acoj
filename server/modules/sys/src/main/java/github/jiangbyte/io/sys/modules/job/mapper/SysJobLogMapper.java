package github.jiangbyte.io.sys.modules.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.job.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务执行记录表 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
}

package github.jiangbyte.io.sys.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.audit.entity.SysAlertLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警日志表 MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysAlertLogMapper extends BaseMapper<SysAlertLog> {
}

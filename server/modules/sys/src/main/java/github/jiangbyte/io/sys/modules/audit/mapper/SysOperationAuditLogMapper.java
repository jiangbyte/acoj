package github.jiangbyte.io.sys.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作审计日志表 MyBatis-Plus Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface SysOperationAuditLogMapper extends BaseMapper<SysOperationAuditLog> {
}

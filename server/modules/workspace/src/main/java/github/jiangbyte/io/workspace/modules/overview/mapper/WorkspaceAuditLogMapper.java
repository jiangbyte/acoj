package github.jiangbyte.io.workspace.modules.overview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.workspace.modules.overview.entity.WorkspaceAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作台审计日志 Mapper（MyBatis-Plus）。
 *
 * Author: Charlie
 */
@Mapper
public interface WorkspaceAuditLogMapper extends BaseMapper<WorkspaceAuditLog> {
}

package github.jiangbyte.io.sys.modules.audit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.log.audit.AuditEventMessage;
import github.jiangbyte.io.sys.modules.audit.entity.SysOperationAuditLog;
import github.jiangbyte.io.sys.modules.audit.param.SysAuditPageParam;

/**
 * 操作审计服务接口：分页与写入。
 *
 * Author: Charlie
 */
public interface AuditService extends IService<SysOperationAuditLog> {

    /** 分页查询审计日志。 */
    Page<SysOperationAuditLog> page(SysAuditPageParam param);

    /** 当前登录用户本人审计日志分页（强制绑定 accountId）。 */
    Page<SysOperationAuditLog> myPage(SysAuditPageParam param);

    /** 查询详情。 */
    SysOperationAuditLog detail(String id);

    /** 当前登录用户本人审计详情（越权返回 404）。 */
    SysOperationAuditLog myDetail(String id);

    /** 持久化审计事件。 */
    void persistEvent(AuditEventMessage event);

    /**
     * 清理过期登录/登出审计日志。
     *
     * @param retentionDays 保留天数；小于等于 0 时不删除
     * @param batchSize 单次删除上限；小于等于 0 时按 1000 处理
     * @return 实际删除行数
     */
    int cleanupExpiredLoginLogs(int retentionDays, int batchSize);

    /**
     * 清理过期操作审计日志（不含 login/logout）。
     *
     * @param retentionDays 保留天数；小于等于 0 时不删除
     * @param batchSize 单次删除上限；小于等于 0 时按 1000 处理
     * @return 实际删除行数
     */
    int cleanupExpiredOperationLogs(int retentionDays, int batchSize);
}

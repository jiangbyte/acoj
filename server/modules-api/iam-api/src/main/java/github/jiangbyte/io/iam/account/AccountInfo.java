package github.jiangbyte.io.iam.account;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 跨模块账号快照：账号状态、密码哈希与最近登录/注销元数据。
 * 非 HTTP 结果，亦非持久化实体。
 *
 * Author: Charlie
 */
@Data
public class AccountInfo {
    private String id;
    private String accountType;
    private String accountStatus;
    private String passwordHash;
    private String latestLoginIp;
    private OffsetDateTime latestLoginTime;
    private String latestLoginDevice;
    private OffsetDateTime cancelledAt;
    private String cancelledBy;
    private String cancelReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

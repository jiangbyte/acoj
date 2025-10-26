package io.charlie.web.oj.modular.data.ranking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;

import java.math.BigDecimal;
import java.util.List;

public interface UserActivityService {
    
    /**
     * 增加用户活跃度
     * @param userId 用户ID
     * @param actionType 操作类型：LOGIN, SUBMIT
     * @param isSolved 是否解题成功
     */
    void addActivity(String userId, String actionType, boolean isSolved);
    
    /**
     * 获取用户当月活跃度
     */
    BigDecimal getUserActivityScore(String userId);
    
    /**
     * 分页查询活跃用户
     */
    Page<SysUser> getActiveUsersPage(Page<SysUser> page);
    
    /**
     * 获取TopN活跃用户
     */
    List<SysUser> getTopNActiveUsers(int n);
    
    /**
     * 获取用户活跃度排名
     */
    Long getUserActivityRank(String userId);
    
    /**
     * 每月重置活跃度
     */
    void resetMonthlyActivity();
}
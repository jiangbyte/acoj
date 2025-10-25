package io.charlie.web.oj.modular.context;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据权限上下文
 */
@Slf4j
@Data
@Builder
public class DataScopeContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String groupId;
    private List<String> roleCodes;
    private List<String> roleIds;
    private Set<String> dataScopes;
    private String maxDataScope;
    private List<String> accessibleRoleIds;
    private List<String> accessibleGroupIds;
    private List<String> accessibleMenuIds;
    private boolean isSuperAdmin;

    /**
     * 构建完成后初始化 maxDataScope
     */
    public String getMaxDataScope() {
        if (this.maxDataScope == null) {
            if (this.dataScopes != null && !this.dataScopes.isEmpty()) {
                this.maxDataScope = getHighestDataScope(this.dataScopes);
            } else {
                this.maxDataScope = DataScopeConstant.SELF;
            }
        }
        return this.maxDataScope;
    }

    /**
     * 确保 dataScopes 不为空
     */
    public Set<String> getDataScopes() {
        if (this.dataScopes == null || this.dataScopes.isEmpty()) {
            this.dataScopes = Set.of(DataScopeConstant.SELF);
        }
        return this.dataScopes;
    }

    public boolean hasDataScope(String dataScope) {
        return getDataScopes().contains(dataScope);
    }

    public boolean getIsSuperAdmin() {
        // 如果最高权限是 all，则返回 true
        return maxDataScope.equals(DataScopeConstant.ALL);
    }

    /**
     * 获取最高的数据权限
     */
    private String getHighestDataScope(Set<String> dataScopes) {
        return dataScopes.stream()
                .min(Comparator.comparingInt(scope ->
                        DataScopeConstant.DATA_SCOPE_LEVEL.getOrDefault(scope, Integer.MAX_VALUE)))
                .orElse(DataScopeConstant.SELF);
    }

    public void printDataScopeContext() {
        log.info("数据权限上下文初始化完成：{}", this);
    }
}
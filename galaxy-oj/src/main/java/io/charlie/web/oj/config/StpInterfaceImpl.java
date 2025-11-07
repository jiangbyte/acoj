package io.charlie.web.oj.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.context.DataScopeContext;
import io.charlie.web.oj.context.DataScopeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final DataScopeUtil dataScopeUtil;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        DataScopeContext dataScopeContext = dataScopeUtil.getDataScopeContext();
        List<String> permissionList = dataScopeContext.getAccessiblePermissions();
        log.debug("permissionList: {}", JSONUtil.toJsonStr(permissionList));
        return permissionList;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        DataScopeContext dataScopeContext = dataScopeUtil.getDataScopeContext();
        List<String> roleList = dataScopeContext.getAccessibleRoleIds();
        log.debug("roleList: {}", JSONUtil.toJsonStr(roleList));
        return roleList;
    }

}

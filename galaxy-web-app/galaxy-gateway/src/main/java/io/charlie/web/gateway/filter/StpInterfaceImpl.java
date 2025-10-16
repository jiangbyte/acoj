//package io.charlie.web.gateway.filter;
//
//import cn.dev33.satoken.stp.StpInterface;
//import cn.hutool.json.JSONUtil;
//import io.charlie.permission.PermissionService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.config.annotation.DubboReference;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * 自定义权限验证接口扩展
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StpInterfaceImpl implements StpInterface {
//    @DubboReference
//    private final PermissionService permissionService;
//
//    @Override
//    public List<String> getPermissionList(Object loginId, String loginType) {
//        List<String> permissionList = permissionService.getPermissionList((String) loginId, loginType);
//        log.info("permissionList: {}", JSONUtil.toJsonStr(permissionList));
//        return permissionList;
//    }
//
//    @Override
//    public List<String> getRoleList(Object loginId, String loginType) {
//        // 返回此 loginId 拥有的角色列表
//        return List.of();
//    }
//
//}

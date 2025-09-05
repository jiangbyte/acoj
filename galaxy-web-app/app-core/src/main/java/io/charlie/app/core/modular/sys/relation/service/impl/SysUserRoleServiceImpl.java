package io.charlie.app.core.modular.sys.relation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.app.core.modular.sys.relation.service.SysUserRoleService;
import io.charlie.app.core.modular.sys.relation.entity.SysUserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-05
* @description 用户-角色 关联表(1-N) 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
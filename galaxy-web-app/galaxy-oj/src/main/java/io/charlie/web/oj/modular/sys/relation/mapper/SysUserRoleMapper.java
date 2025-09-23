package io.charlie.web.oj.modular.sys.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-05
* @description 用户-角色 关联表(1-N) Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}

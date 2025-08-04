package io.charlie.app.core.modular.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.app.core.modular.relation.entity.SysUserRole;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import org.apache.ibatis.annotations.CacheNamespace;
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

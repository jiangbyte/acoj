package io.charlie.app.core.modular.sys.user.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 用户表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysUserMapper extends BaseMapper<SysUser> {

}

package io.charlie.web.oj.modular.sys.menu.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-24
* @description 菜单表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysMenuMapper extends BaseMapper<SysMenu> {

}

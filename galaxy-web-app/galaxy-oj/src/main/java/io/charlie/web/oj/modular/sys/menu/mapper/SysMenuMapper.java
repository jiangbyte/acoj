package io.charlie.web.oj.modular.sys.menu.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-24
 * @description 菜单表 Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT JSON_UNQUOTE(JSON_EXTRACT(m.ex_json, '$[*]')) as permission " +
            "FROM sys_user_role ur " +
            "JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "JOIN sys_menu m ON rm.menu_id = m.id " +
            "WHERE ur.user_id = #{userId} " +
            "AND m.ex_json IS NOT NULL " +
            "AND m.ex_json != '[]'")
    List<String> selectPermissionsByUserId(String userId);
}

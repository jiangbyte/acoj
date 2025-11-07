package io.charlie.web.oj.modular.sys.group.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 用户组表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysGroupMapper extends BaseMapper<SysGroup> {
    /**
     * 查询用户组及其所有子组的ID列表（包含自身）
     * @param groupId 用户组ID
     * @return 用户组ID列表
     */
    @DS("slave")
    List<String> selectGroupAndChildrenIds(@Param("groupId") String groupId);

    /**
     * 查询用户组及其所有子组的ID列表（可选择是否包含自身）
     * @param groupId 用户组ID
     * @param includeSelf 是否包含自身
     * @return 用户组ID列表
     */
    @DS("slave")
    List<String> selectGroupAndChildrenIdsWithOption(@Param("groupId") String groupId,
                                                     @Param("includeSelf") boolean includeSelf);
}

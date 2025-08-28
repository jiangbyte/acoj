package io.charlie.app.core.modular.set.solved.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户题集解决记录表 Mapper 接口
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSetSolvedMapper extends BaseMapper<ProSetSolved> {
    IPage<SysUser> selectSetUsers(IPage<SysUser> page, Wrapper<SysUser> ew, String setId);
}

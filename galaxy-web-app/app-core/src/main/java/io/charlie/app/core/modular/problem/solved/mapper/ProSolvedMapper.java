package io.charlie.app.core.modular.problem.solved.mapper;

import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 用户解决表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSolvedMapper extends BaseMapper<ProSolved> {

}

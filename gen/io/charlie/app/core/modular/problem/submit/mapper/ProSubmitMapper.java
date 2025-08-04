package io.charlie.app.core.modular.problem.submit.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-27
* @description 提交表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSubmitMapper extends BaseMapper<ProSubmit> {

}

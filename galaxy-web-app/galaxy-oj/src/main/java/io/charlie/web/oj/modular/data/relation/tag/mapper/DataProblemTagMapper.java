package io.charlie.web.oj.modular.data.relation.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.relation.tag.entity.DataProblemTag;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-05
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataProblemTagMapper extends BaseMapper<DataProblemTag> {

}

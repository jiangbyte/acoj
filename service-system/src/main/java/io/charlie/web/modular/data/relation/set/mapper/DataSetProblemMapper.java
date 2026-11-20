package io.charlie.web.modular.data.relation.set.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.web.modular.data.relation.set.entity.DataSetProblem;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-05
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataSetProblemMapper extends BaseMapper<DataSetProblem> {

}

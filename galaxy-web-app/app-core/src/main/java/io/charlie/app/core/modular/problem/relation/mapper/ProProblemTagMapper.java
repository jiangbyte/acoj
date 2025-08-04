package io.charlie.app.core.modular.problem.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.app.core.modular.problem.relation.entity.ProProblemTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 27/07/2025
 * @description 题目-标签 关联表(1-N) Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProProblemTagMapper extends BaseMapper<ProProblemTag> {

}
package io.charlie.app.core.modular.problem.mapper;

import io.charlie.app.core.modular.problem.entity.submit.ProSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 提交表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSubmitMapper extends BaseMapper<ProSubmit> {

}

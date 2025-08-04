package io.charlie.app.core.modular.set.mapper;

import io.charlie.app.core.modular.set.entity.submit.ProSetSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-22
* @description 题单提交表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ProSetSubmitMapper extends BaseMapper<ProSetSubmit> {

}

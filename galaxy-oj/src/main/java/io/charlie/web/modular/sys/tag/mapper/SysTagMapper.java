package io.charlie.web.modular.sys.tag.mapper;

import io.charlie.cores.cache.MybatisPlusRedisCache;
import io.charlie.web.modular.sys.tag.entity.SysTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 标签表 Mapper 接口
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysTagMapper extends BaseMapper<SysTag> {

}

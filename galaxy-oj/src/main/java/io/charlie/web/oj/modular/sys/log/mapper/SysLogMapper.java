package io.charlie.web.oj.modular.sys.log.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.sys.log.entity.SysLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-17
* @description 系统活动/日志记录表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysLogMapper extends BaseMapper<SysLog> {

}

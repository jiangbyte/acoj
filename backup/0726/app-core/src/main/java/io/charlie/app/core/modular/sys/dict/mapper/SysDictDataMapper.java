package io.charlie.app.core.modular.sys.dict.mapper;

import io.charlie.app.core.modular.sys.dict.entity.SysDictData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 字典数据表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

}

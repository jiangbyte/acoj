package io.charlie.web.modular.data.library.mapper;

import io.charlie.web.modular.data.library.entity.DataLibrary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交样本库 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataLibraryMapper extends BaseMapper<DataLibrary> {

}

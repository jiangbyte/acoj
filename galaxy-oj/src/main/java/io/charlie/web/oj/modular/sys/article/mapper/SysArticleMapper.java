package io.charlie.web.oj.modular.sys.article.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.sys.article.entity.SysArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 系统文章表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface SysArticleMapper extends BaseMapper<SysArticle> {

}

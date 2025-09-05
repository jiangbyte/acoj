package ${package.Mapper};

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import org.apache.ibatis.annotations.CacheNamespace;
<#if mapperAnnotationClass??>
import ${mapperAnnotationClass.name};
</#if>

/**
* @author ${author}
* @version v1.0
* @date ${date}
* @description ${table.comment!} Mapper 接口
*/
<#if mapperAnnotationClass??>
@${mapperAnnotationClass.simpleName}
</#if>
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

}

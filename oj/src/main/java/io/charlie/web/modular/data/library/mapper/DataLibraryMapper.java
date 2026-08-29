package io.charlie.web.modular.data.library.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 提交样本库 Mapper 接口
 */
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataLibraryMapper extends BaseMapper<DataLibrary> {

    /**
     * 忽略逻辑删除：仅恢复软删行，业务字段由后续常规 update 刷新（保证 JSON TypeHandler）
     */
    @DS("master")
    @InterceptorIgnore(logicDelete = "true")
    @Update("""
            UPDATE data_library
            SET deleted = 0
            WHERE user_id = #{userId}
              AND module_type = #{moduleType}
              AND module_id = #{moduleId}
              AND problem_id = #{problemId}
              AND language = #{language}
            """)
    int reviveByBusinessKey(@Param("userId") String userId,
                            @Param("moduleType") String moduleType,
                            @Param("moduleId") String moduleId,
                            @Param("problemId") String problemId,
                            @Param("language") String language);
}

package io.charlie.web.oj.modular.data.submit.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.web.oj.modular.data.submit.param.JudgeStatusCountDTO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交表 Mapper 接口
*/
@Mapper
//@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface DataSubmitMapper extends BaseMapper<DataSubmit> {
    /**
     * 按状态统计提交数量
     */
    @DS("slave")
    @Select("SELECT status, COUNT(*) as count FROM data_submit WHERE deleted = 0 AND module_type = 'PROBLEM' GROUP BY status")
    List<JudgeStatusCountDTO> countByStatus();
}

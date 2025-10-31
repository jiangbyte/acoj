package io.charlie.web.oj.modular.data.similarity.mapper;

import io.charlie.galaxy.cache.MybatisPlusRedisCache;
import io.charlie.web.oj.modular.data.similarity.dto.CloneLevel;
import io.charlie.web.oj.modular.data.similarity.dto.TaskReportStats;
import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-21
* @description 检测结果任务库 Mapper 接口
*/
@Mapper
@CacheNamespace(implementation = MybatisPlusRedisCache.class, eviction = MybatisPlusRedisCache.class)
public interface TaskSimilarityMapper extends BaseMapper<TaskSimilarity> {
    TaskReportStats selectSimilarityStats(String taskId, String problemId, String setId, int isSet);

    TaskReportStats selectSimilarityStatsByTaskId(String taskId);

    /**
     * 相似度分布查询 - 0-100分10个区间，返回数量数组
     * 返回格式: [0,0,1,2,5,6,5,4,8,1]
     */
    List<Integer> selectSimilarityDistribution(@Param("taskId") String taskId);

    /**
     * 程度统计查询 - 基于阈值
     * 返回每个可疑程度的统计信息
     */
    List<CloneLevel> selectDegreeStatistics(
            @Param("taskId") String taskId,
            @Param("threshold") BigDecimal threshold
    );

    /**
     * 根据相似度获取所属程度
     */
    String getDegreeBySimilarity(
            @Param("similarity") BigDecimal similarity,
            @Param("threshold") BigDecimal threshold
    );
}

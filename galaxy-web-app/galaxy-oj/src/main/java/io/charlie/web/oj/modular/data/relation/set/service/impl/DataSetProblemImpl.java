package io.charlie.web.oj.modular.data.relation.set.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.relation.set.entity.DataSetProblem;
import io.charlie.web.oj.modular.data.relation.set.mapper.DataSetProblemMapper;
import io.charlie.web.oj.modular.data.relation.set.service.DataSetProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
@Service
@RequiredArgsConstructor
public class DataSetProblemImpl extends ServiceImpl<DataSetProblemMapper, DataSetProblem> implements DataSetProblemService {
    private final DataProblemMapper dataProblemMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存key前缀
    private static final String DATACACHE_SET_PROBLEM_PROBLEMS = "datacache:set:problem:problems:";
    private static final String DATACACHE_SET_PROBLEM_IDS = "datacache:set:problem:ids:";
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_TIME = 24 * 60 * 60;
    // 空值缓存过期时间（5分钟，防止缓存穿透）
    private static final long NULL_CACHE_EXPIRE_TIME = 5 * 60;

    @Override
    public List<DataProblem> getProblemsBySetId(String setId) {
        String cacheKey = DATACACHE_SET_PROBLEM_PROBLEMS + setId;

        // 优先查询缓存
        List<DataProblem> cachedProblems = (List<DataProblem>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProblems != null) {
            return cachedProblems;
        }

        // 缓存不存在，查询数据库
        List<DataSetProblem> dataSetProblems = this.baseMapper.selectList(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
        );

        List<DataProblem> problems;
        if (ObjectUtil.isEmpty(dataSetProblems)) {
            // 数据库为空，缓存空值（防止缓存穿透）
            problems = List.of();
            redisTemplate.opsForValue().set(cacheKey, problems, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            return problems;
        }

        // 得到题目ID
        List<String> problemIds = dataSetProblems.stream()
                .map(DataSetProblem::getProblemId)
                .toList();

        // 再去找题目
        if (ObjectUtil.isNotEmpty(problemIds)) {
            problems = dataProblemMapper.selectByIds(problemIds);
            // 将结果存入缓存
            redisTemplate.opsForValue().set(cacheKey, problems, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } else {
            // 题目ID为空，缓存空值
            problems = List.of();
            redisTemplate.opsForValue().set(cacheKey, problems, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        return problems;
    }

    @Override
    public List<String> getProblemIdsBySetId(String setId) {
        String cacheKey = DATACACHE_SET_PROBLEM_IDS + setId;

        // 优先查询缓存
        List<String> cachedProblemIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProblemIds != null) {
            return cachedProblemIds;
        }

        // 获取数据集下的题目
        List<DataSetProblem> dataSetProblems = this.baseMapper.selectList(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
        );

        List<String> problemIds;
        if (ObjectUtil.isEmpty(dataSetProblems)) {
            // 数据库为空，缓存空值（防止缓存穿透）
            problemIds = List.of();
            redisTemplate.opsForValue().set(cacheKey, problemIds, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            return problemIds;
        }

        problemIds = dataSetProblems.stream()
                .map(DataSetProblem::getProblemId)
                .toList();

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, problemIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        return problemIds;
    }

    @Override
    public boolean addOrUpdate(String setId, List<String> problemIds) {
        // 先清除题集下的所有题目
        this.remove(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
        );

        if (ObjectUtil.isNotEmpty(problemIds)) {
            problemIds.forEach(item -> {
                DataSetProblem dataSetProblem = new DataSetProblem();
                dataSetProblem.setSetId(setId);
                dataSetProblem.setProblemId(item);
                // 添加
                this.save(dataSetProblem);
            });
        }
        clearCache(setId);
        return true;
    }

    @Override
    public boolean addOrUpdateWithSort(String setId, List<DataSetProblem> sets) {
        // 先清除题集下的所有题目
        this.remove(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
        );
        if (ObjectUtil.isNotEmpty(sets)) {
            sets.forEach(item -> {
                item.setSetId(setId);
                // 添加
                this.save(item);
            });
        }
        clearCache(setId);
        return true;
    }

    private void clearCache(String setId) {
        String setProblemIdsKey = DATACACHE_SET_PROBLEM_IDS + setId;
        String setProblemProblemsKey = DATACACHE_SET_PROBLEM_PROBLEMS + setId;
        redisTemplate.delete(setProblemIdsKey);
        redisTemplate.delete(setProblemProblemsKey);
    }
}

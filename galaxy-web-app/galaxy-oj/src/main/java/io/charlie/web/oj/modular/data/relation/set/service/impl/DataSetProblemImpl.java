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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
                .orderByAsc(DataSetProblem::getSort)
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
                .orderByAsc(DataSetProblem::getSort)
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
                .sorted(Comparator.naturalOrder())
                .toList();

        // 将结果存入缓存
        redisTemplate.opsForValue().set(cacheKey, problemIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        return problemIds;
    }

    @Override
    public Map<String, List<String>> getProblemIdsBySetIds(List<String> setIds) {
        if (ObjectUtil.isEmpty(setIds)) {
            return Map.of();
        }

        Map<String, List<String>> result = new HashMap<>();
        List<String> uncachedSetIds = new ArrayList<>();

        // 第一步：先尝试从缓存中获取
        for (String setId : setIds) {
            String cacheKey = DATACACHE_SET_PROBLEM_IDS + setId;
            List<String> cachedProblemIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProblemIds != null) {
                result.put(setId, cachedProblemIds);
            } else {
                uncachedSetIds.add(setId);
            }
        }

        // 如果所有数据都在缓存中，直接返回
        if (uncachedSetIds.isEmpty()) {
            return result;
        }

        // 第二步：查询数据库中未缓存的数据
        List<DataSetProblem> dataSetProblems = this.baseMapper.selectList(new LambdaQueryWrapper<DataSetProblem>()
                .in(DataSetProblem::getSetId, uncachedSetIds)
                .orderByAsc(DataSetProblem::getSetId)
                .orderByAsc(DataSetProblem::getSort)
        );

        // 按setId分组
        Map<String, List<DataSetProblem>> setProblemsMap = dataSetProblems.stream()
                .collect(Collectors.groupingBy(DataSetProblem::getSetId));

        // 第三步：处理每个setId的数据
        for (String setId : uncachedSetIds) {
            List<DataSetProblem> problems = setProblemsMap.get(setId);
            List<String> problemIds;

            if (ObjectUtil.isEmpty(problems)) {
                // 数据库为空，缓存空值（防止缓存穿透）
                problemIds = List.of();
                String cacheKey = DATACACHE_SET_PROBLEM_IDS + setId;
                redisTemplate.opsForValue().set(cacheKey, problemIds, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            } else {
                // 提取problemId并排序
                problemIds = problems.stream()
                        .map(DataSetProblem::getProblemId)
                        .sorted(Comparator.naturalOrder())
                        .toList();

                // 缓存结果
                String cacheKey = DATACACHE_SET_PROBLEM_IDS + setId;
                redisTemplate.opsForValue().set(cacheKey, problemIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            }

            result.put(setId, problemIds);
        }

        return result;
    }

    @Override
    public boolean addOrUpdate(String setId, List<String> problemIds) {
        // 先清除题集下的所有题目
        this.remove(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
        );

        if (ObjectUtil.isNotEmpty(problemIds)) {
//            problemIds.forEach(item -> {
//                DataSetProblem dataSetProblem = new DataSetProblem();
//                dataSetProblem.setSetId(setId);
//                dataSetProblem.setProblemId(item);
//                dataSetProblem.setSort(problemIds.indexOf(item));
//                // 添加
//                this.save(dataSetProblem);
//            });

            List<DataSetProblem> list = problemIds.stream().map(item -> {
                DataSetProblem dataSetProblem = new DataSetProblem();
                dataSetProblem.setSetId(setId);
                dataSetProblem.setProblemId(item);
                dataSetProblem.setSort(problemIds.indexOf(item));
                return dataSetProblem;
            }).toList();
            clearCache(setId);
            return this.saveBatch(list);
        }
        return true;
    }

    @Override
    public boolean addOrUpdateWithSort(String setId, List<DataSetProblem> sets) {
        // 先清除题集下的所有题目
        this.remove(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
        );
        if (ObjectUtil.isNotEmpty(sets)) {
//            sets.forEach(item -> {
//                item.setSetId(setId);
//                // 添加
//                this.save(item);
//            });
            List<DataSetProblem> list = sets.stream().map(item -> {
                item.setSetId(setId);
                return item;
            }).toList();
            clearCache(setId);
            return this.saveBatch(list);
        }
        return true;
    }

    private void clearCache(String setId) {
        String setProblemIdsKey = DATACACHE_SET_PROBLEM_IDS + setId;
        String setProblemProblemsKey = DATACACHE_SET_PROBLEM_PROBLEMS + setId;
        redisTemplate.delete(setProblemIdsKey);
        redisTemplate.delete(setProblemProblemsKey);
    }
}

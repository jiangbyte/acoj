package io.charlie.web.oj.modular.data.relation.tag.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.web.oj.modular.data.relation.tag.entity.DataProblemTag;
import io.charlie.web.oj.modular.data.relation.tag.mapper.DataProblemTagMapper;
import io.charlie.web.oj.modular.data.relation.tag.service.DataProblemTagService;
import io.charlie.web.oj.modular.sys.tag.entity.SysTag;
import io.charlie.web.oj.modular.sys.tag.mapper.SysTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
@Service
@RequiredArgsConstructor
public class DataProblemTagImpl extends ServiceImpl<DataProblemTagMapper, DataProblemTag> implements DataProblemTagService {
    private final SysTagMapper sysTagMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存key前缀
    private static final String DATACACHE_PROBLEM_TAG_IDS = "datacache:problem:tag:ids:";
    private static final String DATACACHE_PROBLEM_TAG_NAMES = "datacache:problem:tag:names:";
    private static final String DATACACHE_PROBLEM_TAG_PROBLEM_IDS = "datacache:problem:tag:problem:ids:";
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_TIME = 24 * 60 * 60;
    // 空值缓存过期时间（5分钟，防止缓存穿透）
    private static final long NULL_CACHE_EXPIRE_TIME = 5 * 60;

    @Override
    public List<String> getTagIdsById(String problemId) {
        String cacheKey = DATACACHE_PROBLEM_TAG_IDS + problemId;

        // 优先查询缓存
        List<String> cachedTagIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTagIds != null) {
            return cachedTagIds;
        }

        // 缓存不存在，查询数据库
        List<DataProblemTag> dataProblemTags = this.baseMapper.selectList(new LambdaQueryWrapper<DataProblemTag>()
                .eq(DataProblemTag::getProblemId, problemId)
        );

        List<String> tagIds;
        if (ObjectUtil.isNotEmpty(dataProblemTags)) {
            tagIds = dataProblemTags.stream()
                    .map(DataProblemTag::getTagId)
                    .toList();
            // 将结果存入缓存
            redisTemplate.opsForValue().set(cacheKey, tagIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } else {
            // 数据库为空，缓存空值（防止缓存穿透）
            tagIds = List.of();
            redisTemplate.opsForValue().set(cacheKey, tagIds, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        return tagIds;
    }

    @Override
    public List<String> getTagNamesById(String problemId) {
        String cacheKey = DATACACHE_PROBLEM_TAG_NAMES + problemId;

        // 优先查询缓存
        List<String> cachedTagNames = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTagNames != null) {
            return cachedTagNames;
        }

        // 缓存不存在，查询数据库
        List<DataProblemTag> dataProblemTags = this.baseMapper.selectList(new LambdaQueryWrapper<DataProblemTag>()
                .eq(DataProblemTag::getProblemId, problemId)
        );

        List<String> tagNames;
        if (ObjectUtil.isNotEmpty(dataProblemTags)) {
            List<String> tagIds = dataProblemTags.stream()
                    .map(DataProblemTag::getTagId)
                    .toList();

            if (ObjectUtil.isNotEmpty(tagIds)) {
                List<SysTag> sysTags = sysTagMapper.selectByIds(tagIds);
                tagNames = sysTags.stream()
                        .map(SysTag::getName)
                        .toList();
            } else {
                tagNames = List.of();
            }

            // 将结果存入缓存
            redisTemplate.opsForValue().set(cacheKey, tagNames, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } else {
            // 数据库为空，缓存空值（防止缓存穿透）
            tagNames = List.of();
            redisTemplate.opsForValue().set(cacheKey, tagNames, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        return tagNames;
    }

    @Override
    public Map<String, List<String>> batchGetTagIdsByIds(List<String> problemIds) {
        if (CollectionUtil.isEmpty(problemIds)) {
            return Collections.emptyMap();
        }

        // 实现批量查询逻辑，例如：
        List<DataProblemTag> tags = this.list(new LambdaQueryWrapper<DataProblemTag>()
                .in(DataProblemTag::getProblemId, problemIds));

        return tags.stream()
                .collect(Collectors.groupingBy(
                        DataProblemTag::getProblemId,
                        Collectors.mapping(DataProblemTag::getTagId, Collectors.toList())
                ));
    }

    @Override
    public boolean addOrUpdate(String problemId, List<String> tagIds) {
        // 先全部清除这些标签
        QueryWrapper<DataProblemTag> queryWrapper = new QueryWrapper<DataProblemTag>().checkSqlInjection();
        queryWrapper.lambda().eq(DataProblemTag::getProblemId, problemId);
        this.remove(queryWrapper);

        // 再增加关联标签
        if (ObjectUtil.isNotEmpty(tagIds)) {
            tagIds.forEach(item -> {
                DataProblemTag dataProblemTag = new DataProblemTag();
                dataProblemTag.setProblemId(problemId);
                dataProblemTag.setTagId(item);
                // 添加
                this.save(dataProblemTag);
            });
        }

        // 清除相关缓存
        clearCache(problemId);

        return true;
    }

    @Override
    public List<String> getProblemIdsByTagId(String tagId) {
        String cacheKey = DATACACHE_PROBLEM_TAG_PROBLEM_IDS + tagId;
        // 优先查询缓存
        List<String> cachedProblemIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProblemIds != null) {
            return cachedProblemIds;
        }
        // 缓存不存在，查询数据库
        List<DataProblemTag> dataProblemTags = this.baseMapper.selectList(new LambdaQueryWrapper<DataProblemTag>()
                .eq(DataProblemTag::getTagId, tagId)
        );
        List<String> problemIds;
        if (ObjectUtil.isNotEmpty(dataProblemTags)) {
            problemIds = dataProblemTags.stream()
                    .map(DataProblemTag::getProblemId)
                    .toList();
            // 将结果存入缓存
            redisTemplate.opsForValue().set(cacheKey, problemIds, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            return problemIds;
        } else {
            // 缓存空值（防止缓存穿透）
            problemIds = List.of();
            redisTemplate.opsForValue().set(cacheKey, problemIds, NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }
        return problemIds;
    }

    @Override
    public Map<String, List<String>> batchGetTagNamesByIds(List<String> problemIds) {
        if (CollectionUtil.isEmpty(problemIds)) {
            return Collections.emptyMap();
        }
        // 实现批量查询逻辑，例如：
        List<DataProblemTag> tags = this.list(new LambdaQueryWrapper<DataProblemTag>()
                .in(DataProblemTag::getProblemId, problemIds));

        // 批量查询标签名称
        List<String> tagIds = tags.stream()
                .map(DataProblemTag::getTagId)
                .toList();

        if (ObjectUtil.isEmpty(tagIds)) {
            return Collections.emptyMap();
        }

        List<SysTag> sysTags = sysTagMapper.selectByIds(tagIds);
        return tags.stream()
                .collect(Collectors.groupingBy(
                        DataProblemTag::getProblemId,
                        Collectors.mapping(item -> sysTags.stream()
                                        .filter(tag -> tag.getId().equals(item.getTagId()))
                                        .findFirst()
                                        .map(SysTag::getName)
                                        .orElse(null),
                                Collectors.toList())
                ));

    }

    private void clearCache(String problemId) {
        String tagIdsKey = DATACACHE_PROBLEM_TAG_IDS + problemId;
        String tagNamesKey = DATACACHE_PROBLEM_TAG_NAMES + problemId;
        String problemTagIdsKey = DATACACHE_PROBLEM_TAG_PROBLEM_IDS + problemId;

        redisTemplate.delete(tagIdsKey);
        redisTemplate.delete(tagNamesKey);
        redisTemplate.delete(problemTagIdsKey);
    }
}

package io.charlie.web.oj.modular.data.relation.tag.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
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
import java.util.stream.Stream;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
@Service
@RequiredArgsConstructor
public class DataProblemTagImpl extends ServiceImpl<DataProblemTagMapper, DataProblemTag> implements DataProblemTagService {
    private final SysTagMapper sysTagMapper;


    @Override
    @DS("slave")
    public List<String> getTagIdsById(String problemId) {
        // 缓存不存在，查询数据库
        List<DataProblemTag> dataProblemTags = this.baseMapper.selectList(new LambdaQueryWrapper<DataProblemTag>()
                .eq(DataProblemTag::getProblemId, problemId)
        );

        List<String> tagIds;
        if (ObjectUtil.isNotEmpty(dataProblemTags)) {
            tagIds = dataProblemTags.stream()
                    .map(DataProblemTag::getTagId)
                    .toList();
        } else {
            tagIds = List.of();
        }

        return tagIds;
    }

    @Override
    @DS("slave")
    public List<String> getTagNamesById(String problemId) {
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

        } else {
            tagNames = List.of();
        }

        return tagNames;
    }

    @Override
    @DS("slave")
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
//            tagIds.forEach(item -> {
//                DataProblemTag dataProblemTag = new DataProblemTag();
//                dataProblemTag.setProblemId(problemId);
//                dataProblemTag.setTagId(item);
//                // 添加
//                this.save(dataProblemTag);
//            });

            List<DataProblemTag> list = tagIds.stream().map(item -> {
                DataProblemTag dataProblemTag = new DataProblemTag();
                dataProblemTag.setProblemId(problemId);
                dataProblemTag.setTagId(item);
                return dataProblemTag;
            }).toList();

            // 清除相关缓存
            return this.saveBatch(list);
        }
        return true;
    }

    @Override
    @DS("slave")
    public List<String> getProblemIdsByTagId(String tagId) {
        // 缓存不存在，查询数据库
        List<DataProblemTag> dataProblemTags = this.baseMapper.selectList(new LambdaQueryWrapper<DataProblemTag>()
                .eq(DataProblemTag::getTagId, tagId)
        );
        List<String> problemIds;
        if (ObjectUtil.isNotEmpty(dataProblemTags)) {
            problemIds = dataProblemTags.stream()
                    .map(DataProblemTag::getProblemId)
                    .toList();
            return problemIds;
        } else {
            problemIds = List.of();
        }
        return problemIds;
    }

    @Override
    @DS("slave")
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

}

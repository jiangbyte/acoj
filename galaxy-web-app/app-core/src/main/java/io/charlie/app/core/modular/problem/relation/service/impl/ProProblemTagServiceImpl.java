package io.charlie.app.core.modular.problem.relation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.charlie.app.core.modular.problem.problem.entity.ProProblem;
import io.charlie.app.core.modular.problem.relation.entity.ProProblemTag;
import io.charlie.app.core.modular.problem.relation.mapper.ProProblemTagMapper;
import io.charlie.app.core.modular.problem.relation.service.ProProblemTagService;
import io.charlie.app.core.modular.tag.entity.ProTag;
import io.charlie.app.core.modular.tag.mapper.ProTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 27/07/2025
 * @description 题目-标签 关联表(1-N) 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProProblemTagServiceImpl extends ServiceImpl<ProProblemTagMapper, ProProblemTag> implements ProProblemTagService {
    private final ProTagMapper proTagMapper;

    @Override
    public List<ProTag> getTagsById(String problemId) {
        if (ObjectUtil.isEmpty(problemId)) {
            return List.of();
        }
        // 先查询这个题目的关联标签ID列表
        QueryWrapper<ProProblemTag> queryWrapper = new QueryWrapper<ProProblemTag>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblemTag::getProblemId, problemId);
        List<ProProblemTag> list = this.list(queryWrapper);
        if (ObjectUtil.isEmpty(list)) {
            return List.of();
        }
        // 过滤出单独的ID
        List<String> list1 = list.stream().map(ProProblemTag::getTagId).distinct().toList();
        // 查询标签
        QueryWrapper<ProTag> queryWrapper1 = new QueryWrapper<ProTag>().checkSqlInjection();
        queryWrapper1.lambda().in(ProTag::getId, list1);
        return proTagMapper.selectList(queryWrapper1);
    }

    @Override
    public List<String> getTagIdsById(String problemId) {
        if (ObjectUtil.isEmpty(problemId)) {
            return List.of();
        }
        // 先查询这个题目的关联标签ID列表
        QueryWrapper<ProProblemTag> queryWrapper = new QueryWrapper<ProProblemTag>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblemTag::getProblemId, problemId);
        List<ProProblemTag> list = this.list(queryWrapper);
        if (ObjectUtil.isEmpty(list)) {
            return List.of();
        }
        // 过滤出单独的ID
        return list.stream().map(ProProblemTag::getTagId).distinct().toList();
    }

    @Override
    public List<String> getIdsByTagId(String tagId) {
        // 先查询这个标签关联的题目ID列表
        QueryWrapper<ProProblemTag> queryWrapper = new QueryWrapper<ProProblemTag>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblemTag::getTagId, tagId);
        return this.list(queryWrapper).stream().map(ProProblemTag::getProblemId).distinct().toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProblemTags(String problemId, List<String> tagIds) {
        // 先全部清除这些标签
        QueryWrapper<ProProblemTag> queryWrapper = new QueryWrapper<ProProblemTag>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblemTag::getProblemId, problemId);
        this.remove(queryWrapper);

        if (ObjectUtil.isNotEmpty(tagIds)) {
            // 非空再增加关联标签
            tagIds.forEach(item -> {
                ProProblemTag proProblemTag = new ProProblemTag();
                proProblemTag.setProblemId(problemId);
                proProblemTag.setTagId(item);
                // 保存
                this.save(proProblemTag);
            });
        }
    }

    @Override
    public void deleteByTagId(String tagId) {
        QueryWrapper<ProProblemTag> queryWrapper = new QueryWrapper<ProProblemTag>().checkSqlInjection();
        queryWrapper.lambda().eq(ProProblemTag::getTagId, tagId);
        this.remove(queryWrapper);
    }
}

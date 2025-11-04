package io.charlie.web.oj.modular.data.relation.set.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
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


    @Override
    @DS("slave")
    public List<DataProblem> getProblemsBySetId(String setId) {

        // 缓存不存在，查询数据库
        List<DataSetProblem> dataSetProblems = this.baseMapper.selectList(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
                .orderByAsc(DataSetProblem::getSort)
        );

        // 得到题目ID
        List<String> problemIds = dataSetProblems.stream()
                .map(DataSetProblem::getProblemId)
                .toList();

        if (ObjectUtil.isEmpty(problemIds)) {
            return new ArrayList<>();
        }

        return dataProblemMapper.selectByIds(problemIds);
    }

    @Override
    @DS("slave")
    public List<String> getProblemIdsBySetId(String setId) {
        // 获取数据集下的题目
        List<DataSetProblem> dataSetProblems = this.baseMapper.selectList(new LambdaQueryWrapper<DataSetProblem>()
                .eq(DataSetProblem::getSetId, setId)
                .orderByAsc(DataSetProblem::getSort)
        );
        if (ObjectUtil.isNotEmpty(dataSetProblems)) {
            return dataSetProblems.stream()
                    .map(DataSetProblem::getProblemId)
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
        return new ArrayList<>();
    }

    @Override
    @DS("slave")
    public Map<String, List<String>> getProblemIdsBySetIds(List<String> setIds) {
        if (ObjectUtil.isEmpty(setIds)) {
            return Map.of();
        }

        Map<String, List<String>> result = new HashMap<>();

        List<DataSetProblem> dataSetProblems = this.baseMapper.selectList(new LambdaQueryWrapper<DataSetProblem>()
                .in(DataSetProblem::getSetId, setIds)
                .orderByAsc(DataSetProblem::getSetId)
                .orderByAsc(DataSetProblem::getSort)
        );

        if (ObjectUtil.isNotEmpty(dataSetProblems)) {
            // 按setId分组
            Map<String, List<DataSetProblem>> setProblemsMap = dataSetProblems.stream()
                    .collect(Collectors.groupingBy(DataSetProblem::getSetId));

            // 第三步：处理每个setId的数据
            for (String setId : setIds) {
                List<DataSetProblem> problems = setProblemsMap.get(setId);
                if (ObjectUtil.isNotEmpty(problems)) {
                    result.put(setId, problems.stream()
                            .map(DataSetProblem::getProblemId)
                            .sorted(Comparator.naturalOrder())
                            .toList());
                }
            }
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
            List<DataSetProblem> list = problemIds.stream().map(item -> {
                DataSetProblem dataSetProblem = new DataSetProblem();
                dataSetProblem.setSetId(setId);
                dataSetProblem.setProblemId(item);
                dataSetProblem.setSort(problemIds.indexOf(item));
                return dataSetProblem;
            }).toList();
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
            return this.saveBatch(list);
        }
        return true;
    }
}

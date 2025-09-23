package io.charlie.web.oj.modular.data.relation.set.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.relation.set.entity.DataSetProblem;
import io.charlie.web.oj.modular.data.relation.tag.entity.DataProblemTag;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
public interface DataSetProblemService extends IService<DataSetProblem> {
    // 得到某个集合下的所有题目
    List<DataProblem> getProblemsBySetId(String setId);

    // 得到某个集合下的所有题目ID
    List<String> getProblemIdsBySetId(String setId);

    // 添加或更新某个集合下的题目关联关系
    boolean addOrUpdate(String setId, List<String> problemIds);

    // 添加或更新某个集合下的题目关联关系
    boolean addOrUpdateWithSort(String setId, List<DataSetProblem> sets);
}

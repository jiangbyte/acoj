package io.charlie.web.oj.modular.data.relation.tag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.relation.tag.entity.DataProblemTag;

import java.util.List;
import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 21/09/2025
 */
public interface DataProblemTagService extends IService<DataProblemTag> {
    // 得到某个题目的标签ID
    List<String> getTagIdsById(String problemId);

    // 得到某个题目的标签名称
    List<String> getTagNamesById(String problemId);

    Map<String, List<String>> batchGetTagIdsByIds(List<String> problemIds);

    // 添加或更新某个题目的标签
    boolean addOrUpdate(String problemId, List<String> tagIds);

    // 根据标签ID得到题目ID列表
    List<String> getProblemIdsByTagId(String tagId);

    Map<String, List<String>> batchGetTagNamesByIds(List<String> problemIds);
}

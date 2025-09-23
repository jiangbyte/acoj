package io.charlie.web.oj.modular.data.relation.tag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.relation.tag.entity.DataProblemTag;

import java.util.List;

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

    // 添加或更新某个题目的标签
    boolean addOrUpdate(String problemId, List<String> tagIds);

}

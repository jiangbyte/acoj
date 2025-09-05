package io.charlie.app.core.modular.problem.relation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.relation.entity.ProProblemTag;
import io.charlie.app.core.modular.sys.tag.entity.SysTag;

import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 27/07/2025
 * @description 关系服务
 */
public interface ProProblemTagService extends IService<ProProblemTag> {
    // 获取某个题目的标签
    List<SysTag> getTagsById(String problemId);

    List<String> getTagIdsById(String problemId);

    List<String> getIdsByTagId(String tagId);

    // 为某个题目更新标签
    void updateProblemTags(String problemId, List<String> tagIds);

    // 删除标签的时候删除关联
    void deleteByTagId(String tagId);
}

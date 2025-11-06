package io.charlie.web.oj.modular.data.contest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.contest.entity.DataContest;
import io.charlie.web.oj.modular.data.contest.param.DataContestAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestIdParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛表 服务类
*/
public interface DataContestService extends IService<DataContest> {
    Page<DataContest> page(DataContestPageParam dataContestPageParam);

    void add(DataContestAddParam dataContestAddParam);

    void edit(DataContestEditParam dataContestEditParam);

    void delete(List<DataContestIdParam> dataContestIdParamList);

    DataContest detail(DataContestIdParam dataContestIdParam);

    List<DataContest> getHotN(int n);
}
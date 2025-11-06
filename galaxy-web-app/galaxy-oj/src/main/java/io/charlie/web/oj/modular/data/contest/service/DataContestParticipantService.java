package io.charlie.web.oj.modular.data.contest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.contest.entity.DataContestParticipant;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantAddParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantEditParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantIdParam;
import io.charlie.web.oj.modular.data.contest.param.DataContestParticipantPageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛参与表 服务类
*/
public interface DataContestParticipantService extends IService<DataContestParticipant> {
    Page<DataContestParticipant> page(DataContestParticipantPageParam dataContestParticipantPageParam);

    void add(DataContestParticipantAddParam dataContestParticipantAddParam);

    void edit(DataContestParticipantEditParam dataContestParticipantEditParam);

    void delete(List<DataContestParticipantIdParam> dataContestParticipantIdParamList);

    DataContestParticipant detail(DataContestParticipantIdParam dataContestParticipantIdParam);

}
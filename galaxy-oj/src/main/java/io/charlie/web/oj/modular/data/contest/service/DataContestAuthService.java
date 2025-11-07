package io.charlie.web.oj.modular.data.contest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.contest.entity.DataContestAuth;
import io.charlie.web.oj.modular.data.contest.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-11-06
* @description 竞赛认证表 服务类
*/
public interface DataContestAuthService extends IService<DataContestAuth> {
    Page<DataContestAuth> page(DataContestAuthPageParam dataContestAuthPageParam);

    void add(DataContestAuthAddParam dataContestAuthAddParam);

    void edit(DataContestAuthEditParam dataContestAuthEditParam);

    void delete(List<DataContestAuthIdParam> dataContestAuthIdParamList);

    DataContestAuth detail(DataContestAuthIdParam dataContestAuthIdParam);

    Boolean auth(DataContestAuthParam dataContestAuthParam);
}
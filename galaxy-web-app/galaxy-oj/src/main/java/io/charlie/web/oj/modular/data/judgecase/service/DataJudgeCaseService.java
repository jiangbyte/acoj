package io.charlie.web.oj.modular.data.judgecase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.judgecase.entity.DataJudgeCase;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseAddParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseEditParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCaseIdParam;
import io.charlie.web.oj.modular.data.judgecase.param.DataJudgeCasePageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 判题结果用例表 服务类
*/
public interface DataJudgeCaseService extends IService<DataJudgeCase> {
    Page<DataJudgeCase> page(DataJudgeCasePageParam dataJudgeCasePageParam);

    void add(DataJudgeCaseAddParam dataJudgeCaseAddParam);

    void edit(DataJudgeCaseEditParam dataJudgeCaseEditParam);

    void delete(List<DataJudgeCaseIdParam> dataJudgeCaseIdParamList);

    DataJudgeCase detail(DataJudgeCaseIdParam dataJudgeCaseIdParam);

}
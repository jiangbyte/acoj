package io.charlie.web.oj.modular.data.testcase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.testcase.entity.DataTestCase;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseAddParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseEditParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCaseIdParam;
import io.charlie.web.oj.modular.data.testcase.param.DataTestCasePageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-10-26
* @description 题目测试用例表 服务类
*/
public interface DataTestCaseService extends IService<DataTestCase> {
    Page<DataTestCase> page(DataTestCasePageParam dataTestCasePageParam);

    void add(DataTestCaseAddParam dataTestCaseAddParam);

    void edit(DataTestCaseEditParam dataTestCaseEditParam);

    void delete(List<DataTestCaseIdParam> dataTestCaseIdParamList);

    DataTestCase detail(DataTestCaseIdParam dataTestCaseIdParam);

    List<DataTestCase> getTestCaseByProblemId(String problemId);
}
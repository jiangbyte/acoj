package io.charlie.web.oj.modular.data.submit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 提交表 服务类
*/
public interface DataSubmitService extends IService<DataSubmit> {
    Page<DataSubmit> page(DataSubmitPageParam dataSubmitPageParam);

    Page<DataSubmit> problemPage(DataSubmitPageParam dataSubmitPageParam);

    Page<DataSubmit> setPage(DataSubmitPageParam dataSubmitPageParam);

    Page<DataSubmit> modulePage(DataSubmitPageParam dataSubmitPageParam);

    void add(DataSubmitAddParam dataSubmitAddParam);

    void edit(DataSubmitEditParam dataSubmitEditParam);

    void delete(List<DataSubmitIdParam> dataSubmitIdParamList);

    DataSubmit detail(DataSubmitIdParam dataSubmitIdParam);

    String handleProblemSubmit(DataSubmitExeParam dataSubmitExeParam);

    String handleSetSubmit(DataSubmitExeParam dataSubmitExeParam);

    List<StatusCount> countStatusStatistics();
}
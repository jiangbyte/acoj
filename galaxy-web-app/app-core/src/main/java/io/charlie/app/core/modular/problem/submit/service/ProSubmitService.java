package io.charlie.app.core.modular.problem.submit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 提交表 服务类
*/
public interface ProSubmitService extends IService<ProSubmit> {
    Page<ProSubmit> page(ProSubmitPageParam proSubmitPageParam);

    void add(ProSubmitAddParam proSubmitAddParam);

    void edit(ProSubmitEditParam proSubmitEditParam);

    void delete(List<ProSubmitIdParam> proSubmitIdParamList);

    ProSubmit detail(ProSubmitIdParam proSubmitIdParam);

    String execute(ProSubmitExecuteParam proSubmitExecuteParam);

    Page<ProSubmit> problemUserSubmitPage(ProUserSubmitPageParam proUserSubmitPageParam);
}
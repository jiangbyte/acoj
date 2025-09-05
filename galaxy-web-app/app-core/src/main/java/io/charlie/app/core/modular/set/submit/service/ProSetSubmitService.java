package io.charlie.app.core.modular.set.submit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.app.core.modular.problem.submit.entity.ProSubmit;
import io.charlie.app.core.modular.problem.submit.param.ProUserSubmitPageParam;
import io.charlie.app.core.modular.set.submit.entity.ProSetSubmit;
import io.charlie.app.core.modular.set.submit.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-07-25
* @description 题单提交表 服务类
*/
public interface ProSetSubmitService extends IService<ProSetSubmit> {
    Page<ProSetSubmit> page(ProSetSubmitPageParam proSetSubmitPageParam);

    void add(ProSetSubmitAddParam proSetSubmitAddParam);

    void edit(ProSetSubmitEditParam proSetSubmitEditParam);

    void delete(List<ProSetSubmitIdParam> proSetSubmitIdParamList);

    ProSetSubmit detail(ProSetSubmitIdParam proSetSubmitIdParam);

    Page<ProSetSubmit> problemUserSubmitPage(ProSetUserSubmitPageParam proSetUserSubmitPageParam);

    String execute(ProSetSubmitExecuteParam proSetSubmitExecuteParam);

}
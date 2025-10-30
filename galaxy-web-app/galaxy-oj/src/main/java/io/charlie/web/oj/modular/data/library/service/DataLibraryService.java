package io.charlie.web.oj.modular.data.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.*;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.task.judge.dto.JudgeResultDto;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 提交样本库 服务类
 */
public interface DataLibraryService extends IService<DataLibrary> {
    Page<DataLibrary> page(DataLibraryPageParam dataLibraryPageParam);

    void add(DataLibraryAddParam dataLibraryAddParam);

    void edit(DataLibraryEditParam dataLibraryEditParam);

    void delete(List<DataLibraryIdParam> dataLibraryIdParamList);

    DataLibrary detail(DataLibraryIdParam dataLibraryIdParam);

    void addLibrary(DataSubmit submit);

    // 修改原方法，返回处理结果
    <R> List<R> processCodeLibrariesInBatches(boolean isSet, String language,
                                              List<String> problemIds, List<String> setIds,
                                              List<String> userIds, int batchSize,
                                              String filterProblemId, String filterSetId,
                                              String filterUserId,
                                              int maxBatches,
                                              Function<List<DataLibrary>, List<R>> processor);

    Page<SysUser> getLibraryUserPage(DataLibraryUserPageParam dataLibraryUserPageParam);
}
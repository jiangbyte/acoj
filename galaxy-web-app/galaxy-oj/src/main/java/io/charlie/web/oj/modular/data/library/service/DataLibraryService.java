package io.charlie.web.oj.modular.data.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.param.*;
import io.charlie.web.oj.modular.data.library.entity.LibraryBatchCount;
import io.charlie.web.oj.modular.data.library.param.BatchLibraryQueryParam;
import io.charlie.web.oj.modular.sys.group.entity.SysGroup;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;

import java.util.List;

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

    List<SysGroup> getLibraryUserGroupList(String keyword);

    Page<SysUser> getLibraryUserPage(DataLibraryUserPageParam dataLibraryUserPageParam);

    QueryWrapper<DataLibrary> queryLibrary(BatchLibraryQueryParam libraryQueryParam);

    LibraryBatchCount batchQuery(BatchLibraryQueryParam libraryQueryParam);

    List<String> libraryIds(BatchLibraryQueryParam libraryQueryParam);

}
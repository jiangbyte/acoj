package io.charlie.web.oj.modular.sys.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.web.oj.modular.sys.notice.entity.SysNotice;
import io.charlie.web.oj.modular.sys.notice.param.SysNoticeAddParam;
import io.charlie.web.oj.modular.sys.notice.param.SysNoticeEditParam;
import io.charlie.web.oj.modular.sys.notice.param.SysNoticeIdParam;
import io.charlie.web.oj.modular.sys.notice.param.SysNoticePageParam;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 公告表 服务类
*/
public interface SysNoticeService extends IService<SysNotice> {
    Page<SysNotice> page(SysNoticePageParam sysNoticePageParam);

    void add(SysNoticeAddParam sysNoticeAddParam);

    void edit(SysNoticeEditParam sysNoticeEditParam);

    void delete(List<SysNoticeIdParam> sysNoticeIdParamList);

    SysNotice detail(SysNoticeIdParam sysNoticeIdParam);

    List<SysNotice> latestN(int n);

    List<SysNotice> lists(int n);

}
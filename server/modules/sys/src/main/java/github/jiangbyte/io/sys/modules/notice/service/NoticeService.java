package github.jiangbyte.io.sys.modules.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.sys.modules.notice.entity.SysNotice;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeAddParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeEditParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticePageParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticePinParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeReadParam;

/**
 * 公告/通知领域服务：管理端 CRUD 与发布流转，以及用户侧可见列表、详情、未读与已读。
 *
 * Author: Charlie
 */
public interface NoticeService extends IService<SysNotice> {

    /**
     * 创建公告或通知（校验类型/目标范围并落库）。
     */
    void create(SysNoticeAddParam param);

    /**
     * 更新公告或通知内容与状态。
     */
    void update(SysNoticeEditParam param);

    /**
     * 批量删除消息及其已读记录。
     */
    void delete(IdsParam param);

    /**
     * 管理端按 ID 查询消息详情。
     */
    SysNotice detail(String id);

    /**
     * 管理端按标题/状态/类型分页查询。
     */
    Page<SysNotice> page(SysNoticePageParam param);

    /**
     * 批量发布消息并记录发布人与发布时间。
     */
    void publish(IdsParam param);

    /**
     * 批量撤回已发布消息。
     */
    void revoke(IdsParam param);

    /**
     * 设置公告置顶状态与置顶截止时间。
     */
    void pin(SysNoticePinParam param);

    /**
     * 门户公告列表（已发布且对当前用户可见）。
     */
    Page<SysNotice> portalList(SysNoticePageParam param);

    /**
     * 当前用户可见的已发布消息分页（含已读标记）。
     */
    Page<SysNotice> myPage(SysNoticePageParam param);

    /**
     * 查看本人可见消息详情；公告累加浏览并标记已读。
     */
    SysNotice myDetail(String id);

    /**
     * 统计当前用户未读消息数量。
     */
    int unreadCount();

    /**
     * 将指定消息标记为已读。
     */
    void markRead(SysNoticeReadParam param);

    /**
     * 将当前用户可见的全部已发布消息标记为已读。
     */
    void markAllRead();
}

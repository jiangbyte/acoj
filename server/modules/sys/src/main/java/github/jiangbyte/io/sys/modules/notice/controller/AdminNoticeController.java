package github.jiangbyte.io.sys.modules.notice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.notice.entity.SysNotice;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeAddParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeEditParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticePageParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticePinParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeReadParam;
import github.jiangbyte.io.sys.modules.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端公告/通知 API：创建编辑发布撤回置顶，以及当前用户消息列表、详情、未读与已读标记。
 *
 * Author: Charlie
 */
@Tag(name = "管理端公告/通知 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    /** 创建公告或通知。 */
    @Operation(summary = "创建公告或通知。")
    @PostMapping("/v1/admin/sys/notices/create")
    @SaCheckPermission(value = "sys:notice:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_notice", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysNoticeAddParam param) {
        noticeService.create(param);
        return ApiResponse.ok();
    }

    /** 更新公告或通知。 */
    @Operation(summary = "更新公告或通知。")
    @PostMapping("/v1/admin/sys/notices/update")
    @SaCheckPermission(value = "sys:notice:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_notice", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysNoticeEditParam param) {
        noticeService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除公告/通知。 */
    @Operation(summary = "批量删除公告/通知。")
    @PostMapping("/v1/admin/sys/notices/delete")
    @SaCheckPermission(value = "sys:notice:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_notice", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        noticeService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询消息详情（管理端）。 */
    @Operation(summary = "查询消息详情（管理端）。")
    @GetMapping("/v1/admin/sys/notices/detail")
    @SaCheckPermission(value = "sys:notice:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysNotice> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(noticeService.detail(param.getId()));
    }

    /** 分页查询消息（管理端）。 */
    @Operation(summary = "分页查询消息（管理端）。")
    @GetMapping("/v1/admin/sys/notices/page")
    @SaCheckPermission(value = "sys:notice:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysNotice>> page(@Valid @ModelAttribute SysNoticePageParam param) {
        return ApiResponse.ok(noticeService.page(param));
    }

    /** 批量发布消息。 */
    @Operation(summary = "批量发布消息。")
    @PostMapping("/v1/admin/sys/notices/publish")
    @SaCheckPermission(value = "sys:notice:publish", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_notice", action = "publish")
    public ApiResponse<Void> publish(@Valid @RequestBody IdsParam param) {
        noticeService.publish(param);
        return ApiResponse.ok();
    }

    /** 批量撤回消息。 */
    @Operation(summary = "批量撤回消息。")
    @PostMapping("/v1/admin/sys/notices/revoke")
    @SaCheckPermission(value = "sys:notice:revoke", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_notice", action = "revoke")
    public ApiResponse<Void> revoke(@Valid @RequestBody IdsParam param) {
        noticeService.revoke(param);
        return ApiResponse.ok();
    }

    /** 设置公告置顶。 */
    @Operation(summary = "设置公告置顶。")
    @PostMapping("/v1/admin/sys/notices/pin")
    @SaCheckPermission(value = "sys:notice:pin", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_notice", action = "pin")
    public ApiResponse<Void> pin(@Valid @RequestBody SysNoticePinParam param) {
        noticeService.pin(param);
        return ApiResponse.ok();
    }

    /** 当前用户可见消息分页。 */
    @Operation(summary = "当前用户可见消息分页。")
    @GetMapping("/v1/admin/sys/notices/my-page")
    public ApiResponse<Page<SysNotice>> myPage(@Valid @ModelAttribute SysNoticePageParam param) {
        return ApiResponse.ok(noticeService.myPage(param));
    }

    /** 当前用户可见消息详情。 */
    @Operation(summary = "当前用户可见消息详情。")
    @GetMapping("/v1/admin/sys/notices/my-detail")
    public ApiResponse<SysNotice> myDetail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(noticeService.myDetail(param.getId()));
    }

    /** 当前用户未读消息数。 */
    @Operation(summary = "当前用户未读消息数。")
    @GetMapping("/v1/admin/sys/notices/unread-count")
    public ApiResponse<Integer> unreadCount() {
        return ApiResponse.ok(noticeService.unreadCount());
    }

    /** 标记指定消息为已读。 */
    @Operation(summary = "标记指定消息为已读。")
    @PostMapping("/v1/admin/sys/notices/read")
    public ApiResponse<Void> read(@Valid @RequestBody SysNoticeReadParam param) {
        noticeService.markRead(param);
        return ApiResponse.ok();
    }

    /** 将全部可见消息标记为已读。 */
    @Operation(summary = "将全部可见消息标记为已读。")
    @PostMapping("/v1/admin/sys/notices/read-all")
    public ApiResponse<Void> readAll() {
        noticeService.markAllRead();
        return ApiResponse.ok();
    }
}
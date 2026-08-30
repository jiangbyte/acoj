package github.jiangbyte.io.sys.modules.config.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.sys.modules.config.param.SysConfigAddParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigBatchSaveParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigEditParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigPageParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigTestWebhookParam;
import github.jiangbyte.io.sys.modules.config.result.SysConfigResult;
import github.jiangbyte.io.sys.modules.config.service.ConfigService;
import github.jiangbyte.io.sys.modules.config.support.AuditAlertTestSender;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端系统配置 API：CRUD、批量保存与 Webhook 测试。
 *
 * Author: Charlie
 */
@Tag(name = "管理端系统配置 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminConfigController {

    private final ConfigService configService;
    private final AuditAlertTestSender auditAlertTestSender;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/sys/config/create")
    @SaCheckPermission(value = "sys:config:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_config", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody SysConfigAddParam param) {
        configService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/sys/config/update")
    @SaCheckPermission(value = "sys:config:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_config", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody SysConfigEditParam param) {
        configService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/sys/config/delete")
    @SaCheckPermission(value = "sys:config:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_config", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        configService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/sys/config/detail")
    @SaCheckPermission(value = "sys:config:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SysConfigResult> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(configService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/sys/config/page")
    @SaCheckPermission(value = "sys:config:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SysConfigResult>> page(@Valid @ModelAttribute SysConfigPageParam param) {
        return ApiResponse.ok(configService.page(param));
    }

    /** 列表查询。 */
    @Operation(summary = "列表查询。")
    @GetMapping("/v1/admin/sys/config/list")
    @SaCheckPermission(value = "sys:config:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SysConfigResult>> list(@RequestParam(required = false) String category) {
        return ApiResponse.ok(configService.list(category));
    }

    /** 批量保存。 */
    @Operation(summary = "批量保存。")
    @PostMapping("/v1/admin/sys/config/batch-save")
    @SaCheckPermission(value = "sys:config:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "sys_config", action = "update")
    public ApiResponse<Void> batchSave(@Valid @RequestBody SysConfigBatchSaveParam param) {
        configService.batchSave(param);
        return ApiResponse.ok();
    }

    /** 测试审计告警 Webhook。 */
    @Operation(summary = "测试审计告警 Webhook。")
    @PostMapping("/v1/admin/sys/config/audit-alert/test-webhook")
    @OperationAudit(resourceType = "sys_config", action = "test_webhook")
    public ApiResponse<Map<String, String>> testAuditAlertWebhook(
            @RequestBody(required = false) SysConfigTestWebhookParam param) {
        SysConfigTestWebhookParam body = param == null ? new SysConfigTestWebhookParam() : param;
        AuditSnapshots.after(Map.of(
                "Webhook地址", body.getWebhookUrl() == null ? "" : body.getWebhookUrl(),
                "密钥", maskSecret(body.getWebhookSecret())));
        auditAlertTestSender.testWebhook(body.getWebhookUrl(), body.getWebhookSecret());
        return ApiResponse.ok(Map.of("message", "测试消息已发送"));
    }

    /** 测试审计告警推送。 */
    @Operation(summary = "测试审计告警推送。")
    @PostMapping("/v1/admin/sys/config/audit-alert/test-push")
    @OperationAudit(resourceType = "sys_config", action = "test_push")
    public ApiResponse<Map<String, String>> testAuditAlertPush() {
        AuditSnapshots.after(Map.of("推送渠道", "审计告警"));
        auditAlertTestSender.testPush();
        return ApiResponse.ok(Map.of("message", "测试消息已发送"));
    }

    private static String maskSecret(String secret) {
        if (!StringUtils.hasText(secret)) {
            return "";
        }
        String value = secret.trim();
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}

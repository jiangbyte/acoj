package github.jiangbyte.io.common.log.audit;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 审计展示文案：模块名、操作名、操作类型与可读操作内容。
 *
 * Author: Charlie
 */
public final class AuditLabelCatalog {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwordhash", "password_hash", "oldpassword", "old_password",
            "newpassword", "new_password", "token", "secret", "accesskey",
            "access_key", "privatekey", "private_key", "cryptokey", "crypto_key",
            "realname", "real_name", "realnamecipher", "real_name_cipher",
            "documentno", "document_no", "documentnocipher", "document_no_cipher",
            "documentnohash", "document_no_hash", "applicantcontact", "applicant_contact",
            "attachmentids", "attachment_ids", "providerorderno", "provider_order_no",
            "providerpayload", "provider_payload", "reviewremark", "review_remark");

    /** 常见字段中文名（展示用；未知字段回退原 key）。 */
    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("id", "编号"),
            Map.entry("name", "名称"),
            Map.entry("code", "编码"),
            Map.entry("title", "标题"),
            Map.entry("label", "标签"),
            Map.entry("value", "值"),
            Map.entry("account", "账号"),
            Map.entry("nickname", "昵称"),
            Map.entry("username", "用户名"),
            Map.entry("email", "邮箱"),
            Map.entry("phone", "手机号"),
            Map.entry("status", "状态"),
            Map.entry("sort", "排序"),
            Map.entry("remark", "备注"),
            Map.entry("description", "描述"),
            Map.entry("category", "分类"),
            Map.entry("type", "类型"),
            Map.entry("scopeType", "范围类型"),
            Map.entry("scope_type", "范围类型"),
            Map.entry("dataScope", "数据范围"),
            Map.entry("data_scope", "数据范围"),
            Map.entry("ownerDeptId", "所属部门"),
            Map.entry("owner_dept_id", "所属部门"),
            Map.entry("parentId", "上级"),
            Map.entry("parent_id", "上级"),
            Map.entry("content", "内容"),
            Map.entry("summary", "摘要"),
            Map.entry("enabled", "启用"),
            Map.entry("pinned", "置顶"),
            Map.entry("publishStatus", "发布状态"),
            Map.entry("publish_status", "发布状态"),
            Map.entry("avatar", "头像"),
            Map.entry("avatarUrl", "头像"),
            Map.entry("avatar_url", "头像"),
            Map.entry("originalName", "文件名"),
            Map.entry("original_name", "文件名"),
            Map.entry("fileName", "文件名"),
            Map.entry("cron", "Cron"),
            Map.entry("cronExpression", "Cron"),
            Map.entry("handlerName", "处理器"),
            Map.entry("handler_name", "处理器"),
            Map.entry("roleIds", "角色"),
            Map.entry("role_ids", "角色"),
            Map.entry("deptIds", "部门"),
            Map.entry("dept_ids", "部门"),
            Map.entry("groupIds", "用户组"),
            Map.entry("group_ids", "用户组"),
            Map.entry("positionIds", "岗位"),
            Map.entry("position_ids", "岗位"),
            Map.entry("accountIds", "账号"),
            Map.entry("account_ids", "账号"),
            Map.entry("grantInfoList", "授权资源"),
            Map.entry("grant_info_list", "授权资源"),
            Map.entry("passwordHash", "密码"),
            Map.entry("password_hash", "密码"),
            Map.entry("businessType", "认证方式"),
            Map.entry("business_type", "认证方式"),
            Map.entry("documentType", "证件类型"),
            Map.entry("document_type", "证件类型"),
            Map.entry("caseId", "工单编号"),
            Map.entry("case_id", "工单编号"),
            Map.entry("reviewRemark", "审核意见"),
            Map.entry("review_remark", "审核意见"),
            Map.entry("providerCode", "认证渠道"),
            Map.entry("provider_code", "认证渠道"));

    private AuditLabelCatalog() {
    }

    public static String moduleLabel(String resourceType) {
        if (!StringUtils.hasText(resourceType)) {
            return "系统";
        }
        String key = resourceType.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "auth", "account" -> "认证 - 账号";
            case "auth_session" -> "认证 - 会话";
            case "iam_account" -> "权限 - 账号";
            case "iam_role" -> "权限 - 角色";
            case "iam_dept" -> "权限 - 部门";
            case "iam_group" -> "权限 - 用户组";
            case "iam_position" -> "权限 - 岗位";
            case "iam_resource", "resources" -> "权限 - 资源";
            case "iam_client_module" -> "权限 - 客户端模块";
            case "iam_client_resource" -> "权限 - 客户端资源";
            case "sys_notice" -> "系统 - 消息";
            case "sys_banner" -> "系统 - 展示图";
            case "sys_file" -> "系统 - 文件";
            case "sys_config" -> "系统 - 配置";
            case "sys_dict" -> "系统 - 字典";
            case "sys_job" -> "系统 - 任务";
            case "sys_feedback" -> "系统 - 反馈";
            case "sys_codegen" -> "系统 - 代码生成";
            case "sys_weakpassword" -> "系统 - 弱密码";
            case "profile_center" -> "个人中心";
            case "real_name_case" -> "实名认证 - 工单";
            case "profile_identity" -> "实名认证 - 身份";
            case "workspace_shortcut" -> "工作台 - 快捷应用";
            default -> {
                if (key.startsWith("biz_")) {
                    yield "业务 - " + key.substring(4);
                }
                if (key.startsWith("sys_")) {
                    yield "系统 - " + key.substring(4);
                }
                if (key.startsWith("iam_")) {
                    yield "权限 - " + key.substring(4);
                }
                yield resourceType;
            }
        };
    }

    public static String entityShortName(String resourceType) {
        String module = moduleLabel(resourceType);
        if (module.contains(" - ")) {
            return module.substring(module.indexOf(" - ") + 3);
        }
        return module;
    }

    public static String actionName(String resourceType, String action, String explicitName) {
        if (StringUtils.hasText(explicitName)) {
            return explicitName.trim();
        }
        String act = normalize(action);
        String shortModule = entityShortName(resourceType);
        return switch (act) {
            case "create" -> "创建" + shortModule;
            case "update" -> "更新" + shortModule;
            case "delete" -> "删除" + shortModule;
            case "login" -> "登录";
            case "logout" -> "退出登录";
            case "register" -> "注册";
            case "refresh" -> "刷新令牌";
            case "upload" -> "上传文件";
            case "publish" -> "发布" + shortModule;
            case "revoke" -> "撤回" + shortModule;
            case "pin" -> "置顶" + shortModule;
            case "read", "read_all", "read-all" -> "阅读" + shortModule;
            case "export" -> "导出" + shortModule;
            case "import" -> "导入" + shortModule;
            case "enabled", "enable" -> "启用" + shortModule;
            case "run" -> "执行" + shortModule;
            case "submit" -> "提交" + shortModule;
            case "approve" -> "审核通过" + shortModule;
            case "reject" -> "审核驳回" + shortModule;
            case "init_third_party", "init-third-party" -> "发起第三方实名";
            case "callback" -> "第三方实名回调";
            case "batch-save", "batch_save" -> "批量保存" + shortModule;
            case "forgot_password", "forgot-password" -> "忘记密码";
            case "reset_password", "reset-password" -> "重置密码";
            case "update_password", "update-password" -> "修改密码";
            case "update_profile", "update-profile" -> "更新资料";
            case "upload_avatar", "upload-avatar" -> "上传头像";
            case "update_phone", "update-phone" -> "绑定手机号";
            case "update_email", "update-email" -> "绑定邮箱";
            case "cancel" -> "注销账号";
            case "interaction" -> "互动";
            case "oauth_wechat_mp_login" -> "微信小程序登录";
            case "oauth_bind_authorize" -> "三方账号绑定";
            case "oauth_unbind" -> "解绑三方账号";
            case "test_webhook" -> "测试审计 Webhook";
            case "test_push" -> "测试审计推送";
            case "grant", "grant_resources", "grant-resources", "grant_users", "grant-users",
                    "grant_roles", "grant-roles", "grant_groups", "grant-groups",
                    "grant_depts", "grant-depts", "grant_client_resources", "grant-client-resources",
                    "grant_resource", "grant_user", "grant_client_resource" -> "授权" + shortModule;
            case "exit", "token_exit", "token-exit" -> "强制下线";
            default -> StringUtils.hasText(action) ? action : "操作";
        };
    }

    public static String actionType(String action, String explicitType) {
        if (StringUtils.hasText(explicitType)) {
            return explicitType.trim().toUpperCase(Locale.ROOT);
        }
        String act = normalize(action);
        return switch (act) {
            case "create", "register", "submit" -> "CREATE";
            case "update", "update_password", "update-password", "update_profile", "update-profile",
                    "update_phone", "update-phone", "update_email", "update-email",
                    "batch-save", "batch_save", "pin", "publish", "revoke", "enabled", "enable",
                    "approve", "reject",
                    "grant", "grant_resources", "grant-resources", "grant_users", "grant-users",
                    "grant_roles", "grant-roles", "grant_groups", "grant-groups",
                    "grant_depts", "grant-depts", "grant_client_resources", "grant-client-resources",
                    "grant_resource", "grant_user", "grant_client_resource" -> "UPDATE";
            case "delete", "cancel" -> "DELETE";
            case "login", "oauth_wechat_mp_login" -> "LOGIN";
            case "logout" -> "LOGOUT";
            case "export" -> "EXPORT";
            case "read", "read_all", "read-all", "refresh", "page", "detail", "list" -> "QUERY";
            case "upload", "upload_avatar", "upload-avatar", "import", "run", "interaction",
                    "forgot_password", "forgot-password", "reset_password", "reset-password",
                    "send_login_code", "send-login-code", "exit", "token_exit", "token-exit",
                    "oauth_bind_authorize", "oauth_unbind", "test_webhook", "test_push",
                    "init_third_party", "init-third-party", "callback" -> "OTHER";
            default -> "OTHER";
        };
    }

    /**
     * 参考业务审计风格的可读操作内容（不拼接 HTTP 路径）。
     */
    public static String buildContent(
            String action,
            String resourceType,
            String actionName,
            String subject,
            boolean success,
            Map<String, Object> beforeData,
            Map<String, Object> afterData) {
        String act = normalize(action);
        String entity = entityShortName(resourceType);
        String subjectPart = StringUtils.hasText(subject) ? " 【" + subject.trim() + "】" : "";
        String result = success ? "成功" : "失败";
        String resourceKey = resourceType == null ? "" : resourceType.trim().toLowerCase(Locale.ROOT);

        String identityContent = buildIdentityContent(
                act, resourceKey, subjectPart, success, beforeData, afterData);
        if (StringUtils.hasText(identityContent)) {
            return identityContent;
        }

        if ("login".equals(act)) {
            return "账号" + subjectPart + "登录" + result;
        }
        if ("logout".equals(act)) {
            return "账号" + subjectPart + "退出" + result;
        }
        if ("register".equals(act)) {
            return "账号" + subjectPart + "注册" + result;
        }
        if ("oauth_wechat_mp_login".equals(act)) {
            return "账号" + subjectPart + "通过微信小程序登录" + result;
        }
        if ("oauth_bind_authorize".equals(act)) {
            String diff = formatDiff(beforeData, afterData);
            if (StringUtils.hasText(diff)) {
                return "发起三方账号绑定" + subjectPart + "：" + diff;
            }
            return "发起三方账号绑定" + subjectPart + result;
        }
        if ("reset_password".equals(act) || "reset-password".equals(act)) {
            String pwdDiff = passwordResetDiff(beforeData, afterData);
            if (StringUtils.hasText(pwdDiff)) {
                return "将" + entity + subjectPart + "的" + pwdDiff;
            }
            return "重置了" + entity + subjectPart + "的密码";
        }
        if ("update_password".equals(act) || "update-password".equals(act)) {
            return "修改了" + entity + subjectPart + "的密码";
        }
        if ("delete".equals(act) || "cancel".equals(act)) {
            return "删除了" + entity + subjectPart;
        }

        String verb = switch (act) {
            case "create", "submit" -> "创建了";
            case "update", "update_profile", "update-profile", "update_phone", "update-phone",
                    "update_email", "update-email", "batch-save", "batch_save", "enabled", "enable",
                    "pin", "publish", "revoke", "approve", "reject" -> "更新了";
            case "upload", "upload_avatar", "upload-avatar" -> "上传了";
            case "run" -> "执行了";
            case "read", "read_all", "read-all" -> "阅读了";
            case "interaction" -> "互动了";
            case "test_webhook", "test_push" -> "测试了";
            case "grant", "grant_resources", "grant-resources", "grant_users", "grant-users",
                    "grant_roles", "grant-roles", "grant_groups", "grant-groups",
                    "grant_depts", "grant-depts", "grant_client_resources", "grant-client-resources",
                    "grant_resource", "grant_user", "grant_client_resource" -> "授权了";
            case "exit", "token_exit", "token-exit" -> "强制下线了";
            default -> null;
        };

        String diff = formatDiff(beforeData, afterData);
        if (verb != null) {
            if (StringUtils.hasText(diff)) {
                return verb + entity + subjectPart + "：" + diff;
            }
            String name = StringUtils.hasText(actionName) ? actionName : verb + entity;
            return name + subjectPart + (success ? "" : "失败");
        }

        String name = StringUtils.hasText(actionName) ? actionName : "操作";
        if (StringUtils.hasText(diff)) {
            return "【" + name + "】" + result + "：" + diff;
        }
        return "【" + name + "】" + subjectPart + result;
    }

    /** @deprecated 保留兼容，内部转叙事格式且忽略 path。 */
    public static String buildContent(
            String actionName,
            boolean success,
            String path,
            Map<String, Object> beforeData,
            Map<String, Object> afterData) {
        return buildContent(null, null, actionName, null, success, beforeData, afterData);
    }

    public static String formatDiff(Map<String, Object> beforeData, Map<String, Object> afterData) {
        if ((beforeData == null || beforeData.isEmpty()) && (afterData == null || afterData.isEmpty())) {
            return null;
        }
        Map<String, Object> before = beforeData == null ? Map.of() : beforeData;
        Map<String, Object> after = afterData == null ? Map.of() : afterData;
        Map<String, Object> keys = new LinkedHashMap<>();
        before.forEach(keys::putIfAbsent);
        after.forEach(keys::putIfAbsent);
        StringBuilder sb = new StringBuilder();
        for (String key : keys.keySet()) {
            if (shouldSkipField(key)) {
                continue;
            }
            Object oldVal = before.get(key);
            Object newVal = after.get(key);
            if (equalsLoose(oldVal, newVal)) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append("；");
            }
            // 集合类变更：删除了【A，B】 / 添加了【C】
            if (oldVal instanceof Iterable<?> || newVal instanceof Iterable<?>) {
                sb.append("【").append(fieldLabel(key)).append("】")
                        .append(collectionChangeText(oldVal, newVal));
            } else {
                sb.append("【").append(fieldLabel(key)).append("】从【")
                        .append(displayValue(oldVal))
                        .append("】修改为【")
                        .append(displayValue(newVal))
                        .append("】");
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    public static String fieldLabel(String key) {
        if (!StringUtils.hasText(key)) {
            return "字段";
        }
        String direct = FIELD_LABELS.get(key);
        if (direct != null) {
            return direct;
        }
        String snake = key.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        String fromSnake = FIELD_LABELS.get(snake);
        if (fromSnake != null) {
            return fromSnake;
        }
        return key;
    }

    private static String passwordResetDiff(Map<String, Object> beforeData, Map<String, Object> afterData) {
        Map<String, Object> before = beforeData == null ? Map.of() : beforeData;
        Map<String, Object> after = afterData == null ? Map.of() : afterData;
        Object oldPwd = firstPresent(before, "passwordHash", "password_hash", "password");
        Object newPwd = firstPresent(after, "passwordHash", "password_hash", "password");
        if (oldPwd == null && newPwd == null) {
            return null;
        }
        return "密码从【" + displayValue(oldPwd) + "】重置为【" + displayValue(newPwd) + "】";
    }

    private static Object firstPresent(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private static String collectionChangeText(Object oldVal, Object newVal) {
        String oldText = displayValue(oldVal);
        String newText = displayValue(newVal);
        if ("空".equals(oldText) && !"空".equals(newText)) {
            return "添加了【" + newText + "】";
        }
        if (!"空".equals(oldText) && "空".equals(newText)) {
            return "删除了【" + oldText + "】";
        }
        return "从【" + oldText + "】修改为【" + newText + "】";
    }

    private static boolean shouldSkipField(String key) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        String normalized = key.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
        if ("id".equals(normalized) || "createdat".equals(normalized) || "updatedat".equals(normalized)
                || "createdby".equals(normalized) || "updatedby".equals(normalized)) {
            return true;
        }
        return isSensitive(key);
    }

    private static boolean isSensitive(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String normalized = key.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
        // 密码重置场景需要展示 hash 变更，由专用文案处理；普通 diff 仍脱敏
        for (String sensitive : SENSITIVE_KEYS) {
            if (normalized.contains(sensitive.replace("_", ""))) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalsLoose(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof Iterable<?> || b instanceof Iterable<?>) {
            return displayValue(a).equals(displayValue(b));
        }
        return String.valueOf(a).equals(String.valueOf(b));
    }

    private static String displayValue(Object value) {
        if (value == null) {
            return "空";
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder sb = new StringBuilder();
            for (Object item : iterable) {
                if (item == null) {
                    continue;
                }
                if (!sb.isEmpty()) {
                    sb.append("，");
                }
                sb.append(item);
            }
            return sb.isEmpty() ? "空" : sb.toString();
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "空" : text;
    }

    private static String normalize(String action) {
        return action == null ? "" : action.trim().toLowerCase(Locale.ROOT);
    }

    private static String buildIdentityContent(
            String act,
            String resourceKey,
            String subjectPart,
            boolean success,
            Map<String, Object> beforeData,
            Map<String, Object> afterData) {
        String result = success ? "成功" : "失败";
        if ("real_name_case".equals(resourceKey)) {
            return switch (act) {
                case "submit" -> "提交实名认证" + subjectPart + identityHint(afterData) + result;
                case "approve" -> "通过实名认证审核" + subjectPart + result;
                case "reject" -> {
                    String remark = identityReviewRemark(afterData, beforeData);
                    if (StringUtils.hasText(remark)) {
                        yield "驳回实名认证" + subjectPart + "：" + remark + result;
                    }
                    yield "驳回实名认证" + subjectPart + result;
                }
                case "init_third_party", "init-third-party" ->
                        "发起第三方实名认证" + subjectPart + identityHint(afterData) + result;
                case "callback" -> "第三方实名认证回调" + subjectPart + result;
                default -> null;
            };
        }
        if ("profile_identity".equals(resourceKey) && "revoke".equals(act)) {
            return "撤销实名认证" + subjectPart + result;
        }
        return null;
    }

    private static String identityHint(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        appendIdentityHint(sb, data, "businessType", "business_type");
        appendIdentityHint(sb, data, "documentType", "document_type");
        appendIdentityHint(sb, data, "providerCode", "provider_code");
        if (sb.isEmpty()) {
            return "";
        }
        return "（" + sb + "）";
    }

    private static void appendIdentityHint(StringBuilder sb, Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                if (!sb.isEmpty()) {
                    sb.append("，");
                }
                sb.append(fieldLabel(key)).append("：").append(identityEnumLabel(String.valueOf(value)));
                return;
            }
        }
    }

    private static String identityReviewRemark(Map<String, Object> afterData, Map<String, Object> beforeData) {
        Object value = firstPresentValue(afterData, "reviewRemark", "review_remark");
        if (value == null) {
            value = firstPresentValue(beforeData, "reviewRemark", "review_remark");
        }
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        return "【审核意见】" + String.valueOf(value).trim();
    }

    private static Object firstPresentValue(Map<String, Object> map, String... keys) {
        if (map == null) {
            return null;
        }
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private static String identityEnumLabel(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (!StringUtils.hasText(value)) {
            return "空";
        }
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "ID_CARD" -> "身份证";
            case "PASSPORT" -> "护照";
            case "ACCOUNT_VERIFY" -> "人工审核";
            case "THIRD_PARTY" -> "第三方认证";
            case "EID" -> "电子身份证";
            default -> value;
        };
    }
}

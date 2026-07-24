-- ============================================================================
-- sys_config seed data
-- 说明：运行前请确认 sys_config 表已通过 Alembic 迁移创建
-- 使用 ON CONFLICT (config_key) DO NOTHING 保证幂等
-- ============================================================================

-- ============================================================================
-- AUTH_TOKEN — Token 配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200001', 'auth.token_ttl_seconds', '2592000', 'AUTH_TOKEN', 'Token 过期时间（秒），默认 30 天', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200002', 'auth.refresh_ttl_seconds', '2592000', 'AUTH_TOKEN', 'Refresh Token 过期时间（秒），默认 30 天', 2, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200003', 'auth.password_reset_token_ttl_seconds', '600', 'AUTH_TOKEN', '密码重置 Token 有效期（秒），默认 10 分钟', 3, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- ============================================================================
-- AUTH_LOGIN — 登录安全配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200010', 'auth.login_failure_window_seconds', '900', 'AUTH_LOGIN', '登录失败统计窗口（秒），默认 15 分钟', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200011', 'auth.login_account_max_failures', '5', 'AUTH_LOGIN', '单账号最大登录失败次数', 2, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200012', 'auth.login_ip_max_failures', '30', 'AUTH_LOGIN', '单 IP 最大登录失败次数', 3, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200013', 'auth.login_lock_seconds', '900', 'AUTH_LOGIN', '登录锁定时间（秒），默认 15 分钟', 4, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- ============================================================================
-- AUTH_REGISTER — 注册配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200020', 'auth.admin_register_enabled', 'false', 'AUTH_REGISTER', '管理端是否开放注册', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200021', 'auth.portal_register_enabled', 'true', 'AUTH_REGISTER', '门户端是否开放注册', 2, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- ============================================================================
-- AUTH_PASSWORD — 密码配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200030', 'auth.default_password', '', 'AUTH_PASSWORD', '新建账户的默认密码', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- ============================================================================
-- STORAGE — 存储基础配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200040', 'storage.provider', 'local', 'STORAGE', '存储服务商：local / minio / s3 / oss', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200041', 'storage.bucket', 'hei-fastapi', 'STORAGE', '存储桶名称', 2, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200042', 'storage.endpoint', 'http://127.0.0.1:9000', 'STORAGE', '存储服务端点地址', 3, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200043', 'storage.access_key', 'admin', 'STORAGE', '存储访问密钥 ID', 4, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200044', 'storage.secret_key', '123456', 'STORAGE', '存储访问密钥 Secret', 5, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200045', 'storage.region', 'us-east-1', 'STORAGE', '存储区域', 6, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200046', 'storage.use_ssl', 'false', 'STORAGE', '是否启用 SSL 连接', 7, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200047', 'storage.base_url', '', 'STORAGE', '自定义存储基础 URL（为空则使用 endpoint）', 8, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200048', 'storage.public_path', '/api/v1/files', 'STORAGE', '公开文件访问路径前缀', 9, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200049', 'storage.local_root', 'storage', 'STORAGE', '本地存储根目录', 10, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- ============================================================================
-- UPLOAD — 上传配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200050', 'storage.upload_max_bytes', '10485760', 'UPLOAD', '上传文件大小上限（字节），默认 10 MB', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200051', 'storage.public_upload_enabled', 'false', 'UPLOAD', '是否允许公开上传', 2, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200052', 'storage.presign_expire_seconds', '3600', 'UPLOAD', '预签名 URL 有效期（秒），默认 1 小时', 3, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200053', 'storage.upload_allowed_content_types', '["image/jpeg","image/png","image/webp","application/pdf","text/plain"]', 'UPLOAD', '允许的 MIME 类型列表（JSON 数组）', 4, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200054', 'storage.upload_allowed_extensions', '[".jpg",".jpeg",".png",".webp",".pdf",".txt"]', 'UPLOAD', '允许的文件扩展名列表（JSON 数组）', 5, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200055', 'storage.upload_denied_extensions', '[".exe",".bat",".cmd",".sh",".js",".html",".php",".py",".jar"]', 'UPLOAD', '禁止上传的扩展名列表（JSON 数组）', 6, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200056', 'storage.upload_category_max_length', '64', 'UPLOAD', '上传分类名最大长度', 7, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

-- ============================================================================
-- MAIL — 邮件配置
-- ============================================================================
INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200060', 'mail.host', '', 'MAIL', 'SMTP 服务器地址', 1, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200061', 'mail.port', '587', 'MAIL', 'SMTP 端口', 2, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200062', 'mail.username', '', 'MAIL', 'SMTP 用户名', 3, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200063', 'mail.password', '', 'MAIL', 'SMTP 密码', 4, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200064', 'mail.from_email', '', 'MAIL', '发件人邮箱', 5, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200065', 'mail.from_name', 'hei-fastapi', 'MAIL', '发件人显示名称', 6, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO "public"."sys_config" ("id", "config_key", "config_value", "category", "remark", "sort_code", "ext_json", "created_at", "updated_at")
VALUES ('200066', 'mail.use_tls', 'true', 'MAIL', '是否启用 TLS', 7, '{}', now(), now())
ON CONFLICT (config_key) DO NOTHING;

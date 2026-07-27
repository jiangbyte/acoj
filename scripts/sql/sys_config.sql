/*
 Navicat Premium Dump SQL

 Source Server         : dev-postgres
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : 127.0.0.1:5432
 Source Catalog        : hei_fastapi
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 24/07/2026 20:49:47
*/


-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_config";
CREATE TABLE "public"."sys_config" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "config_key" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "config_value" text COLLATE "pg_catalog"."default",
  "category" varchar(255) COLLATE "pg_catalog"."default",
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "sort_code" int4 NOT NULL,
  "ext_json" json NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_config"."id" IS '主键';
COMMENT ON COLUMN "public"."sys_config"."config_key" IS '配置键';
COMMENT ON COLUMN "public"."sys_config"."config_value" IS '配置值';
COMMENT ON COLUMN "public"."sys_config"."category" IS '分类';
COMMENT ON COLUMN "public"."sys_config"."remark" IS '备注';
COMMENT ON COLUMN "public"."sys_config"."sort_code" IS '排序码';
COMMENT ON COLUMN "public"."sys_config"."ext_json" IS '扩展信息';
COMMENT ON COLUMN "public"."sys_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_config"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."sys_config"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_config"."updated_by" IS '更新人';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO "public"."sys_config" VALUES ('200010', 'auth.login_failure_window_seconds', '900', 'AUTH_LOGIN', '登录失败统计窗口（秒），默认 15 分钟', 1, '{}', '2026-07-24 06:03:41.858752+00', NULL, '2026-07-24 06:03:41.858752+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200011', 'auth.login_account_max_failures', '5', 'AUTH_LOGIN', '单账号最大登录失败次数', 2, '{}', '2026-07-24 06:03:41.861694+00', NULL, '2026-07-24 06:03:41.861694+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200012', 'auth.login_ip_max_failures', '30', 'AUTH_LOGIN', '单 IP 最大登录失败次数', 3, '{}', '2026-07-24 06:03:41.865081+00', NULL, '2026-07-24 06:03:41.865081+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200013', 'auth.login_lock_seconds', '900', 'AUTH_LOGIN', '登录锁定时间（秒），默认 15 分钟', 4, '{}', '2026-07-24 06:03:41.868232+00', NULL, '2026-07-24 06:03:41.868232+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200020', 'auth.admin_register_enabled', 'false', 'AUTH_REGISTER', '管理端是否开放注册', 1, '{}', '2026-07-24 06:03:41.871573+00', NULL, '2026-07-24 06:03:41.871573+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200021', 'auth.portal_register_enabled', 'true', 'AUTH_REGISTER', '门户端是否开放注册', 2, '{}', '2026-07-24 06:03:41.875251+00', NULL, '2026-07-24 06:03:41.875251+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200030', 'auth.default_password', 'gAAAAABqYyOFYorm1xgsmueRMxleIU4haHixc2-8irdM1VoZBCT0lYEkEnDLPNVqH2Twp_1a-0RjM9U78OpMxlGQ7x6UVwOXgA==', 'AUTH_PASSWORD', '新建账户的默认密码', 1, '{}', '2026-07-24 06:03:41.878493+00', NULL, '2026-07-24 08:34:13.700094+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200060', 'mail.host', 'localhost', 'MAIL', 'SMTP 服务器地址', 1, '{}', '2026-07-24 06:03:41.946374+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('crypto_storage_access_key', 'storage.access_key', '', 'STORAGE', NULL, 1, '{}', '2026-07-24 07:46:42.70225+00', NULL, '2026-07-24 07:46:42.867406+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('crypto_storage_secret_key', 'storage.secret_key', '', 'STORAGE', NULL, 1, '{}', '2026-07-24 07:46:42.70225+00', NULL, '2026-07-24 07:46:42.867406+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200061', 'mail.port', '1025', 'MAIL', 'SMTP 端口', 2, '{}', '2026-07-24 06:03:41.951034+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200001', 'auth.token_ttl_seconds', '2592000', 'AUTH_TOKEN', 'Token 过期时间（秒），默认 30 天', 1, '{}', '2026-07-24 06:03:41.837583+00', NULL, '2026-07-24 08:00:48.042285+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200002', 'auth.refresh_ttl_seconds', '2592000', 'AUTH_TOKEN', 'Refresh Token 过期时间（秒），默认 30 天', 2, '{}', '2026-07-24 06:03:41.852806+00', NULL, '2026-07-24 08:00:48.042285+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200003', 'auth.password_reset_token_ttl_seconds', '600', 'AUTH_TOKEN', '密码重置 Token 有效期（秒），默认 10 分钟', 3, '{}', '2026-07-24 06:03:41.856009+00', NULL, '2026-07-24 08:00:48.042285+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200062', 'mail.username', '', 'MAIL', 'SMTP 用户名', 3, '{}', '2026-07-24 06:03:41.95505+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200064', 'mail.from_email', 'test@hei-fastapi.local', 'MAIL', '发件人邮箱', 5, '{}', '2026-07-24 06:03:41.962994+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200065', 'mail.from_name', 'hei-fastapi', 'MAIL', '发件人显示名称', 6, '{}', '2026-07-24 06:03:41.967142+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200066', 'mail.use_tls', 'false', 'MAIL', '是否启用 TLS', 7, '{}', '2026-07-24 06:03:41.97235+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200063', 'mail.password', '', 'MAIL', 'SMTP 密码', 4, '{}', '2026-07-24 06:03:41.958755+00', NULL, '2026-07-24 09:10:47.68563+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200050', 'storage.upload_max_bytes', '10485760', 'UPLOAD', '上传文件大小上限（字节），默认 10 MB', 1, '{}', '2026-07-24 06:03:41.918638+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200051', 'storage.public_upload_enabled', 'false', 'UPLOAD', '是否允许公开上传', 2, '{}', '2026-07-24 06:03:41.921745+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200052', 'storage.presign_expire_seconds', '3600', 'UPLOAD', '预签名 URL 有效期（秒），默认 1 小时', 3, '{}', '2026-07-24 06:03:41.925569+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200053', 'storage.upload_allowed_content_types', '["image/jpeg","image/png","image/webp","application/pdf","text/plain","application/octet-stream"]', 'UPLOAD', '允许的 MIME 类型列表（JSON 数组）', 4, '{}', '2026-07-24 06:03:41.930356+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200054', 'storage.upload_allowed_extensions', '[".jpg",".jpeg",".png",".webp",".pdf",".txt",".ini"]', 'UPLOAD', '允许的文件扩展名列表（JSON 数组）', 5, '{}', '2026-07-24 06:03:41.934689+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200055', 'storage.upload_denied_extensions', '[".exe",".bat",".cmd",".sh",".js",".html",".php",".py",".jar"]', 'UPLOAD', '禁止上传的扩展名列表（JSON 数组）', 6, '{}', '2026-07-24 06:03:41.938463+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('200056', 'storage.upload_category_max_length', '64', 'UPLOAD', '上传分类名最大长度', 7, '{}', '2026-07-24 06:03:41.942372+00', NULL, '2026-07-24 08:54:20.844448+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('85b616ba-98b3-46b1-b7fb-e9b63480b608', 'mail.template.forgot_password.subject', '{{app_name}} 密码重置', 'MAIL_TEMPLATE', '忘记密码邮件主题模板', 10, '{}', '2026-07-24 09:25:30.72193+00', NULL, '2026-07-24 09:25:30.72193+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('3d20f2bd-509c-4993-a1cd-cea0b871f43c', 'mail.template.forgot_password.body', '请点击以下链接重置密码，该链接将在 {{expire_minutes}} 分钟内有效。

{{reset_link}}', 'MAIL_TEMPLATE', '忘记密码邮件正文模板', 20, '{}', '2026-07-24 09:25:30.72193+00', NULL, '2026-07-24 09:25:30.72193+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_100', 'audit_alert.enabled', 'true', 'AUDIT_ALERT', '审计告警总开关', 1, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_101', 'audit_alert.webhook_url', 'https://open.feishu.cn/open-apis/bot/v2/hook/xxxxx', 'AUDIT_ALERT', 'Webhook 地址', 2, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_102', 'audit_alert.webhook_secret', 'xxxxxxxxxxxxxxxx', 'AUDIT_ALERT', 'Webhook 签名密钥(可选)', 3, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_103', 'audit_alert.analysis_interval_seconds', '60', 'AUDIT_ALERT', '分析周期(秒)', 4, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_104', 'audit_alert.alert_cooldown_seconds', '1800', 'AUDIT_ALERT', '告警冷却(秒)', 5, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_105', 'audit_alert.rule_brute_force', 'true', 'AUDIT_ALERT', '暴力破解检测', 10, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_106', 'audit_alert.rule_unusual_hours', 'true', 'AUDIT_ALERT', '异常时间操作检测', 11, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_107', 'audit_alert.rule_sensitive_ops', 'true', 'AUDIT_ALERT', '敏感操作监控', 12, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_108', 'audit_alert.rule_bulk_delete', 'true', 'AUDIT_ALERT', '批量删除检测', 13, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_109', 'audit_alert.rule_ip_anomaly', 'true', 'AUDIT_ALERT', 'IP 异常检测', 14, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_110', 'audit_alert.brute_force_threshold', '10', 'AUDIT_ALERT', '暴力破解阈值(次/分钟)', 20, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_111', 'audit_alert.bulk_delete_threshold', '20', 'AUDIT_ALERT', '批量删除阈值(次/5分钟)', 21, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);
INSERT INTO "public"."sys_config" VALUES ('sac_112', 'audit_alert.ip_anomaly_threshold', '3', 'AUDIT_ALERT', 'IP异常阈值(不同IP数/15分钟)', 22, '{}', '2026-07-24 10:53:28.125414+00', NULL, '2026-07-24 11:18:54.991626+00', NULL);

-- ----------------------------
-- Indexes structure for table sys_config
-- ----------------------------
CREATE INDEX "idx_sys_config_category" ON "public"."sys_config" USING btree (
  "category" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "idx_sys_config_key" ON "public"."sys_config" USING btree (
  "config_key" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_config
-- ----------------------------
ALTER TABLE "public"."sys_config" ADD CONSTRAINT "pk_sys_config" PRIMARY KEY ("id");

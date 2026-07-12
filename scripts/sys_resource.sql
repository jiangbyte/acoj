/*
 Navicat Premium Dump SQL

 Source Server         : 8.130.8.80
 Source Server 类型    : PostgreSQL
 Source Server Version : 170010 (170010)
 Source Host           : 8.130.8.80:5433
 Source 目录        : hei_fastapi
 Source Schema         : public

 Target Server 类型    : PostgreSQL
 Target Server Version : 170010 (170010)
 File Encoding         : 65001

 Date: 12/07/2026 19:14:54
*/


-- ----------------------------
-- Table structure for sys_resource
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_resource";
CREATE TABLE "public"."sys_resource" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" varchar(64) COLLATE "pg_catalog"."default",
  "code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "resource_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" varchar(64) COLLATE "pg_catalog"."default",
  "path" varchar(255) COLLATE "pg_catalog"."default",
  "component" varchar(255) COLLATE "pg_catalog"."default",
  "redirect" varchar(255) COLLATE "pg_catalog"."default",
  "icon" varchar(255) COLLATE "pg_catalog"."default",
  "color" varchar(32) COLLATE "pg_catalog"."default",
  "href" varchar(255) COLLATE "pg_catalog"."default",
  "sort" int4 NOT NULL,
  "is_visible" bool NOT NULL,
  "is_cache" bool NOT NULL,
  "is_affix" bool NOT NULL,
  "status" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "extra" json NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_resource"."id" IS '主键';
COMMENT ON COLUMN "public"."sys_resource"."parent_id" IS '父资源ID';
COMMENT ON COLUMN "public"."sys_resource"."code" IS '资源编码';
COMMENT ON COLUMN "public"."sys_resource"."name" IS '资源名称';
COMMENT ON COLUMN "public"."sys_resource"."resource_type" IS '资源类型';
COMMENT ON COLUMN "public"."sys_resource"."module_id" IS '所属资源模块ID';
COMMENT ON COLUMN "public"."sys_resource"."path" IS '路由路径';
COMMENT ON COLUMN "public"."sys_resource"."component" IS '前端组件';
COMMENT ON COLUMN "public"."sys_resource"."redirect" IS '重定向地址';
COMMENT ON COLUMN "public"."sys_resource"."icon" IS '图标';
COMMENT ON COLUMN "public"."sys_resource"."color" IS '颜色';
COMMENT ON COLUMN "public"."sys_resource"."href" IS '外链地址';
COMMENT ON COLUMN "public"."sys_resource"."sort" IS '排序';
COMMENT ON COLUMN "public"."sys_resource"."is_visible" IS '是否可见';
COMMENT ON COLUMN "public"."sys_resource"."is_cache" IS '是否缓存';
COMMENT ON COLUMN "public"."sys_resource"."is_affix" IS '是否固定标签';
COMMENT ON COLUMN "public"."sys_resource"."status" IS '状态';
COMMENT ON COLUMN "public"."sys_resource"."description" IS '描述';
COMMENT ON COLUMN "public"."sys_resource"."extra" IS '扩展信息';
COMMENT ON COLUMN "public"."sys_resource"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_resource"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."sys_resource"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_resource"."updated_by" IS '更新人';

-- ----------------------------
-- Records of sys_resource
-- ----------------------------
INSERT INTO "public"."sys_resource" VALUES ('200001', NULL, 'dashboard', '运营工作台', 'MENU', '210001', '/dashboard', '/dashboard/index.vue', NULL, 'icon-park-outline:analysis', NULL, NULL, 1, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200003', NULL, 'sys', '系统', 'CATALOG', '210001', '/sys', NULL, NULL, 'icon-park-outline:setting-two', NULL, NULL, 10, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200004', '200003', 'sys-dict', '字典管理', 'MENU', '210001', '/sys/dict', '/sys/dict/index.vue', NULL, 'icon-park-outline:file-search', NULL, NULL, 2, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200005', '200003', 'sys-banner', '展示图管理', 'MENU', '210001', '/sys/banner', '/sys/banner/index.vue', NULL, 'icon-park-outline:ad-product', NULL, NULL, 3, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200023', '200003', 'sys-file', '文件管理', 'MENU', '210001', '/sys/file', '/sys/file/index.vue', NULL, 'icon-park-outline:file-code', NULL, NULL, 4, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200027', '200003', 'sys-audit-api', '操作审计接口', 'API_GROUP', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 9, 'f', 'f', 'f', 'ENABLED', '操作审计后端权限组', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200024', NULL, 'security', '认证', 'CATALOG', '210001', '/security', NULL, NULL, 'icon-park-outline:lock', NULL, NULL, 12, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200025', '200024', 'security-session', '在线会话', 'MENU', '210001', '/security/session', '/auth/session/index.vue', NULL, 'icon-park-outline:connection', NULL, NULL, 1, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200006', NULL, 'iam', '身份与权限', 'CATALOG', '210001', '/iam', NULL, NULL, 'icon-park-outline:permissions', NULL, NULL, 15, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200007', '200006', 'iam-account', '账号管理', 'MENU', '210001', '/iam/account', '/iam/account/index.vue', NULL, 'icon-park-outline:people', NULL, NULL, 1, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200008', '200006', 'iam-dept', '部门管理', 'MENU', '210001', '/iam/dept', '/iam/dept/index.vue', NULL, 'icon-park-outline:tree-diagram', NULL, NULL, 2, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200009', '200006', 'iam-group', '用户组管理', 'MENU', '210001', '/iam/group', '/iam/group/index.vue', NULL, 'icon-park-outline:group', NULL, NULL, 3, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200010', '200006', 'iam-position', '岗位管理', 'MENU', '210001', '/iam/position', '/iam/position/index.vue', NULL, 'icon-park-outline:people-bottom', NULL, NULL, 4, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200011', '200006', 'iam-role', '角色管理', 'MENU', '210001', '/iam/role', '/iam/role/index.vue', NULL, 'icon-park-outline:peoples', NULL, NULL, 5, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200012', '200006', 'iam-resource', '资源管理', 'MENU', '210001', '/iam/resource', '/iam/resource/index.vue', NULL, 'icon-park-outline:all-application', NULL, NULL, 6, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200018', '200006', 'iam-resourcemodule', '资源模块管理', 'MENU', '210001', '/iam/resource_module', '/iam/resource_module/index.vue', NULL, 'icon-park-outline:blocks-and-arrows', NULL, NULL, 7, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200019', NULL, 'message', '消息中心', 'CATALOG', '210001', '/message', NULL, NULL, 'icon-park-outline:message', NULL, NULL, 18, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200020', '200019', 'message-notification', '通知管理', 'MENU', '210001', '/message/notification', '/message/notification/index.vue', NULL, 'icon-park-outline:tips-one', NULL, NULL, 1, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200021', '200019', 'message-message', '站内信管理', 'MENU', '210001', '/message/message', '/message/message/index.vue', NULL, 'icon-park-outline:message', NULL, NULL, 2, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200022', '200019', 'message-todo', '待办管理', 'MENU', '210001', '/message/todo', '/message/todo/index.vue', NULL, 'icon-park-outline:checklist', NULL, NULL, 3, 't', 'f', 'f', 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200026', NULL, 'portal-demo', '示例页面', 'MENU', '210002', '/demo', '/demo/index.vue', NULL, 'icon-park-outline:experiment-one', NULL, NULL, 1, 't', 'f', 'f', 'ENABLED', '门户端公开示例菜单', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201011', '200004', 'sys-dict-create', '新增字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201012', '200004', 'sys-dict-detail', '查看字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201013', '200004', 'sys-dict-update', '编辑字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201014', '200004', 'sys-dict-delete', '删除字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201021', '200005', 'sys-banner-create', '新增展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201022', '200005', 'sys-banner-detail', '查看展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201023', '200005', 'sys-banner-update', '编辑展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201024', '200005', 'sys-banner-delete', '删除展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201031', '200023', 'sys-file-upload', '上传文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201032', '200023', 'sys-file-detail', '查看文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201033', '200023', 'sys-file-update', '编辑文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201034', '200023', 'sys-file-url', '打开文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201035', '200023', 'sys-file-delete', '删除文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201041', '200025', 'auth-session-tokenlist', '查看令牌', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201042', '200025', 'auth-session-exit', '强退账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201043', '200025', 'auth-session-tokenexit', '强退令牌', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201101', '200007', 'iam-account-create', '新增账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201102', '200007', 'iam-account-detail', '查看账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201103', '200007', 'iam-account-update', '编辑账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201104', '200007', 'iam-account-delete', '删除账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201105', '200007', 'iam-account-grant-role', '分配角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201106', '200007', 'iam-account-grant-group', '分配用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201107', '200007', 'iam-account-grant-dept', '分配部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 7, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201108', '200007', 'iam-account-grant-resource', '分配资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 8, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201121', '200008', 'iam-dept-create', '新增部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201122', '200008', 'iam-dept-detail', '查看部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201123', '200008', 'iam-dept-update', '编辑部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201124', '200008', 'iam-dept-delete', '删除部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201131', '200009', 'iam-group-create', '新增用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201132', '200009', 'iam-group-detail', '查看用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201133', '200009', 'iam-group-update', '编辑用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201134', '200009', 'iam-group-delete', '删除用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201135', '200009', 'iam-group-grant-user', '分配用户', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201136', '200009', 'iam-group-grant-role', '分配角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201137', '200009', 'iam-group-grant-resource', '分配资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 7, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201151', '200010', 'iam-position-create', '新增岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201152', '200010', 'iam-position-detail', '查看岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201153', '200010', 'iam-position-update', '编辑岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201154', '200010', 'iam-position-delete', '删除岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201161', '200011', 'iam-role-create', '新增角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201162', '200011', 'iam-role-detail', '查看角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201163', '200011', 'iam-role-update', '编辑角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201164', '200011', 'iam-role-delete', '删除角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201165', '200011', 'iam-role-grant-resource', '分配资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201167', '200011', 'iam-role-grant-user', '分配用户', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 7, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201181', '200012', 'iam-resource-create', '新增资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201182', '200012', 'iam-resource-detail', '查看资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201183', '200012', 'iam-resource-update', '编辑资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201184', '200012', 'iam-resource-delete', '删除资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201185', '200012', 'iam-resource-grant', '绑定权限', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201191', '200018', 'iam-resourcemodule-create', '新增资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201192', '200018', 'iam-resourcemodule-detail', '查看资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201193', '200018', 'iam-resourcemodule-update', '编辑资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201194', '200018', 'iam-resourcemodule-delete', '删除资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201201', '200020', 'message-notification-create', '新增通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201202', '200020', 'message-notification-detail', '查看通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201203', '200020', 'message-notification-update', '编辑通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201204', '200020', 'message-notification-delete', '删除通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201205', '200020', 'message-notification-publish', '发布通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201206', '200020', 'message-notification-revoke', '撤回通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201221', '200021', 'message-thread-detail', '查看会话', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201222', '200021', 'message-thread-send', '发送站内信', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201223', '200021', 'message-group-create', '新增消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201224', '200021', 'message-group-detail', '查看消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201225', '200021', 'message-group-update', '编辑消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201226', '200021', 'message-group-delete', '删除消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201241', '200022', 'message-todo-create', '新增待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201242', '200022', 'message-todo-detail', '查看待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201243', '200022', 'message-todo-update', '编辑待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201244', '200022', 'message-todo-delete', '删除待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('201245', '200022', 'message-todo-cancel', '取消待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, 'f', 'f', 'f', 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('7481617843012898816', NULL, 'test', 'test', 'MENU', '7481609907767218176', 'test', NULL, NULL, NULL, NULL, NULL, 0, 't', 'f', 'f', 'ENABLED', 'testtest', '{}', '2026-07-11 07:58:15.841688+00', '1', '2026-07-11 07:59:08.611726+00', '1');

-- ----------------------------
-- Uniques structure for table sys_resource
-- ----------------------------
ALTER TABLE "public"."sys_resource" ADD CONSTRAINT "uq_sys_resource_code" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table sys_resource
-- ----------------------------
ALTER TABLE "public"."sys_resource" ADD CONSTRAINT "pk_sys_resource" PRIMARY KEY ("id");

-- ----------------------------
-- Table structure for sys_resource_module
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_resource_module";
CREATE TABLE "public"."sys_resource_module" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "client" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "locale_key" varchar(255) COLLATE "pg_catalog"."default",
  "icon" varchar(255) COLLATE "pg_catalog"."default",
  "color" varchar(32) COLLATE "pg_catalog"."default",
  "sort" int4 NOT NULL,
  "status" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "extra" json NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_resource_module"."id" IS '主键';
COMMENT ON COLUMN "public"."sys_resource_module"."name" IS '模块名称';
COMMENT ON COLUMN "public"."sys_resource_module"."code" IS '模块编码';
COMMENT ON COLUMN "public"."sys_resource_module"."client" IS '所属端';
COMMENT ON COLUMN "public"."sys_resource_module"."locale_key" IS '国际化键';
COMMENT ON COLUMN "public"."sys_resource_module"."icon" IS '图标';
COMMENT ON COLUMN "public"."sys_resource_module"."color" IS '颜色';
COMMENT ON COLUMN "public"."sys_resource_module"."sort" IS '排序';
COMMENT ON COLUMN "public"."sys_resource_module"."status" IS '状态';
COMMENT ON COLUMN "public"."sys_resource_module"."description" IS '描述';
COMMENT ON COLUMN "public"."sys_resource_module"."extra" IS '扩展信息';
COMMENT ON COLUMN "public"."sys_resource_module"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_resource_module"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."sys_resource_module"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_resource_module"."updated_by" IS '更新人';

-- ----------------------------
-- Records of sys_resource_module
-- ----------------------------
INSERT INTO "public"."sys_resource_module" ("id", "name", "code", "client", "locale_key", "icon", "color", "sort", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('210001', '系统', 'system', 'ADMIN', 'resource.sys.title', 'icon-park-outline:setting-two', '#2563eb', 1, 'ENABLED', '系统内置资源模块', '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource_module" ("id", "name", "code", "client", "locale_key", "icon", "color", "sort", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('210002', '门户', 'portal', 'PORTAL', NULL, 'icon-park-outline:browser', '#18a058', 2, 'ENABLED', '门户端公开资源模块', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);

-- ----------------------------
-- Table structure for sys_resource
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_resource";
CREATE TABLE "public"."sys_resource" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" varchar(64) COLLATE "pg_catalog"."default",
  "code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "locale_key" varchar(255) COLLATE "pg_catalog"."default",
  "resource_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "module_id" varchar(64) COLLATE "pg_catalog"."default",
  "path" varchar(255) COLLATE "pg_catalog"."default",
  "component" varchar(255) COLLATE "pg_catalog"."default",
  "redirect" varchar(255) COLLATE "pg_catalog"."default",
  "icon" varchar(255) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "public"."sys_resource"."locale_key" IS '国际化键';
COMMENT ON COLUMN "public"."sys_resource"."resource_type" IS '资源类型';
COMMENT ON COLUMN "public"."sys_resource"."module_id" IS '所属资源模块ID';
COMMENT ON COLUMN "public"."sys_resource"."path" IS '路由路径';
COMMENT ON COLUMN "public"."sys_resource"."component" IS '前端组件';
COMMENT ON COLUMN "public"."sys_resource"."redirect" IS '重定向地址';
COMMENT ON COLUMN "public"."sys_resource"."icon" IS '图标';
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
-- Table structure for sys_resource_permission_rel
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_resource_permission_rel";
CREATE TABLE "public"."sys_resource_permission_rel" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "resource_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "permission_key" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "data_scope" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "custom_scope_dept_ids" json NOT NULL,
  "sort" int4 NOT NULL,
  "status" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."id" IS '主键';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."resource_id" IS '资源ID';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."permission_key" IS '权限标识';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."data_scope" IS '数据范围';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."custom_scope_dept_ids" IS '自定义数据范围部门ID列表';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."sort" IS '排序';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."status" IS '状态';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."description" IS '描述';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_resource_permission_rel"."updated_by" IS '更新人';

-- ----------------------------
-- Records of sys_resource
-- ----------------------------
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200001', NULL, 'dashboard', '工作台', 'resource.dashboard.title', 'MENU', '210001', '/dashboard', '/dashboard/index.vue', NULL, 'icon-park-outline:analysis', NULL, 1, true, false, true, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200003', NULL, 'sys', '系统管理', 'resource.sys.title', 'CATALOG', '210001', '/sys', NULL, NULL, 'icon-park-outline:setting-two', NULL, 10, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200004', '200003', 'sys-dict', '字典管理', 'resource.sys.dict.title', 'MENU', '210001', '/sys/dict', '/sys/dict/index.vue', NULL, 'icon-park-outline:file-search', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200005', '200003', 'sys-banner', '展示图管理', 'resource.sys.banner.title', 'MENU', '210001', '/sys/banner', '/sys/banner/index.vue', NULL, 'icon-park-outline:ad-product', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200023', '200003', 'sys-file', '文件管理', 'resource.sys.file.title', 'MENU', '210001', '/sys/file', '/sys/file/index.vue', NULL, 'icon-park-outline:file-code', NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200027', '200003', 'sys-audit-api', '操作审计接口', NULL, 'API_GROUP', '210001', NULL, NULL, NULL, NULL, NULL, 9, false, false, false, 'ENABLED', '操作审计后端权限组', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200024', NULL, 'security', '认证管理', 'resource.auth.title', 'CATALOG', '210001', '/security', NULL, NULL, 'icon-park-outline:lock', NULL, 12, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200025', '200024', 'security-session', '在线会话', 'resource.auth.session.title', 'MENU', '210001', '/security/session', '/auth/session/index.vue', NULL, 'icon-park-outline:connection', NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200006', NULL, 'iam', '身份权限', 'resource.iam.title', 'CATALOG', '210001', '/iam', NULL, NULL, 'icon-park-outline:permissions', NULL, 15, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200007', '200006', 'iam-account', '账号管理', 'resource.iam.account.title', 'MENU', '210001', '/iam/account', '/iam/account/index.vue', NULL, 'icon-park-outline:people', NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200008', '200006', 'iam-dept', '部门管理', 'resource.iam.dept.title', 'MENU', '210001', '/iam/dept', '/iam/dept/index.vue', NULL, 'icon-park-outline:tree-diagram', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200009', '200006', 'iam-group', '用户组管理', 'resource.iam.group.title', 'MENU', '210001', '/iam/group', '/iam/group/index.vue', NULL, 'icon-park-outline:group', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200010', '200006', 'iam-position', '岗位管理', 'resource.iam.position.title', 'MENU', '210001', '/iam/position', '/iam/position/index.vue', NULL, 'icon-park-outline:people-bottom', NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200011', '200006', 'iam-role', '角色管理', 'resource.iam.role.title', 'MENU', '210001', '/iam/role', '/iam/role/index.vue', NULL, 'icon-park-outline:peoples', NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200012', '200006', 'iam-resource', '资源管理', 'resource.iam.resource.title', 'MENU', '210001', '/iam/resource', '/iam/resource/index.vue', NULL, 'icon-park-outline:all-application', NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200018', '200006', 'iam-resourcemodule', '资源模块管理', 'resource.iam.resource_module.title', 'MENU', '210001', '/iam/resource_module', '/iam/resource_module/index.vue', NULL, 'icon-park-outline:blocks-and-arrows', NULL, 7, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200019', NULL, 'message', '消息中心', 'resource.message.title', 'CATALOG', '210001', '/message', NULL, NULL, 'icon-park-outline:message', NULL, 18, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200020', '200019', 'message-notification', '通知管理', 'resource.message.notification.title', 'MENU', '210001', '/message/notification', '/message/notification/index.vue', NULL, 'icon-park-outline:tips-one', NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200021', '200019', 'message-message', '站内信管理', 'resource.message.message.title', 'MENU', '210001', '/message/message', '/message/message/index.vue', NULL, 'icon-park-outline:message', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200022', '200019', 'message-todo', '待办管理', 'resource.message.todo.title', 'MENU', '210001', '/message/todo', '/message/todo/index.vue', NULL, 'icon-park-outline:checklist', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200026', NULL, 'portal-demo', '示例页面', 'resource.demo.title', 'MENU', '210002', '/demo', '/demo/index.vue', NULL, 'icon-park-outline:experiment-one', NULL, 1, true, false, false, 'ENABLED', '门户端公开示例菜单', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);

-- ----------------------------
-- Records of sys_resource button resources
-- ----------------------------
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES
('201011', '200004', 'sys-dict-create', '新增字典', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201012', '200004', 'sys-dict-detail', '查看字典', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201013', '200004', 'sys-dict-update', '编辑字典', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201014', '200004', 'sys-dict-delete', '删除字典', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201021', '200005', 'sys-banner-create', '新增展示图', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201022', '200005', 'sys-banner-detail', '查看展示图', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201023', '200005', 'sys-banner-update', '编辑展示图', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201024', '200005', 'sys-banner-delete', '删除展示图', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201031', '200023', 'sys-file-upload', '上传文件', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201032', '200023', 'sys-file-detail', '查看文件', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201033', '200023', 'sys-file-update', '编辑文件', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201034', '200023', 'sys-file-url', '打开文件', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201035', '200023', 'sys-file-delete', '删除文件', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201041', '200025', 'auth-session-tokenlist', '查看令牌', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201042', '200025', 'auth-session-exit', '强退账号', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201043', '200025', 'auth-session-tokenexit', '强退令牌', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201101', '200007', 'iam-account-create', '新增账号', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201102', '200007', 'iam-account-detail', '查看账号', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201103', '200007', 'iam-account-update', '编辑账号', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201104', '200007', 'iam-account-delete', '删除账号', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201105', '200007', 'iam-account-grant-role', '分配角色', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201106', '200007', 'iam-account-grant-group', '分配用户组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 6, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201107', '200007', 'iam-account-grant-dept', '分配部门', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 7, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201108', '200007', 'iam-account-grant-resource', '分配资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 8, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201109', '200007', 'iam-account-grant-permission', '分配权限', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 9, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201121', '200008', 'iam-dept-create', '新增部门', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201122', '200008', 'iam-dept-detail', '查看部门', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201123', '200008', 'iam-dept-update', '编辑部门', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201124', '200008', 'iam-dept-delete', '删除部门', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201131', '200009', 'iam-group-create', '新增用户组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201132', '200009', 'iam-group-detail', '查看用户组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201133', '200009', 'iam-group-update', '编辑用户组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201134', '200009', 'iam-group-delete', '删除用户组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201135', '200009', 'iam-group-grant-user', '分配用户', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201136', '200009', 'iam-group-grant-role', '分配角色', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 6, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201137', '200009', 'iam-group-grant-resource', '分配资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 7, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201138', '200009', 'iam-group-grant-permission', '分配权限', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 8, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201151', '200010', 'iam-position-create', '新增岗位', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201152', '200010', 'iam-position-detail', '查看岗位', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201153', '200010', 'iam-position-update', '编辑岗位', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201154', '200010', 'iam-position-delete', '删除岗位', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201161', '200011', 'iam-role-create', '新增角色', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201162', '200011', 'iam-role-detail', '查看角色', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201163', '200011', 'iam-role-update', '编辑角色', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201164', '200011', 'iam-role-delete', '删除角色', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201165', '200011', 'iam-role-grant-resource', '分配资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201166', '200011', 'iam-role-grant-permission', '分配权限', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 6, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201167', '200011', 'iam-role-grant-user', '分配用户', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 7, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201181', '200012', 'iam-resource-create', '新增资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201182', '200012', 'iam-resource-detail', '查看资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201183', '200012', 'iam-resource-update', '编辑资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201184', '200012', 'iam-resource-delete', '删除资源', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201185', '200012', 'iam-resource-grant', '绑定权限', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201191', '200018', 'iam-resourcemodule-create', '新增资源模块', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201192', '200018', 'iam-resourcemodule-detail', '查看资源模块', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201193', '200018', 'iam-resourcemodule-update', '编辑资源模块', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201194', '200018', 'iam-resourcemodule-delete', '删除资源模块', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201201', '200020', 'message-notification-create', '新增通知', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201202', '200020', 'message-notification-detail', '查看通知', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201203', '200020', 'message-notification-update', '编辑通知', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201204', '200020', 'message-notification-delete', '删除通知', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201205', '200020', 'message-notification-publish', '发布通知', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201206', '200020', 'message-notification-revoke', '撤回通知', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 6, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201221', '200021', 'message-thread-detail', '查看会话', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201222', '200021', 'message-thread-send', '发送站内信', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201223', '200021', 'message-group-create', '新增消息组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201224', '200021', 'message-group-detail', '查看消息组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201225', '200021', 'message-group-update', '编辑消息组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201226', '200021', 'message-group-delete', '删除消息组', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 6, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201241', '200022', 'message-todo-create', '新增待办', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 1, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201242', '200022', 'message-todo-detail', '查看待办', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 2, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201243', '200022', 'message-todo-update', '编辑待办', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 3, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201244', '200022', 'message-todo-delete', '删除待办', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 4, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL),
('201245', '200022', 'message-todo-cancel', '取消待办', NULL, 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);

-- ----------------------------
-- Records of sys_resource_permission_rel
-- ----------------------------
INSERT INTO "public"."sys_resource_permission_rel" ("id", "resource_id", "permission_key", "data_scope", "custom_scope_dept_ids", "sort", "status", "description", "created_at", "created_by", "updated_at", "updated_by")
SELECT (400000 + row_number() OVER (ORDER BY v.sort, v.resource_id, v.permission_key))::text, v.resource_id, v.permission_key, 'ALL', '[]'::json, v.sort, 'ENABLED', v.description, '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL
FROM (VALUES
(1, '200001', 'dashboard:overview:view', '工作台查看'),
(10, '200004', 'sys:dict:page', '字典分页'),
(11, '200027', 'sys:audit:page', '操作审计分页'),
(12, '200027', 'sys:audit:detail', '操作审计详情'),
(20, '201011', 'sys:dict:create', '新增字典'),
(21, '201012', 'sys:dict:detail', '查看字典'),
(22, '201013', 'sys:dict:update', '编辑字典'),
(23, '201014', 'sys:dict:delete', '删除字典'),
(30, '200005', 'sys:banner:page', '展示图分页'),
(40, '201021', 'sys:banner:create', '新增展示图'),
(41, '201022', 'sys:banner:detail', '查看展示图'),
(42, '201023', 'sys:banner:update', '编辑展示图'),
(43, '201024', 'sys:banner:delete', '删除展示图'),
(50, '200023', 'sys:file:page', '文件分页'),
(60, '201031', 'sys:file:upload', '上传文件'),
(61, '201032', 'sys:file:detail', '查看文件'),
(62, '201033', 'sys:file:update', '编辑文件'),
(63, '201034', 'sys:file:url', '打开文件'),
(64, '201034', 'sys:file:presignedurl', '获取文件预签名地址'),
(65, '201035', 'sys:file:delete', '删除文件'),
(70, '200025', 'auth:session:analysis', '会话分析'),
(71, '200025', 'auth:session:page', '会话分页'),
(80, '201041', 'auth:session:tokenlist', '查看令牌'),
(81, '201042', 'auth:session:exit', '强退账号'),
(82, '201043', 'auth:session:tokenexit', '强退令牌'),
(100, '200007', 'iam:account:page', '账号分页'),
(110, '201101', 'iam:account:create', '新增账号'),
(111, '201102', 'iam:account:detail', '查看账号'),
(112, '201103', 'iam:account:update', '编辑账号'),
(113, '201104', 'iam:account:delete', '删除账号'),
(114, '201105', 'iam:account:ownrole', '获取账号角色'),
(115, '201105', 'iam:account:grantrole', '分配账号角色'),
(116, '201106', 'iam:account:owngroup', '获取账号用户组'),
(117, '201106', 'iam:account:grantgroup', '分配账号用户组'),
(118, '201107', 'iam:account:owndept', '获取账号部门'),
(119, '201107', 'iam:account:grantdept', '分配账号部门'),
(120, '201107', 'iam:dept:list', '部门树选择'),
(121, '201108', 'iam:account:ownresource', '获取账号资源'),
(122, '201108', 'iam:account:grantresource', '分配账号资源'),
(123, '201109', 'iam:account:ownpermission', '获取账号权限'),
(124, '201109', 'iam:account:grantpermission', '分配账号权限'),
(130, '200008', 'iam:dept:page', '部门分页'),
(131, '200008', 'iam:dept:list', '部门列表'),
(140, '201121', 'iam:dept:create', '新增部门'),
(141, '201122', 'iam:dept:detail', '查看部门'),
(142, '201123', 'iam:dept:update', '编辑部门'),
(143, '201124', 'iam:dept:delete', '删除部门'),
(150, '200009', 'iam:group:page', '用户组分页'),
(160, '201131', 'iam:group:create', '新增用户组'),
(161, '201132', 'iam:group:detail', '查看用户组'),
(162, '201133', 'iam:group:update', '编辑用户组'),
(163, '201134', 'iam:group:delete', '删除用户组'),
(164, '201135', 'iam:group:ownuser', '获取用户组用户'),
(165, '201135', 'iam:group:grantuser', '分配用户组用户'),
(166, '201136', 'iam:group:ownrole', '获取用户组角色'),
(167, '201136', 'iam:group:grantrole', '分配用户组角色'),
(168, '201137', 'iam:group:ownresource', '获取用户组资源'),
(169, '201137', 'iam:group:grantresource', '分配用户组资源'),
(170, '201138', 'iam:group:ownpermission', '获取用户组权限'),
(171, '201138', 'iam:group:grantpermission', '分配用户组权限'),
(180, '200010', 'iam:position:page', '岗位分页'),
(190, '201151', 'iam:position:create', '新增岗位'),
(191, '201152', 'iam:position:detail', '查看岗位'),
(192, '201153', 'iam:position:update', '编辑岗位'),
(193, '201154', 'iam:position:delete', '删除岗位'),
(200, '200011', 'iam:role:page', '角色分页'),
(210, '201161', 'iam:role:create', '新增角色'),
(211, '201162', 'iam:role:detail', '查看角色'),
(212, '201163', 'iam:role:update', '编辑角色'),
(213, '201164', 'iam:role:delete', '删除角色'),
(214, '201165', 'iam:role:ownresource', '获取角色资源'),
(215, '201165', 'iam:role:grantresource', '分配角色资源'),
(216, '201166', 'iam:role:permissiontree', '角色权限树'),
(217, '201166', 'iam:role:ownpermission', '获取角色权限'),
(218, '201166', 'iam:role:grantpermission', '分配角色权限'),
(219, '201166', 'iam:dept:list', '部门树选择'),
(220, '201167', 'iam:role:ownuser', '获取角色用户'),
(221, '201167', 'iam:role:grantuser', '分配角色用户'),
(230, '200012', 'iam:resource:page', '资源分页'),
(231, '200012', 'iam:resource:list', '资源列表'),
(240, '201181', 'iam:resource:create', '新增资源'),
(241, '201182', 'iam:resource:detail', '查看资源'),
(242, '201183', 'iam:resource:update', '编辑资源'),
(243, '201184', 'iam:resource:delete', '删除资源'),
(244, '201185', 'iam:resource:grant', '绑定资源权限'),
(250, '200018', 'iam:resourcemodule:page', '资源模块分页'),
(260, '201191', 'iam:resourcemodule:create', '新增资源模块'),
(261, '201192', 'iam:resourcemodule:detail', '查看资源模块'),
(262, '201193', 'iam:resourcemodule:update', '编辑资源模块'),
(263, '201194', 'iam:resourcemodule:delete', '删除资源模块'),
(270, '200020', 'message:notification:page', '通知分页'),
(280, '201201', 'message:notification:create', '新增通知'),
(281, '201202', 'message:notification:detail', '查看通知'),
(282, '201203', 'message:notification:update', '编辑通知'),
(283, '201204', 'message:notification:delete', '删除通知'),
(284, '201205', 'message:notification:publish', '发布通知'),
(285, '201206', 'message:notification:revoke', '撤回通知'),
(290, '200021', 'message:thread:page', '站内信分页'),
(291, '200021', 'message:group:page', '消息组分页'),
(300, '201221', 'message:thread:detail', '查看站内信'),
(301, '201222', 'message:thread:send', '发送站内信'),
(302, '201223', 'message:group:create', '新增消息组'),
(303, '201224', 'message:group:detail', '查看消息组'),
(304, '201225', 'message:group:update', '编辑消息组'),
(305, '201226', 'message:group:delete', '删除消息组'),
(310, '200022', 'message:todo:page', '待办分页'),
(320, '201241', 'message:todo:create', '新增待办'),
(321, '201242', 'message:todo:detail', '查看待办'),
(322, '201243', 'message:todo:update', '编辑待办'),
(323, '201244', 'message:todo:delete', '删除待办'),
(324, '201245', 'message:todo:cancel', '取消待办')
) AS v(sort, resource_id, permission_key, description);

-- ----------------------------
-- Records of sys_subject_resource_grant_rel
-- 超管角色（id=1）默认拥有全部资源
-- ----------------------------
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300001', 'ROLE', '1', '200001', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300003', 'ROLE', '1', '200003', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300004', 'ROLE', '1', '200004', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300005', 'ROLE', '1', '200005', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300023', 'ROLE', '1', '200023', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300027', 'ROLE', '1', '200027', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300024', 'ROLE', '1', '200024', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300025', 'ROLE', '1', '200025', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300006', 'ROLE', '1', '200006', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300007', 'ROLE', '1', '200007', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300008', 'ROLE', '1', '200008', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300009', 'ROLE', '1', '200009', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300010', 'ROLE', '1', '200010', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300011', 'ROLE', '1', '200011', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300012', 'ROLE', '1', '200012', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300018', 'ROLE', '1', '200018', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300019', 'ROLE', '1', '200019', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300020', 'ROLE', '1', '200020', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300021', 'ROLE', '1', '200021', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300022', 'ROLE', '1', '200022', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by")
SELECT concat('3', substring(id from 2)), 'ROLE', '1', id, 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认按钮资源授权', NULL, '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL
FROM "public"."sys_resource"
WHERE "resource_type" = 'BUTTON' AND "module_id" = '210001';

-- ----------------------------
-- Uniques structure for table sys_resource_module
-- ----------------------------
ALTER TABLE "public"."sys_resource_module" ADD CONSTRAINT "uq_sys_resource_module_code" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table sys_resource_module
-- ----------------------------
ALTER TABLE "public"."sys_resource_module" ADD CONSTRAINT "pk_sys_resource_module" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table sys_resource
-- ----------------------------
ALTER TABLE "public"."sys_resource" ADD CONSTRAINT "uq_sys_resource_code" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table sys_resource
-- ----------------------------
ALTER TABLE "public"."sys_resource" ADD CONSTRAINT "pk_sys_resource" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table sys_resource_permission_rel
-- ----------------------------
ALTER TABLE "public"."sys_resource_permission_rel" ADD CONSTRAINT "uq_sys_resource_permission_rel_resource_permission" UNIQUE ("resource_id", "permission_key");

-- ----------------------------
-- Primary Key structure for table sys_resource_permission_rel
-- ----------------------------
ALTER TABLE "public"."sys_resource_permission_rel" ADD CONSTRAINT "pk_sys_resource_permission_rel" PRIMARY KEY ("id");

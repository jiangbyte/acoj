-- ----------------------------
-- Table structure for sys_resource_module
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_resource_module";
CREATE TABLE "public"."sys_resource_module" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
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
INSERT INTO "public"."sys_resource_module" ("id", "name", "code", "locale_key", "icon", "color", "sort", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('210001', '系统', 'system', 'resource.sys.title', 'icon-park-outline:setting-two', '#2563eb', 1, 'ENABLED', '系统内置资源模块', '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);

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
-- Records of sys_resource
-- ----------------------------
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200001', NULL, 'dashboard', '工作台', 'resource.dashboard.title', 'MENU', '210001', '/dashboard', '/dashboard/index.vue', NULL, 'icon-park-outline:analysis', NULL, 1, true, false, true, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200003', NULL, 'sys', '系统管理', 'resource.sys.title', 'CATALOG', '210001', '/sys', NULL, NULL, 'icon-park-outline:setting-two', NULL, 10, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200004', '200003', 'sys-dict', '字典管理', 'resource.sys.dict.title', 'MENU', '210001', '/sys/dict', '/sys/dict/index.vue', NULL, 'icon-park-outline:file-search', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200005', '200003', 'sys-banner', '展示图管理', 'resource.sys.banner.title', 'MENU', '210001', '/sys/banner', '/sys/banner/index.vue', NULL, 'icon-park-outline:ad-product', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" ("id", "parent_id", "code", "name", "locale_key", "resource_type", "module_id", "path", "component", "redirect", "icon", "href", "sort", "is_visible", "is_cache", "is_affix", "status", "description", "extra", "created_at", "created_by", "updated_at", "updated_by") VALUES ('200023', '200003', 'sys-file', '文件管理', 'resource.sys.file.title', 'MENU', '210001', '/sys/file', '/sys/file/index.vue', NULL, 'icon-park-outline:file-code', NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
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

-- ----------------------------
-- Records of sys_subject_resource_grant_rel
-- 超管角色（id=1）默认拥有全部资源
-- ----------------------------
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300001', 'ROLE', '1', '200001', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300003', 'ROLE', '1', '200003', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300004', 'ROLE', '1', '200004', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300005', 'ROLE', '1', '200005', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" ("id", "subject_type", "subject_id", "resource_id", "grant_mode", "effect", "status", "description", "expired_at", "created_at", "created_by", "updated_at", "updated_by") VALUES ('300023', 'ROLE', '1', '200023', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
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

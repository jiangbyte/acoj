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
  "module" varchar(64) COLLATE "pg_catalog"."default",
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
COMMENT ON COLUMN "public"."sys_resource"."resource_type" IS '资源类型';
COMMENT ON COLUMN "public"."sys_resource"."module" IS '所属模块';
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
INSERT INTO "public"."sys_resource" VALUES ('200001', NULL, 'dashboard', '工作台', 'MENU', 'system', '/dashboard', '/dashboard/index.vue', NULL, 'icon-park-outline:analysis', NULL, 1, true, false, true, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200002', NULL, 'demo', '示例页面', 'CATALOG', 'system', '/demo', NULL, NULL, 'icon-park-outline:application-one', NULL, 20, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200003', NULL, 'sys', '系统管理', 'CATALOG', 'system', '/sys', NULL, NULL, 'icon-park-outline:setting-two', NULL, 10, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200004', '200003', 'sys-dict', '字典管理', 'MENU', 'system', '/sys/dict', '/sys/dict/index.vue', NULL, 'icon-park-outline:file-search', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200005', '200003', 'sys-banner', '展示图管理', 'MENU', 'system', '/sys/banner', '/sys/banner/index.vue', NULL, 'icon-park-outline:ad-product', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200006', NULL, 'iam', '身份权限', 'CATALOG', 'system', '/iam', NULL, NULL, 'icon-park-outline:permissions', NULL, 15, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200007', '200006', 'iam-account', '账号管理', 'MENU', 'system', '/iam/account', '/iam/account/index.vue', NULL, 'icon-park-outline:people', NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200008', '200006', 'iam-dept', '部门管理', 'MENU', 'system', '/iam/dept', '/iam/dept/index.vue', NULL, 'icon-park-outline:tree-diagram', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200009', '200006', 'iam-group', '用户组管理', 'MENU', 'system', '/iam/group', '/iam/group/index.vue', NULL, 'icon-park-outline:group', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200010', '200006', 'iam-position', '岗位管理', 'MENU', 'system', '/iam/position', '/iam/position/index.vue', NULL, 'icon-park-outline:people-bottom', NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200011', '200006', 'iam-role', '角色管理', 'MENU', 'system', '/iam/role', '/iam/role/index.vue', NULL, 'icon-park-outline:peoples', NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200012', '200006', 'iam-resource', '资源管理', 'MENU', 'system', '/iam/resource', '/iam/resource/index.vue', NULL, 'icon-park-outline:all-application', NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200013', '200002', 'demo-overview', '示例总览', 'MENU', 'system', '/demo/overview', '/demo/overview/index.vue', NULL, 'icon-park-outline:dashboard-one', NULL, 1, true, true, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200014', '200002', 'demo-list', '列表示例', 'MENU', 'system', '/demo/list', '/demo/list/index.vue', NULL, 'icon-park-outline:list-view', NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200015', '200002', 'demo-form', '表单示例', 'MENU', 'system', '/demo/form', '/demo/form/index.vue', NULL, 'icon-park-outline:edit', NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200016', '200002', 'demo-chart', '图表示例', 'MENU', 'system', '/demo/chart', '/demo/chart/index.vue', NULL, 'icon-park-outline:chart-line', NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_resource" VALUES ('200017', '200002', 'demo-detail', '详情示例', 'PAGE', 'system', '/demo/detail', '/demo/detail/index.vue', NULL, 'icon-park-outline:doc-detail', NULL, 5, false, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);

-- ----------------------------
-- Records of sys_subject_resource_grant_rel
-- 超管角色（id=1）默认拥有全部资源
-- ----------------------------
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300001', 'ROLE', '1', '200001', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300002', 'ROLE', '1', '200002', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300003', 'ROLE', '1', '200003', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300004', 'ROLE', '1', '200004', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300005', 'ROLE', '1', '200005', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300006', 'ROLE', '1', '200006', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300007', 'ROLE', '1', '200007', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300008', 'ROLE', '1', '200008', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300009', 'ROLE', '1', '200009', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300010', 'ROLE', '1', '200010', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300011', 'ROLE', '1', '200011', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300012', 'ROLE', '1', '200012', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300013', 'ROLE', '1', '200013', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300014', 'ROLE', '1', '200014', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300015', 'ROLE', '1', '200015', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300016', 'ROLE', '1', '200016', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO "public"."sys_subject_resource_grant_rel" VALUES ('300017', 'ROLE', '1', '200017', 'CASCADE', 'ALLOW', 'ENABLED', '超管角色默认资源授权', NULL, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);

-- ----------------------------
-- Uniques structure for table sys_resource
-- ----------------------------
ALTER TABLE "public"."sys_resource" ADD CONSTRAINT "uq_sys_resource_code" UNIQUE ("code");

-- ----------------------------
-- Primary Key structure for table sys_resource
-- ----------------------------
ALTER TABLE "public"."sys_resource" ADD CONSTRAINT "pk_sys_resource" PRIMARY KEY ("id");

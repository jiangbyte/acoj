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

 Date: 24/07/2026 20:50:20
*/


-- ----------------------------
-- Table structure for sys_storage_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_storage_config";
CREATE TABLE "public"."sys_storage_config" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "provider" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "bucket" varchar(255) COLLATE "pg_catalog"."default",
  "endpoint" varchar(500) COLLATE "pg_catalog"."default",
  "access_key" varchar(255) COLLATE "pg_catalog"."default",
  "secret_key" varchar(255) COLLATE "pg_catalog"."default",
  "region" varchar(100) COLLATE "pg_catalog"."default",
  "use_ssl" bool NOT NULL,
  "base_url" varchar(500) COLLATE "pg_catalog"."default",
  "public_path" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "local_root" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
  "is_default" bool NOT NULL,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "sort_code" int4 NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."sys_storage_config"."id" IS '主键';
COMMENT ON COLUMN "public"."sys_storage_config"."name" IS '配置名称';
COMMENT ON COLUMN "public"."sys_storage_config"."provider" IS '存储服务商：local/minio/s3/oss';
COMMENT ON COLUMN "public"."sys_storage_config"."bucket" IS '存储桶';
COMMENT ON COLUMN "public"."sys_storage_config"."endpoint" IS '服务端点';
COMMENT ON COLUMN "public"."sys_storage_config"."access_key" IS '访问密钥 ID';
COMMENT ON COLUMN "public"."sys_storage_config"."secret_key" IS '访问密钥 Secret';
COMMENT ON COLUMN "public"."sys_storage_config"."region" IS '区域';
COMMENT ON COLUMN "public"."sys_storage_config"."use_ssl" IS '是否使用 SSL 连接';
COMMENT ON COLUMN "public"."sys_storage_config"."base_url" IS '自定义基础 URL';
COMMENT ON COLUMN "public"."sys_storage_config"."public_path" IS '公开访问路径';
COMMENT ON COLUMN "public"."sys_storage_config"."local_root" IS '本地存储根目录';
COMMENT ON COLUMN "public"."sys_storage_config"."is_default" IS '是否为当前启用的默认配置（互斥）';
COMMENT ON COLUMN "public"."sys_storage_config"."remark" IS '备注';
COMMENT ON COLUMN "public"."sys_storage_config"."sort_code" IS '排序码';
COMMENT ON COLUMN "public"."sys_storage_config"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."sys_storage_config"."created_by" IS '创建人';
COMMENT ON COLUMN "public"."sys_storage_config"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."sys_storage_config"."updated_by" IS '更新人';

-- ----------------------------
-- Records of sys_storage_config
-- ----------------------------
INSERT INTO "public"."sys_storage_config" VALUES ('200101', '默认配置', 'local', 'hei-fastapi', 'http://127.0.0.1:9000', 'admin', '123456', 'us-east-1', 'f', '', '/api/v1/files', 'storage', 'f', '', 0, '2026-07-24 06:49:01.609449+00', NULL, '2026-07-24 08:01:52.8031+00', NULL);
INSERT INTO "public"."sys_storage_config" VALUES ('200103', 'Amazon S3', 's3', 'my-bucket', '', '', '', 'us-east-1', 'f', '', '/api/v1/files', 'storage', 'f', 'Amazon Simple Storage Service', 20, '2026-07-24 07:02:10.322169+00', NULL, '2026-07-24 08:01:52.8031+00', NULL);
INSERT INTO "public"."sys_storage_config" VALUES ('200104', '阿里云 OSS', 'oss', 'my-bucket', 'https://oss-cn-hangzhou.aliyuncs.com', '', '', 'cn-hangzhou', 'f', '', '/api/v1/files', 'storage', 'f', '阿里云对象存储 OSS', 30, '2026-07-24 07:02:10.322169+00', NULL, '2026-07-24 08:01:52.8031+00', NULL);
INSERT INTO "public"."sys_storage_config" VALUES ('200102', 'MinIO', 'minio', 'vms', 'http://127.0.0.1:9000', 'admin', '123456789', '', 'f', 'http://127.0.0.1:9000/vms', '/api/v1/files', 'storage', 'f', 'MinIO 对象存储', 10, '2026-07-24 07:02:10.322169+00', NULL, '2026-07-24 08:01:52.8031+00', '1');

-- ----------------------------
-- Indexes structure for table sys_storage_config
-- ----------------------------
CREATE UNIQUE INDEX "uq_sys_storage_config_default" ON "public"."sys_storage_config" USING btree (
  "is_default" "pg_catalog"."bool_ops" ASC NULLS LAST
) WHERE is_default = true;

-- ----------------------------
-- Primary Key structure for table sys_storage_config
-- ----------------------------
ALTER TABLE "public"."sys_storage_config" ADD CONSTRAINT "pk_sys_storage_config" PRIMARY KEY ("id");

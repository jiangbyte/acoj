-- Codegen CRUD test table.
-- Suggested gen_type: CRUD
-- Main table: cg_test_activity
-- Main PK: id

DROP TABLE IF EXISTS public.cg_test_activity;

CREATE TABLE public.cg_test_activity (
    id varchar(64) PRIMARY KEY,
    code varchar(64) NOT NULL UNIQUE,
    name varchar(120) NOT NULL,
    category varchar(32),
    type varchar(32) NOT NULL,
    status varchar(32) NOT NULL DEFAULT 'ENABLED',
    cover_url varchar(512),
    description text,
    start_at timestamp with time zone NOT NULL,
    end_at timestamp with time zone,
    max_participants integer NOT NULL DEFAULT 0,
    price numeric(12, 2) NOT NULL DEFAULT 0,
    is_public boolean NOT NULL DEFAULT true,
    need_approval boolean NOT NULL DEFAULT false,
    rule_config jsonb NOT NULL DEFAULT '{}'::jsonb,
    extra jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by varchar(64),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_by varchar(64)
);

COMMENT ON TABLE public.cg_test_activity IS '代码生成测试-活动';
COMMENT ON COLUMN public.cg_test_activity.id IS '主键';
COMMENT ON COLUMN public.cg_test_activity.code IS '活动编码';
COMMENT ON COLUMN public.cg_test_activity.name IS '活动名称';
COMMENT ON COLUMN public.cg_test_activity.category IS '活动分类';
COMMENT ON COLUMN public.cg_test_activity.type IS '活动类型';
COMMENT ON COLUMN public.cg_test_activity.status IS '状态';
COMMENT ON COLUMN public.cg_test_activity.cover_url IS '封面地址';
COMMENT ON COLUMN public.cg_test_activity.description IS '活动描述';
COMMENT ON COLUMN public.cg_test_activity.start_at IS '开始时间';
COMMENT ON COLUMN public.cg_test_activity.end_at IS '结束时间';
COMMENT ON COLUMN public.cg_test_activity.max_participants IS '最大参与人数';
COMMENT ON COLUMN public.cg_test_activity.price IS '报名费用';
COMMENT ON COLUMN public.cg_test_activity.is_public IS '是否公开';
COMMENT ON COLUMN public.cg_test_activity.need_approval IS '是否需要审批';
COMMENT ON COLUMN public.cg_test_activity.rule_config IS '规则配置';
COMMENT ON COLUMN public.cg_test_activity.extra IS '扩展信息';
COMMENT ON COLUMN public.cg_test_activity.created_at IS '创建时间';
COMMENT ON COLUMN public.cg_test_activity.created_by IS '创建人';
COMMENT ON COLUMN public.cg_test_activity.updated_at IS '更新时间';
COMMENT ON COLUMN public.cg_test_activity.updated_by IS '更新人';

CREATE INDEX idx_cg_test_activity_status ON public.cg_test_activity (status);
CREATE INDEX idx_cg_test_activity_start_at ON public.cg_test_activity (start_at);

INSERT INTO public.cg_test_activity (
    id, code, name, category, type, status, cover_url, description,
    start_at, end_at, max_participants, price, is_public, need_approval,
    rule_config, extra, created_by, updated_by
) VALUES
(
    '900000000000000001',
    'ACT-BOOTCAMP',
    '暑期训练营',
    'TRAINING',
    'OFFLINE',
    'ENABLED',
    'https://example.com/activity/bootcamp.png',
    '覆盖文本域、时间、金额、开关、JSON 的 CRUD 测试数据。',
    '2026-07-19T01:00:00Z',
    '2026-07-19T09:00:00Z',
    120,
    199.00,
    true,
    false,
    '{"checkin": true, "limit": {"daily": 3}}'::jsonb,
    '{"tags": ["codegen", "crud"]}'::jsonb,
    '1',
    '1'
);

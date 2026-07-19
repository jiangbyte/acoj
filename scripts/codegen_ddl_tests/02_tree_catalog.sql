-- Codegen TREE test table.
-- Suggested gen_type: TREE
-- Main table: cg_test_catalog
-- Main PK: id
-- Tree parent field: parent_id
-- Tree label field: name

DROP TABLE IF EXISTS public.cg_test_catalog;

CREATE TABLE public.cg_test_catalog (
    id varchar(64) PRIMARY KEY,
    parent_id varchar(64),
    code varchar(64) NOT NULL UNIQUE,
    name varchar(120) NOT NULL,
    category varchar(32),
    status varchar(32) NOT NULL DEFAULT 'ENABLED',
    sort integer NOT NULL DEFAULT 0,
    is_visible boolean NOT NULL DEFAULT true,
    icon varchar(128),
    description text,
    extra jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by varchar(64),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_by varchar(64),
    CONSTRAINT fk_cg_test_catalog_parent FOREIGN KEY (parent_id) REFERENCES public.cg_test_catalog(id)
);

COMMENT ON TABLE public.cg_test_catalog IS '代码生成测试-树目录';
COMMENT ON COLUMN public.cg_test_catalog.id IS '主键';
COMMENT ON COLUMN public.cg_test_catalog.parent_id IS '父级ID';
COMMENT ON COLUMN public.cg_test_catalog.code IS '目录编码';
COMMENT ON COLUMN public.cg_test_catalog.name IS '目录名称';
COMMENT ON COLUMN public.cg_test_catalog.category IS '目录分类';
COMMENT ON COLUMN public.cg_test_catalog.status IS '状态';
COMMENT ON COLUMN public.cg_test_catalog.sort IS '排序';
COMMENT ON COLUMN public.cg_test_catalog.is_visible IS '是否显示';
COMMENT ON COLUMN public.cg_test_catalog.icon IS '图标';
COMMENT ON COLUMN public.cg_test_catalog.description IS '描述';
COMMENT ON COLUMN public.cg_test_catalog.extra IS '扩展信息';
COMMENT ON COLUMN public.cg_test_catalog.created_at IS '创建时间';
COMMENT ON COLUMN public.cg_test_catalog.created_by IS '创建人';
COMMENT ON COLUMN public.cg_test_catalog.updated_at IS '更新时间';
COMMENT ON COLUMN public.cg_test_catalog.updated_by IS '更新人';

CREATE INDEX idx_cg_test_catalog_parent ON public.cg_test_catalog (parent_id);
CREATE INDEX idx_cg_test_catalog_sort ON public.cg_test_catalog (sort);

INSERT INTO public.cg_test_catalog (
    id, parent_id, code, name, category, status, sort, is_visible, icon, description, extra, created_by, updated_by
) VALUES
('900000000000000101', NULL, 'ROOT', '根目录', 'SYSTEM', 'ENABLED', 1, true, 'folder', '一级节点', '{"level": 1}'::jsonb, '1', '1'),
('900000000000000102', '900000000000000101', 'CHILD-A', '子目录A', 'SYSTEM', 'ENABLED', 10, true, 'folder-open', '二级节点', '{"level": 2}'::jsonb, '1', '1'),
('900000000000000103', '900000000000000101', 'CHILD-B', '子目录B', 'BUSINESS', 'DISABLED', 20, false, 'folder-open', '二级节点', '{"level": 2}'::jsonb, '1', '1');

-- Codegen LEFT_TREE_TABLE test tables.
-- Suggested gen_type: LEFT_TREE_TABLE
-- Main table: cg_test_knowledge_category
-- Main PK: id
-- Tree parent field: parent_id
-- Tree label field: name
-- Sub table: cg_test_knowledge_doc
-- Sub PK: id
-- Sub foreign key: category_id

DROP TABLE IF EXISTS public.cg_test_knowledge_doc;
DROP TABLE IF EXISTS public.cg_test_knowledge_category;

CREATE TABLE public.cg_test_knowledge_category (
    id varchar(64) PRIMARY KEY,
    parent_id varchar(64),
    code varchar(64) NOT NULL UNIQUE,
    name varchar(120) NOT NULL,
    status varchar(32) NOT NULL DEFAULT 'ENABLED',
    sort integer NOT NULL DEFAULT 0,
    is_visible boolean NOT NULL DEFAULT true,
    description text,
    extra jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by varchar(64),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_by varchar(64),
    CONSTRAINT fk_cg_test_knowledge_category_parent FOREIGN KEY (parent_id) REFERENCES public.cg_test_knowledge_category(id)
);

CREATE TABLE public.cg_test_knowledge_doc (
    id varchar(64) PRIMARY KEY,
    category_id varchar(64) NOT NULL,
    code varchar(64) NOT NULL UNIQUE,
    title varchar(160) NOT NULL,
    type varchar(32) NOT NULL DEFAULT 'ARTICLE',
    status varchar(32) NOT NULL DEFAULT 'DRAFT',
    summary varchar(512),
    content text,
    author varchar(64),
    published_at timestamp with time zone,
    view_count integer NOT NULL DEFAULT 0,
    sort integer NOT NULL DEFAULT 0,
    is_top boolean NOT NULL DEFAULT false,
    settings jsonb NOT NULL DEFAULT '{}'::jsonb,
    extra jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by varchar(64),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_by varchar(64),
    CONSTRAINT fk_cg_test_knowledge_doc_category FOREIGN KEY (category_id) REFERENCES public.cg_test_knowledge_category(id)
);

COMMENT ON TABLE public.cg_test_knowledge_category IS '代码生成测试-知识分类';
COMMENT ON COLUMN public.cg_test_knowledge_category.id IS '主键';
COMMENT ON COLUMN public.cg_test_knowledge_category.parent_id IS '父级ID';
COMMENT ON COLUMN public.cg_test_knowledge_category.code IS '分类编码';
COMMENT ON COLUMN public.cg_test_knowledge_category.name IS '分类名称';
COMMENT ON COLUMN public.cg_test_knowledge_category.status IS '状态';
COMMENT ON COLUMN public.cg_test_knowledge_category.sort IS '排序';
COMMENT ON COLUMN public.cg_test_knowledge_category.is_visible IS '是否显示';
COMMENT ON COLUMN public.cg_test_knowledge_category.description IS '描述';
COMMENT ON COLUMN public.cg_test_knowledge_category.extra IS '扩展信息';
COMMENT ON COLUMN public.cg_test_knowledge_category.created_at IS '创建时间';
COMMENT ON COLUMN public.cg_test_knowledge_category.created_by IS '创建人';
COMMENT ON COLUMN public.cg_test_knowledge_category.updated_at IS '更新时间';
COMMENT ON COLUMN public.cg_test_knowledge_category.updated_by IS '更新人';

COMMENT ON TABLE public.cg_test_knowledge_doc IS '代码生成测试-知识文档';
COMMENT ON COLUMN public.cg_test_knowledge_doc.id IS '主键';
COMMENT ON COLUMN public.cg_test_knowledge_doc.category_id IS '分类ID';
COMMENT ON COLUMN public.cg_test_knowledge_doc.code IS '文档编码';
COMMENT ON COLUMN public.cg_test_knowledge_doc.title IS '文档标题';
COMMENT ON COLUMN public.cg_test_knowledge_doc.type IS '文档类型';
COMMENT ON COLUMN public.cg_test_knowledge_doc.status IS '状态';
COMMENT ON COLUMN public.cg_test_knowledge_doc.summary IS '摘要';
COMMENT ON COLUMN public.cg_test_knowledge_doc.content IS '正文内容';
COMMENT ON COLUMN public.cg_test_knowledge_doc.author IS '作者';
COMMENT ON COLUMN public.cg_test_knowledge_doc.published_at IS '发布时间';
COMMENT ON COLUMN public.cg_test_knowledge_doc.view_count IS '浏览次数';
COMMENT ON COLUMN public.cg_test_knowledge_doc.sort IS '排序';
COMMENT ON COLUMN public.cg_test_knowledge_doc.is_top IS '是否置顶';
COMMENT ON COLUMN public.cg_test_knowledge_doc.settings IS '展示设置';
COMMENT ON COLUMN public.cg_test_knowledge_doc.extra IS '扩展信息';
COMMENT ON COLUMN public.cg_test_knowledge_doc.created_at IS '创建时间';
COMMENT ON COLUMN public.cg_test_knowledge_doc.created_by IS '创建人';
COMMENT ON COLUMN public.cg_test_knowledge_doc.updated_at IS '更新时间';
COMMENT ON COLUMN public.cg_test_knowledge_doc.updated_by IS '更新人';

CREATE INDEX idx_cg_test_knowledge_category_parent ON public.cg_test_knowledge_category (parent_id);
CREATE INDEX idx_cg_test_knowledge_doc_category ON public.cg_test_knowledge_doc (category_id);
CREATE INDEX idx_cg_test_knowledge_doc_status ON public.cg_test_knowledge_doc (status);

INSERT INTO public.cg_test_knowledge_category (
    id, parent_id, code, name, status, sort, is_visible, description, extra, created_by, updated_by
) VALUES
('900000000000000301', NULL, 'KB', '知识库', 'ENABLED', 1, true, '根分类', '{"level": 1}'::jsonb, '1', '1'),
('900000000000000302', '900000000000000301', 'KB-DEV', '研发文档', 'ENABLED', 10, true, '研发相关文档', '{"level": 2}'::jsonb, '1', '1');

INSERT INTO public.cg_test_knowledge_doc (
    id, category_id, code, title, type, status, summary, content, author, published_at,
    view_count, sort, is_top, settings, extra, created_by, updated_by
) VALUES (
    '900000000000000311',
    '900000000000000302',
    'DOC-CODEGEN-001',
    '代码生成测试文档',
    'ARTICLE',
    'PUBLISHED',
    '用于测试左树右表生成。',
    '正文内容用于触发 textarea。',
    'tester',
    '2026-07-19T01:19:18Z',
    88,
    1,
    true,
    '{"showToc": true, "theme": "default"}'::jsonb,
    '{"tags": ["tree", "table"]}'::jsonb,
    '1',
    '1'
);

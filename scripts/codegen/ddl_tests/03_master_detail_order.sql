-- Codegen MASTER_DETAIL test tables.
-- Suggested gen_type: MASTER_DETAIL
-- Main table: cg_test_order
-- Main PK: id
-- Sub table: cg_test_order_item
-- Sub PK: id
-- Sub foreign key: order_id

DROP TABLE IF EXISTS public.cg_test_order_item;
DROP TABLE IF EXISTS public.cg_test_order;

CREATE TABLE public.cg_test_order (
    id varchar(64) PRIMARY KEY,
    order_no varchar(64) NOT NULL UNIQUE,
    name varchar(120) NOT NULL,
    customer_name varchar(120) NOT NULL,
    customer_phone varchar(32),
    status varchar(32) NOT NULL DEFAULT 'PENDING',
    type varchar(32) NOT NULL DEFAULT 'NORMAL',
    ordered_at timestamp with time zone NOT NULL,
    paid_at timestamp with time zone,
    total_amount numeric(12, 2) NOT NULL DEFAULT 0,
    item_count integer NOT NULL DEFAULT 0,
    need_invoice boolean NOT NULL DEFAULT false,
    invoice_config jsonb NOT NULL DEFAULT '{}'::jsonb,
    remark text,
    extra jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by varchar(64),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_by varchar(64)
);

CREATE TABLE public.cg_test_order_item (
    id varchar(64) PRIMARY KEY,
    order_id varchar(64) NOT NULL,
    sku_code varchar(64) NOT NULL,
    name varchar(120) NOT NULL,
    category varchar(32),
    status varchar(32) NOT NULL DEFAULT 'ENABLED',
    quantity integer NOT NULL DEFAULT 1,
    unit_price numeric(12, 2) NOT NULL DEFAULT 0,
    shipped_at timestamp with time zone,
    is_gift boolean NOT NULL DEFAULT false,
    item_config jsonb NOT NULL DEFAULT '{}'::jsonb,
    remark text,
    extra jsonb,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by varchar(64),
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_by varchar(64),
    CONSTRAINT fk_cg_test_order_item_order FOREIGN KEY (order_id) REFERENCES public.cg_test_order(id)
);

COMMENT ON TABLE public.cg_test_order IS '代码生成测试-订单';
COMMENT ON COLUMN public.cg_test_order.id IS '主键';
COMMENT ON COLUMN public.cg_test_order.order_no IS '订单号';
COMMENT ON COLUMN public.cg_test_order.name IS '订单名称';
COMMENT ON COLUMN public.cg_test_order.customer_name IS '客户名称';
COMMENT ON COLUMN public.cg_test_order.customer_phone IS '客户手机号';
COMMENT ON COLUMN public.cg_test_order.status IS '状态';
COMMENT ON COLUMN public.cg_test_order.type IS '订单类型';
COMMENT ON COLUMN public.cg_test_order.ordered_at IS '下单时间';
COMMENT ON COLUMN public.cg_test_order.paid_at IS '支付时间';
COMMENT ON COLUMN public.cg_test_order.total_amount IS '订单金额';
COMMENT ON COLUMN public.cg_test_order.item_count IS '商品数量';
COMMENT ON COLUMN public.cg_test_order.need_invoice IS '是否开票';
COMMENT ON COLUMN public.cg_test_order.invoice_config IS '发票配置';
COMMENT ON COLUMN public.cg_test_order.remark IS '备注';
COMMENT ON COLUMN public.cg_test_order.extra IS '扩展信息';
COMMENT ON COLUMN public.cg_test_order.created_at IS '创建时间';
COMMENT ON COLUMN public.cg_test_order.created_by IS '创建人';
COMMENT ON COLUMN public.cg_test_order.updated_at IS '更新时间';
COMMENT ON COLUMN public.cg_test_order.updated_by IS '更新人';

COMMENT ON TABLE public.cg_test_order_item IS '代码生成测试-订单明细';
COMMENT ON COLUMN public.cg_test_order_item.id IS '主键';
COMMENT ON COLUMN public.cg_test_order_item.order_id IS '订单ID';
COMMENT ON COLUMN public.cg_test_order_item.sku_code IS 'SKU编码';
COMMENT ON COLUMN public.cg_test_order_item.name IS '商品名称';
COMMENT ON COLUMN public.cg_test_order_item.category IS '商品分类';
COMMENT ON COLUMN public.cg_test_order_item.status IS '状态';
COMMENT ON COLUMN public.cg_test_order_item.quantity IS '数量';
COMMENT ON COLUMN public.cg_test_order_item.unit_price IS '单价';
COMMENT ON COLUMN public.cg_test_order_item.shipped_at IS '发货时间';
COMMENT ON COLUMN public.cg_test_order_item.is_gift IS '是否赠品';
COMMENT ON COLUMN public.cg_test_order_item.item_config IS '明细配置';
COMMENT ON COLUMN public.cg_test_order_item.remark IS '备注';
COMMENT ON COLUMN public.cg_test_order_item.extra IS '扩展信息';
COMMENT ON COLUMN public.cg_test_order_item.created_at IS '创建时间';
COMMENT ON COLUMN public.cg_test_order_item.created_by IS '创建人';
COMMENT ON COLUMN public.cg_test_order_item.updated_at IS '更新时间';
COMMENT ON COLUMN public.cg_test_order_item.updated_by IS '更新人';

CREATE INDEX idx_cg_test_order_status ON public.cg_test_order (status);
CREATE INDEX idx_cg_test_order_ordered_at ON public.cg_test_order (ordered_at);
CREATE INDEX idx_cg_test_order_item_order ON public.cg_test_order_item (order_id);

INSERT INTO public.cg_test_order (
    id, order_no, name, customer_name, customer_phone, status, type, ordered_at, paid_at,
    total_amount, item_count, need_invoice, invoice_config, remark, extra, created_by, updated_by
) VALUES (
    '900000000000000201',
    'CG-ORDER-001',
    '测试订单001',
    '张三',
    '13800000000',
    'PAID',
    'NORMAL',
    '2026-07-19T01:10:00Z',
    '2026-07-19T01:20:00Z',
    399.00,
    2,
    true,
    '{"title": "张三", "taxNo": "91300000000000000X"}'::jsonb,
    '主子表生成测试订单',
    '{"source": "codegen"}'::jsonb,
    '1',
    '1'
);

INSERT INTO public.cg_test_order_item (
    id, order_id, sku_code, name, category, status, quantity, unit_price, shipped_at,
    is_gift, item_config, remark, extra, created_by, updated_by
) VALUES
('900000000000000211', '900000000000000201', 'SKU-001', '测试商品A', 'BOOK', 'ENABLED', 1, 199.00, NULL, false, '{"color": "red"}'::jsonb, '普通明细', '{"line": 1}'::jsonb, '1', '1'),
('900000000000000212', '900000000000000201', 'SKU-002', '测试商品B', 'COURSE', 'ENABLED', 1, 200.00, '2026-07-19T02:30:00Z', true, '{"duration": 30}'::jsonb, '赠品明细', '{"line": 2}'::jsonb, '1', '1');

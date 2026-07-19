--
-- PostgreSQL database dump
--

\restrict KtbxZA41zp5p8LP4uaMVhCTCfd3Jl9czpMszF79P2P9FSC6TAhhoRmRggG8qaMu

-- Dumped from database version 15.17 (Debian 15.17-1.pgdg12+1)
-- Dumped by pg_dump version 18.4

-- Started on 2026-07-19 15:52:05

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE IF EXISTS hei_fastapi;
--
-- TOC entry 3721 (class 1262 OID 16388)
-- Name: hei_fastapi; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE hei_fastapi WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE hei_fastapi OWNER TO postgres;

\unrestrict KtbxZA41zp5p8LP4uaMVhCTCfd3Jl9czpMszF79P2P9FSC6TAhhoRmRggG8qaMu
\connect hei_fastapi
\restrict KtbxZA41zp5p8LP4uaMVhCTCfd3Jl9czpMszF79P2P9FSC6TAhhoRmRggG8qaMu

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5 (class 2615 OID 17405)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 3722 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS '';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 215 (class 1259 OID 28408)
-- Name: admin_user_profile; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.admin_user_profile (
    account_id character varying(64) NOT NULL,
    name character varying(64),
    nickname character varying(64),
    avatar text,
    signature text,
    phone character varying(32),
    email character varying(128),
    title character varying(64),
    employee_no character varying(64),
    remark text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.admin_user_profile OWNER TO postgres;

--
-- TOC entry 3724 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.account_id IS '账户ID';


--
-- TOC entry 3725 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.name IS '姓名';


--
-- TOC entry 3726 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.nickname; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.nickname IS '昵称';


--
-- TOC entry 3727 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.avatar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.avatar IS '头像';


--
-- TOC entry 3728 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.signature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.signature IS '个性签名';


--
-- TOC entry 3729 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.phone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.phone IS '手机号';


--
-- TOC entry 3730 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.email IS '邮箱';


--
-- TOC entry 3731 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.title IS '岗位头衔';


--
-- TOC entry 3732 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.employee_no; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.employee_no IS '员工编号';


--
-- TOC entry 3733 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.remark IS '备注';


--
-- TOC entry 3734 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.created_at IS '创建时间';


--
-- TOC entry 3735 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.created_by IS '创建人';


--
-- TOC entry 3736 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.updated_at IS '更新时间';


--
-- TOC entry 3737 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN admin_user_profile.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.updated_by IS '更新人';


--
-- TOC entry 214 (class 1259 OID 28403)
-- Name: alembic_version; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alembic_version (
    version_num character varying(32) NOT NULL
);


ALTER TABLE public.alembic_version OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 28417)
-- Name: cg_test_activity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cg_test_activity (
    id character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(120) NOT NULL,
    category character varying(32),
    type character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    cover_url character varying(512),
    description text,
    start_at timestamp with time zone NOT NULL,
    end_at timestamp with time zone,
    max_participants integer NOT NULL,
    price numeric NOT NULL,
    is_public boolean NOT NULL,
    need_approval boolean NOT NULL,
    rule_config json NOT NULL,
    extra json,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.cg_test_activity OWNER TO postgres;

--
-- TOC entry 3738 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.id IS '主键';


--
-- TOC entry 3739 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.code IS '活动编码';


--
-- TOC entry 3740 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.name IS '活动名称';


--
-- TOC entry 3741 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.category IS '活动分类';


--
-- TOC entry 3742 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.type IS '活动类型';


--
-- TOC entry 3743 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.status IS '状态';


--
-- TOC entry 3744 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.cover_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.cover_url IS '封面地址';


--
-- TOC entry 3745 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.description IS '活动描述';


--
-- TOC entry 3746 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.start_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.start_at IS '开始时间';


--
-- TOC entry 3747 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.end_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.end_at IS '结束时间';


--
-- TOC entry 3748 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.max_participants; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.max_participants IS '最大参与人数';


--
-- TOC entry 3749 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.price; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.price IS '报名费用';


--
-- TOC entry 3750 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.is_public; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.is_public IS '是否公开';


--
-- TOC entry 3751 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.need_approval; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.need_approval IS '是否需要审批';


--
-- TOC entry 3752 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.rule_config; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.rule_config IS '规则配置';


--
-- TOC entry 3753 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.extra IS '扩展信息';


--
-- TOC entry 3754 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.created_at IS '创建时间';


--
-- TOC entry 3755 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.created_by IS '创建人';


--
-- TOC entry 3756 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.updated_at IS '更新时间';


--
-- TOC entry 3757 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN cg_test_activity.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_activity.updated_by IS '更新人';


--
-- TOC entry 217 (class 1259 OID 28426)
-- Name: cg_test_catalog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cg_test_catalog (
    id character varying(64) NOT NULL,
    parent_id character varying(64),
    code character varying(64) NOT NULL,
    name character varying(120) NOT NULL,
    category character varying(32),
    status character varying(32) NOT NULL,
    sort integer NOT NULL,
    is_visible boolean NOT NULL,
    icon character varying(128),
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.cg_test_catalog OWNER TO postgres;

--
-- TOC entry 3758 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.id IS '主键';


--
-- TOC entry 3759 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.parent_id IS '父级ID';


--
-- TOC entry 3760 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.code IS '目录编码';


--
-- TOC entry 3761 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.name IS '目录名称';


--
-- TOC entry 3762 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.category IS '目录分类';


--
-- TOC entry 3763 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.status IS '状态';


--
-- TOC entry 3764 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.sort IS '排序';


--
-- TOC entry 3765 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.is_visible; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.is_visible IS '是否显示';


--
-- TOC entry 3766 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.icon IS '图标';


--
-- TOC entry 3767 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.description IS '描述';


--
-- TOC entry 3768 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.extra IS '扩展信息';


--
-- TOC entry 3769 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.created_at IS '创建时间';


--
-- TOC entry 3770 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.created_by IS '创建人';


--
-- TOC entry 3771 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.updated_at IS '更新时间';


--
-- TOC entry 3772 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN cg_test_catalog.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_catalog.updated_by IS '更新人';


--
-- TOC entry 218 (class 1259 OID 28435)
-- Name: cg_test_knowledge_category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cg_test_knowledge_category (
    id character varying(64) NOT NULL,
    parent_id character varying(64),
    code character varying(64) NOT NULL,
    name character varying(120) NOT NULL,
    status character varying(32) NOT NULL,
    sort integer NOT NULL,
    is_visible boolean NOT NULL,
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.cg_test_knowledge_category OWNER TO postgres;

--
-- TOC entry 3773 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.id IS '主键';


--
-- TOC entry 3774 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.parent_id IS '父级ID';


--
-- TOC entry 3775 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.code IS '分类编码';


--
-- TOC entry 3776 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.name IS '分类名称';


--
-- TOC entry 3777 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.status IS '状态';


--
-- TOC entry 3778 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.sort IS '排序';


--
-- TOC entry 3779 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.is_visible; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.is_visible IS '是否显示';


--
-- TOC entry 3780 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.description IS '描述';


--
-- TOC entry 3781 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.extra IS '扩展信息';


--
-- TOC entry 3782 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.created_at IS '创建时间';


--
-- TOC entry 3783 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.created_by IS '创建人';


--
-- TOC entry 3784 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.updated_at IS '更新时间';


--
-- TOC entry 3785 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN cg_test_knowledge_category.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_category.updated_by IS '更新人';


--
-- TOC entry 219 (class 1259 OID 28444)
-- Name: cg_test_knowledge_doc; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cg_test_knowledge_doc (
    id character varying(64) NOT NULL,
    category_id character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    title character varying(160) NOT NULL,
    type character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    summary character varying(512),
    content text,
    author character varying(64),
    published_at timestamp with time zone,
    view_count integer NOT NULL,
    sort integer NOT NULL,
    is_top boolean NOT NULL,
    settings json NOT NULL,
    extra json,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.cg_test_knowledge_doc OWNER TO postgres;

--
-- TOC entry 3786 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.id IS '主键';


--
-- TOC entry 3787 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.category_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.category_id IS '分类ID';


--
-- TOC entry 3788 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.code IS '文档编码';


--
-- TOC entry 3789 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.title IS '文档标题';


--
-- TOC entry 3790 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.type IS '文档类型';


--
-- TOC entry 3791 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.status IS '状态';


--
-- TOC entry 3792 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.summary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.summary IS '摘要';


--
-- TOC entry 3793 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.content IS '正文内容';


--
-- TOC entry 3794 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.author; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.author IS '作者';


--
-- TOC entry 3795 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.published_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.published_at IS '发布时间';


--
-- TOC entry 3796 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.view_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.view_count IS '浏览次数';


--
-- TOC entry 3797 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.sort IS '排序';


--
-- TOC entry 3798 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.is_top; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.is_top IS '是否置顶';


--
-- TOC entry 3799 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.settings; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.settings IS '展示设置';


--
-- TOC entry 3800 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.extra IS '扩展信息';


--
-- TOC entry 3801 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.created_at IS '创建时间';


--
-- TOC entry 3802 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.created_by IS '创建人';


--
-- TOC entry 3803 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.updated_at IS '更新时间';


--
-- TOC entry 3804 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN cg_test_knowledge_doc.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_knowledge_doc.updated_by IS '更新人';


--
-- TOC entry 220 (class 1259 OID 28453)
-- Name: cg_test_order; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cg_test_order (
    id character varying(64) NOT NULL,
    order_no character varying(64) NOT NULL,
    name character varying(120) NOT NULL,
    customer_name character varying(120) NOT NULL,
    customer_phone character varying(32),
    status character varying(32) NOT NULL,
    type character varying(32) NOT NULL,
    ordered_at timestamp with time zone NOT NULL,
    paid_at timestamp with time zone,
    total_amount numeric NOT NULL,
    item_count integer NOT NULL,
    need_invoice boolean NOT NULL,
    invoice_config json NOT NULL,
    remark text,
    extra json,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.cg_test_order OWNER TO postgres;

--
-- TOC entry 3805 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.id IS '主键';


--
-- TOC entry 3806 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.order_no; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.order_no IS '订单号';


--
-- TOC entry 3807 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.name IS '订单名称';


--
-- TOC entry 3808 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.customer_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.customer_name IS '客户名称';


--
-- TOC entry 3809 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.customer_phone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.customer_phone IS '客户手机号';


--
-- TOC entry 3810 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.status IS '状态';


--
-- TOC entry 3811 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.type IS '订单类型';


--
-- TOC entry 3812 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.ordered_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.ordered_at IS '下单时间';


--
-- TOC entry 3813 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.paid_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.paid_at IS '支付时间';


--
-- TOC entry 3814 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.total_amount; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.total_amount IS '订单金额';


--
-- TOC entry 3815 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.item_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.item_count IS '商品数量';


--
-- TOC entry 3816 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.need_invoice; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.need_invoice IS '是否开票';


--
-- TOC entry 3817 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.invoice_config; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.invoice_config IS '发票配置';


--
-- TOC entry 3818 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.remark IS '备注';


--
-- TOC entry 3819 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.extra IS '扩展信息';


--
-- TOC entry 3820 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.created_at IS '创建时间';


--
-- TOC entry 3821 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.created_by IS '创建人';


--
-- TOC entry 3822 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.updated_at IS '更新时间';


--
-- TOC entry 3823 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN cg_test_order.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order.updated_by IS '更新人';


--
-- TOC entry 221 (class 1259 OID 28462)
-- Name: cg_test_order_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cg_test_order_item (
    id character varying(64) NOT NULL,
    order_id character varying(64) NOT NULL,
    sku_code character varying(64) NOT NULL,
    name character varying(120) NOT NULL,
    category character varying(32),
    status character varying(32) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric NOT NULL,
    shipped_at timestamp with time zone,
    is_gift boolean NOT NULL,
    item_config json NOT NULL,
    remark text,
    extra json,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.cg_test_order_item OWNER TO postgres;

--
-- TOC entry 3824 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.id IS '主键';


--
-- TOC entry 3825 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.order_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.order_id IS '订单ID';


--
-- TOC entry 3826 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.sku_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.sku_code IS 'SKU编码';


--
-- TOC entry 3827 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.name IS '商品名称';


--
-- TOC entry 3828 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.category IS '商品分类';


--
-- TOC entry 3829 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.status IS '状态';


--
-- TOC entry 3830 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.quantity IS '数量';


--
-- TOC entry 3831 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.unit_price; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.unit_price IS '单价';


--
-- TOC entry 3832 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.shipped_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.shipped_at IS '发货时间';


--
-- TOC entry 3833 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.is_gift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.is_gift IS '是否赠品';


--
-- TOC entry 3834 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.item_config; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.item_config IS '明细配置';


--
-- TOC entry 3835 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.remark IS '备注';


--
-- TOC entry 3836 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.extra IS '扩展信息';


--
-- TOC entry 3837 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.created_at IS '创建时间';


--
-- TOC entry 3838 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.created_by IS '创建人';


--
-- TOC entry 3839 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.updated_at IS '更新时间';


--
-- TOC entry 3840 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN cg_test_order_item.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cg_test_order_item.updated_by IS '更新人';


--
-- TOC entry 222 (class 1259 OID 28471)
-- Name: msg_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_group (
    id character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    owner_account_type character varying(32),
    owner_account_id character varying(64),
    avatar character varying(500),
    status character varying(32) NOT NULL,
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.msg_group OWNER TO postgres;

--
-- TOC entry 3841 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.id IS '主键';


--
-- TOC entry 3842 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.name IS '群组名称';


--
-- TOC entry 3843 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.owner_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.owner_account_type IS '群主账户类型';


--
-- TOC entry 3844 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.owner_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.owner_account_id IS '群主账户ID';


--
-- TOC entry 3845 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.avatar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.avatar IS '群头像';


--
-- TOC entry 3846 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.status IS '状态';


--
-- TOC entry 3847 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.description IS '描述';


--
-- TOC entry 3848 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.extra IS '扩展信息';


--
-- TOC entry 3849 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.created_at IS '创建时间';


--
-- TOC entry 3850 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.created_by IS '创建人';


--
-- TOC entry 3851 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.updated_at IS '更新时间';


--
-- TOC entry 3852 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN msg_group.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.updated_by IS '更新人';


--
-- TOC entry 223 (class 1259 OID 28480)
-- Name: msg_group_member; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_group_member (
    id character varying(64) NOT NULL,
    group_id character varying(64) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_id character varying(64) NOT NULL,
    nickname character varying(64),
    is_muted boolean NOT NULL,
    joined_at timestamp with time zone NOT NULL,
    left_at timestamp with time zone
);


ALTER TABLE public.msg_group_member OWNER TO postgres;

--
-- TOC entry 3853 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.id IS '主键';


--
-- TOC entry 3854 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.group_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.group_id IS '群组ID';


--
-- TOC entry 3855 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.account_type IS '账户类型';


--
-- TOC entry 3856 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.account_id IS '账户ID';


--
-- TOC entry 3857 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.nickname; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.nickname IS '群昵称';


--
-- TOC entry 3858 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.is_muted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.is_muted IS '是否免打扰';


--
-- TOC entry 3859 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.joined_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.joined_at IS '加入时间';


--
-- TOC entry 3860 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN msg_group_member.left_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.left_at IS '退出时间';


--
-- TOC entry 224 (class 1259 OID 28488)
-- Name: msg_message; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_message (
    id character varying(64) NOT NULL,
    thread_id character varying(64) NOT NULL,
    parent_id character varying(64),
    sender_type character varying(32) NOT NULL,
    sender_account_type character varying(32),
    sender_account_id character varying(64),
    sender_name character varying(128),
    content text NOT NULL,
    content_type character varying(32) NOT NULL,
    reply_count integer NOT NULL,
    is_revoked boolean NOT NULL,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.msg_message OWNER TO postgres;

--
-- TOC entry 3861 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.id IS '主键';


--
-- TOC entry 3862 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.thread_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.thread_id IS '会话ID';


--
-- TOC entry 3863 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.parent_id IS '回复消息ID';


--
-- TOC entry 3864 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.sender_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_type IS '发送方类型';


--
-- TOC entry 3865 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.sender_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_account_type IS '发送账户类型';


--
-- TOC entry 3866 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.sender_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_account_id IS '发送账户ID';


--
-- TOC entry 3867 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.sender_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_name IS '发送方快照名称';


--
-- TOC entry 3868 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.content IS '内容';


--
-- TOC entry 3869 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.content_type IS '内容格式';


--
-- TOC entry 3870 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.reply_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.reply_count IS '回复数';


--
-- TOC entry 3871 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.is_revoked; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.is_revoked IS '是否撤回';


--
-- TOC entry 3872 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.extra IS '扩展信息';


--
-- TOC entry 3873 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.created_at IS '创建时间';


--
-- TOC entry 3874 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.created_by IS '创建人';


--
-- TOC entry 3875 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.updated_at IS '更新时间';


--
-- TOC entry 3876 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN msg_message.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.updated_by IS '更新人';


--
-- TOC entry 225 (class 1259 OID 28499)
-- Name: msg_message_attachment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_message_attachment (
    id character varying(64) NOT NULL,
    message_id character varying(64) NOT NULL,
    name character varying(255) NOT NULL,
    url character varying(1024) NOT NULL,
    content_type character varying(128),
    size bigint,
    sort integer NOT NULL,
    extra json NOT NULL
);


ALTER TABLE public.msg_message_attachment OWNER TO postgres;

--
-- TOC entry 3877 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.id IS '主键';


--
-- TOC entry 3878 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.message_id IS '消息ID';


--
-- TOC entry 3879 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.name IS '文件名';


--
-- TOC entry 3880 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.url IS '文件地址';


--
-- TOC entry 3881 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.content_type IS '文件类型';


--
-- TOC entry 3882 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.size; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.size IS '文件大小';


--
-- TOC entry 3883 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.sort IS '排序';


--
-- TOC entry 3884 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN msg_message_attachment.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.extra IS '扩展信息';


--
-- TOC entry 226 (class 1259 OID 28507)
-- Name: msg_message_reaction; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_message_reaction (
    id character varying(64) NOT NULL,
    message_id character varying(64) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_id character varying(64) NOT NULL,
    reaction character varying(64) NOT NULL,
    created_at timestamp with time zone NOT NULL
);


ALTER TABLE public.msg_message_reaction OWNER TO postgres;

--
-- TOC entry 3885 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN msg_message_reaction.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.id IS '主键';


--
-- TOC entry 3886 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN msg_message_reaction.message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.message_id IS '消息ID';


--
-- TOC entry 3887 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN msg_message_reaction.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.account_type IS '账户类型';


--
-- TOC entry 3888 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN msg_message_reaction.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.account_id IS '账户ID';


--
-- TOC entry 3889 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN msg_message_reaction.reaction; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.reaction IS '反应';


--
-- TOC entry 3890 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN msg_message_reaction.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.created_at IS '创建时间';


--
-- TOC entry 227 (class 1259 OID 28515)
-- Name: msg_message_receipt; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_message_receipt (
    id character varying(64) NOT NULL,
    message_id character varying(64) NOT NULL,
    thread_id character varying(64) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_id character varying(64) NOT NULL,
    read_at timestamp with time zone
);


ALTER TABLE public.msg_message_receipt OWNER TO postgres;

--
-- TOC entry 3891 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN msg_message_receipt.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.id IS '主键';


--
-- TOC entry 3892 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN msg_message_receipt.message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.message_id IS '消息ID';


--
-- TOC entry 3893 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN msg_message_receipt.thread_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.thread_id IS '会话ID';


--
-- TOC entry 3894 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN msg_message_receipt.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.account_type IS '账户类型';


--
-- TOC entry 3895 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN msg_message_receipt.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.account_id IS '账户ID';


--
-- TOC entry 3896 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN msg_message_receipt.read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.read_at IS '阅读时间';


--
-- TOC entry 228 (class 1259 OID 28523)
-- Name: msg_notification; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_notification (
    id character varying(64) NOT NULL,
    title character varying(255) NOT NULL,
    content text NOT NULL,
    content_type character varying(32) NOT NULL,
    severity character varying(32) NOT NULL,
    target_scope character varying(32) NOT NULL,
    target_account_type character varying(32),
    target_account_id character varying(64),
    sender_account_type character varying(32),
    sender_account_id character varying(64),
    status character varying(32) NOT NULL,
    publish_at timestamp with time zone,
    revoked_at timestamp with time zone,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.msg_notification OWNER TO postgres;

--
-- TOC entry 3897 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.id IS '主键';


--
-- TOC entry 3898 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.title IS '标题';


--
-- TOC entry 3899 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.content IS '内容';


--
-- TOC entry 3900 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.content_type IS '内容格式';


--
-- TOC entry 3901 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.severity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.severity IS '等级';


--
-- TOC entry 3902 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.target_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.target_scope IS '目标范围';


--
-- TOC entry 3903 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.target_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.target_account_type IS '目标账户类型';


--
-- TOC entry 3904 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.target_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.target_account_id IS '目标账户ID';


--
-- TOC entry 3905 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.sender_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.sender_account_type IS '发送账户类型';


--
-- TOC entry 3906 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.sender_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.sender_account_id IS '发送账户ID';


--
-- TOC entry 3907 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.status IS '状态';


--
-- TOC entry 3908 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.publish_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.publish_at IS '发布时间';


--
-- TOC entry 3909 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.revoked_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.revoked_at IS '撤回时间';


--
-- TOC entry 3910 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.extra IS '扩展信息';


--
-- TOC entry 3911 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.created_at IS '创建时间';


--
-- TOC entry 3912 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.created_by IS '创建人';


--
-- TOC entry 3913 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.updated_at IS '更新时间';


--
-- TOC entry 3914 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN msg_notification.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.updated_by IS '更新人';


--
-- TOC entry 229 (class 1259 OID 28534)
-- Name: msg_notification_read; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_notification_read (
    id character varying(64) NOT NULL,
    notification_id character varying(64) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_id character varying(64) NOT NULL,
    read_at timestamp with time zone NOT NULL
);


ALTER TABLE public.msg_notification_read OWNER TO postgres;

--
-- TOC entry 3915 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN msg_notification_read.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.id IS '主键';


--
-- TOC entry 3916 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN msg_notification_read.notification_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.notification_id IS '通知ID';


--
-- TOC entry 3917 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN msg_notification_read.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.account_type IS '账户类型';


--
-- TOC entry 3918 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN msg_notification_read.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.account_id IS '账户ID';


--
-- TOC entry 3919 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN msg_notification_read.read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.read_at IS '阅读时间';


--
-- TOC entry 230 (class 1259 OID 28542)
-- Name: msg_thread; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_thread (
    id character varying(64) NOT NULL,
    thread_type character varying(32) NOT NULL,
    title character varying(255),
    group_id character varying(64),
    created_account_type character varying(32),
    created_account_id character varying(64),
    status character varying(32) NOT NULL,
    last_message_id character varying(64),
    last_message_at timestamp with time zone,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.msg_thread OWNER TO postgres;

--
-- TOC entry 3920 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.id IS '主键';


--
-- TOC entry 3921 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.thread_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.thread_type IS '会话类型';


--
-- TOC entry 3922 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.title IS '会话标题';


--
-- TOC entry 3923 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.group_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.group_id IS '消息群组ID';


--
-- TOC entry 3924 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.created_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_account_type IS '创建账户类型';


--
-- TOC entry 3925 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.created_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_account_id IS '创建账户ID';


--
-- TOC entry 3926 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.status IS '状态';


--
-- TOC entry 3927 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.last_message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.last_message_id IS '最后消息ID';


--
-- TOC entry 3928 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.last_message_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.last_message_at IS '最后消息时间';


--
-- TOC entry 3929 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.extra IS '扩展信息';


--
-- TOC entry 3930 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_at IS '创建时间';


--
-- TOC entry 3931 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_by IS '创建人';


--
-- TOC entry 3932 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.updated_at IS '更新时间';


--
-- TOC entry 3933 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN msg_thread.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.updated_by IS '更新人';


--
-- TOC entry 231 (class 1259 OID 28553)
-- Name: msg_thread_participant; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_thread_participant (
    id character varying(64) NOT NULL,
    thread_id character varying(64) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_id character varying(64) NOT NULL,
    unread_count integer NOT NULL,
    last_read_message_id character varying(64),
    last_read_at timestamp with time zone,
    is_muted boolean NOT NULL,
    joined_at timestamp with time zone NOT NULL,
    left_at timestamp with time zone
);


ALTER TABLE public.msg_thread_participant OWNER TO postgres;

--
-- TOC entry 3934 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.id IS '主键';


--
-- TOC entry 3935 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.thread_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.thread_id IS '会话ID';


--
-- TOC entry 3936 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.account_type IS '账户类型';


--
-- TOC entry 3937 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.account_id IS '账户ID';


--
-- TOC entry 3938 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.unread_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.unread_count IS '未读数';


--
-- TOC entry 3939 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.last_read_message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.last_read_message_id IS '最后已读消息ID';


--
-- TOC entry 3940 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.last_read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.last_read_at IS '最后阅读时间';


--
-- TOC entry 3941 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.is_muted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.is_muted IS '是否免打扰';


--
-- TOC entry 3942 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.joined_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.joined_at IS '加入时间';


--
-- TOC entry 3943 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN msg_thread_participant.left_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.left_at IS '退出时间';


--
-- TOC entry 232 (class 1259 OID 28561)
-- Name: msg_todo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_todo (
    id character varying(64) NOT NULL,
    title character varying(255) NOT NULL,
    content text,
    content_type character varying(32) NOT NULL,
    priority character varying(32) NOT NULL,
    target_scope character varying(32) NOT NULL,
    target_account_type character varying(32),
    target_account_id character varying(64),
    creator_account_type character varying(32),
    creator_account_id character varying(64),
    source_type character varying(64),
    source_id character varying(64),
    status character varying(32) NOT NULL,
    due_at timestamp with time zone,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.msg_todo OWNER TO postgres;

--
-- TOC entry 3944 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.id IS '主键';


--
-- TOC entry 3945 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.title IS '标题';


--
-- TOC entry 3946 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.content IS '内容';


--
-- TOC entry 3947 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.content_type IS '内容格式';


--
-- TOC entry 3948 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.priority; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.priority IS '优先级';


--
-- TOC entry 3949 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.target_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.target_scope IS '目标范围';


--
-- TOC entry 3950 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.target_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.target_account_type IS '目标账户类型';


--
-- TOC entry 3951 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.target_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.target_account_id IS '目标账户ID';


--
-- TOC entry 3952 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.creator_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.creator_account_type IS '创建账户类型';


--
-- TOC entry 3953 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.creator_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.creator_account_id IS '创建账户ID';


--
-- TOC entry 3954 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.source_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.source_type IS '来源类型';


--
-- TOC entry 3955 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.source_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.source_id IS '来源ID';


--
-- TOC entry 3956 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.status IS '状态';


--
-- TOC entry 3957 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.due_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.due_at IS '截止时间';


--
-- TOC entry 3958 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.extra IS '扩展信息';


--
-- TOC entry 3959 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.created_at IS '创建时间';


--
-- TOC entry 3960 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.created_by IS '创建人';


--
-- TOC entry 3961 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.updated_at IS '更新时间';


--
-- TOC entry 3962 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN msg_todo.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.updated_by IS '更新人';


--
-- TOC entry 233 (class 1259 OID 28572)
-- Name: msg_todo_assignee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.msg_todo_assignee (
    id character varying(64) NOT NULL,
    todo_id character varying(64) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_id character varying(64) NOT NULL,
    status character varying(32) NOT NULL,
    read_at timestamp with time zone,
    started_at timestamp with time zone,
    completed_at timestamp with time zone,
    cancelled_at timestamp with time zone
);


ALTER TABLE public.msg_todo_assignee OWNER TO postgres;

--
-- TOC entry 3963 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.id IS '主键';


--
-- TOC entry 3964 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.todo_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.todo_id IS '待办ID';


--
-- TOC entry 3965 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.account_type IS '账户类型';


--
-- TOC entry 3966 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.account_id IS '账户ID';


--
-- TOC entry 3967 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.status IS '处理状态';


--
-- TOC entry 3968 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.read_at IS '阅读时间';


--
-- TOC entry 3969 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.started_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.started_at IS '开始时间';


--
-- TOC entry 3970 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.completed_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.completed_at IS '完成时间';


--
-- TOC entry 3971 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN msg_todo_assignee.cancelled_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.cancelled_at IS '取消时间';


--
-- TOC entry 234 (class 1259 OID 28580)
-- Name: portal_user_profile; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.portal_user_profile (
    account_id character varying(64) NOT NULL,
    name character varying(64),
    nickname character varying(64),
    avatar text,
    signature text,
    phone character varying(32),
    email character varying(128),
    bio character varying(255),
    level character varying(32),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.portal_user_profile OWNER TO postgres;

--
-- TOC entry 3972 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.account_id IS '账户ID';


--
-- TOC entry 3973 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.name IS '姓名';


--
-- TOC entry 3974 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.nickname; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.nickname IS '昵称';


--
-- TOC entry 3975 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.avatar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.avatar IS '头像';


--
-- TOC entry 3976 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.signature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.signature IS '个性签名';


--
-- TOC entry 3977 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.phone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.phone IS '手机号';


--
-- TOC entry 3978 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.email IS '邮箱';


--
-- TOC entry 3979 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.bio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.bio IS '个人简介';


--
-- TOC entry 3980 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.level; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.level IS '门户等级';


--
-- TOC entry 3981 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.created_at IS '创建时间';


--
-- TOC entry 3982 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.created_by IS '创建人';


--
-- TOC entry 3983 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.updated_at IS '更新时间';


--
-- TOC entry 3984 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN portal_user_profile.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.updated_by IS '更新人';


--
-- TOC entry 235 (class 1259 OID 28589)
-- Name: sys_account; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_account (
    id character varying(64) NOT NULL,
    password_hash character varying(255) NOT NULL,
    account_type character varying(32) NOT NULL,
    account_status character varying(32) NOT NULL,
    cancelled_at timestamp with time zone,
    cancelled_by character varying(64),
    cancel_reason text,
    last_login_ip character varying(64),
    last_login_address character varying(255),
    last_login_time timestamp with time zone,
    last_login_device text,
    latest_login_ip character varying(64),
    latest_login_address character varying(255),
    latest_login_time timestamp with time zone,
    latest_login_device text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_account OWNER TO postgres;

--
-- TOC entry 3985 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.id IS '主键';


--
-- TOC entry 3986 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.password_hash; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.password_hash IS '密码哈希';


--
-- TOC entry 3987 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.account_type IS '账户类型';


--
-- TOC entry 3988 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.account_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.account_status IS '账户状态';


--
-- TOC entry 3989 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.cancelled_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.cancelled_at IS '注销时间';


--
-- TOC entry 3990 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.cancelled_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.cancelled_by IS '注销人';


--
-- TOC entry 3991 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.cancel_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.cancel_reason IS '注销原因';


--
-- TOC entry 3992 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.last_login_ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_ip IS '上次登录IP';


--
-- TOC entry 3993 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.last_login_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_address IS '上次登录地点';


--
-- TOC entry 3994 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.last_login_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_time IS '上次登录时间';


--
-- TOC entry 3995 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.last_login_device; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_device IS '上次登录设备';


--
-- TOC entry 3996 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.latest_login_ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_ip IS '最新登录IP';


--
-- TOC entry 3997 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.latest_login_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_address IS '最新登录地点';


--
-- TOC entry 3998 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.latest_login_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_time IS '最新登录时间';


--
-- TOC entry 3999 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.latest_login_device; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_device IS '最新登录设备';


--
-- TOC entry 4000 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.created_at IS '创建时间';


--
-- TOC entry 4001 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.created_by IS '创建人';


--
-- TOC entry 4002 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.updated_at IS '更新时间';


--
-- TOC entry 4003 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN sys_account.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.updated_by IS '更新人';


--
-- TOC entry 236 (class 1259 OID 28598)
-- Name: sys_account_identity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_account_identity (
    id character varying(64) NOT NULL,
    account_id character varying(64) NOT NULL,
    identity_type character varying(32) NOT NULL,
    identifier character varying(128) NOT NULL,
    verified boolean NOT NULL,
    is_primary boolean NOT NULL,
    bind_status character varying(32) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_account_identity OWNER TO postgres;

--
-- TOC entry 4004 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.id IS '主键';


--
-- TOC entry 4005 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.account_id IS '账户ID';


--
-- TOC entry 4006 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.identity_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.identity_type IS '登录标识类型';


--
-- TOC entry 4007 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.identifier IS '登录标识';


--
-- TOC entry 4008 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.verified; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.verified IS '是否已验证';


--
-- TOC entry 4009 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.is_primary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.is_primary IS '是否主标识';


--
-- TOC entry 4010 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.bind_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.bind_status IS '绑定状态';


--
-- TOC entry 4011 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.created_at IS '创建时间';


--
-- TOC entry 4012 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.created_by IS '创建人';


--
-- TOC entry 4013 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.updated_at IS '更新时间';


--
-- TOC entry 4014 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN sys_account_identity.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.updated_by IS '更新人';


--
-- TOC entry 237 (class 1259 OID 28607)
-- Name: sys_banner; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_banner (
    id character varying(64) NOT NULL,
    title character varying(255) NOT NULL,
    image character varying(500) NOT NULL,
    url character varying(500),
    link_type character varying(16) NOT NULL,
    summary character varying(500),
    description text,
    category character varying(32) NOT NULL,
    type character varying(32) NOT NULL,
    "position" character varying(32) NOT NULL,
    display_scope character varying(32) NOT NULL,
    sort integer NOT NULL,
    interaction_count bigint NOT NULL,
    status character varying(32) NOT NULL,
    start_at timestamp with time zone,
    end_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_banner OWNER TO postgres;

--
-- TOC entry 4015 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.id IS '主键';


--
-- TOC entry 4016 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.title IS '标题';


--
-- TOC entry 4017 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.image; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.image IS '图片地址';


--
-- TOC entry 4018 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.url IS '跳转地址';


--
-- TOC entry 4019 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.link_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.link_type IS '链接类型：展示图链接类型，对应 BANNER_LINK_TYPE 字典组子项 value。';


--
-- TOC entry 4020 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.summary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.summary IS '摘要';


--
-- TOC entry 4021 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.description IS '描述';


--
-- TOC entry 4022 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.category IS '分类：展示图分类，对应 BANNER_CATEGORY 字典组子项 value。';


--
-- TOC entry 4023 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.type IS '类型：展示图类型，对应 BANNER_TYPE 字典组子项 value。';


--
-- TOC entry 4024 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner."position" IS '显示位置：展示图显示位置，对应 BANNER_POSITION 字典组子项 value。';


--
-- TOC entry 4025 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.display_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.display_scope IS '显示端：展示图显示端，对应 BANNER_DISPLAY_SCOPE 字典组子项 value。';


--
-- TOC entry 4026 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.sort IS '排序';


--
-- TOC entry 4027 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.interaction_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.interaction_count IS '交互次数';


--
-- TOC entry 4028 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.status IS '状态';


--
-- TOC entry 4029 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.start_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.start_at IS '开始展示时间';


--
-- TOC entry 4030 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.end_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.end_at IS '结束展示时间';


--
-- TOC entry 4031 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.created_at IS '创建时间';


--
-- TOC entry 4032 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.created_by IS '创建人';


--
-- TOC entry 4033 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.updated_at IS '更新时间';


--
-- TOC entry 4034 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_banner.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.updated_by IS '更新人';


--
-- TOC entry 238 (class 1259 OID 28617)
-- Name: sys_codegen_field; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_codegen_field (
    id character varying(64) NOT NULL,
    plan_id character varying(64) NOT NULL,
    table_role character varying(16) NOT NULL,
    column_name character varying(128) NOT NULL,
    column_comment character varying(255),
    db_type character varying(128) NOT NULL,
    python_type character varying(64) NOT NULL,
    typescript_type character varying(64) NOT NULL,
    form_widget character varying(32) NOT NULL,
    dict_code character varying(128),
    query_operator character varying(32),
    show_in_table boolean NOT NULL,
    show_in_form boolean NOT NULL,
    show_in_detail boolean NOT NULL,
    show_in_query boolean NOT NULL,
    is_primary_key boolean NOT NULL,
    is_required boolean NOT NULL,
    is_unique boolean NOT NULL,
    is_nullable boolean NOT NULL,
    max_length integer,
    sort integer NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_codegen_field OWNER TO postgres;

--
-- TOC entry 4035 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.id IS '主键';


--
-- TOC entry 4036 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.plan_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.plan_id IS '方案ID';


--
-- TOC entry 4037 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.table_role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.table_role IS '表角色';


--
-- TOC entry 4038 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.column_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.column_name IS '字段名';


--
-- TOC entry 4039 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.column_comment; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.column_comment IS '字段注释';


--
-- TOC entry 4040 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.db_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.db_type IS '数据库类型';


--
-- TOC entry 4041 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.python_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.python_type IS 'Python类型';


--
-- TOC entry 4042 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.typescript_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.typescript_type IS 'TypeScript类型';


--
-- TOC entry 4043 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.form_widget; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.form_widget IS '表单控件';


--
-- TOC entry 4044 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.dict_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.dict_code IS '字典编码';


--
-- TOC entry 4045 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.query_operator; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.query_operator IS '查询方式';


--
-- TOC entry 4046 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.show_in_table; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.show_in_table IS '表格显示';


--
-- TOC entry 4047 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.show_in_form; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.show_in_form IS '表单显示';


--
-- TOC entry 4048 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.show_in_detail; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.show_in_detail IS '详情显示';


--
-- TOC entry 4049 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.show_in_query; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.show_in_query IS '查询显示';


--
-- TOC entry 4050 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.is_primary_key; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.is_primary_key IS '是否主键';


--
-- TOC entry 4051 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.is_required; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.is_required IS '是否必填';


--
-- TOC entry 4052 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.is_unique; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.is_unique IS '是否唯一';


--
-- TOC entry 4053 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.is_nullable; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.is_nullable IS '是否可空';


--
-- TOC entry 4054 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.max_length; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.max_length IS '最大长度';


--
-- TOC entry 4055 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.sort IS '排序';


--
-- TOC entry 4056 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.created_at IS '创建时间';


--
-- TOC entry 4057 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.created_by IS '创建人';


--
-- TOC entry 4058 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.updated_at IS '更新时间';


--
-- TOC entry 4059 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN sys_codegen_field.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_field.updated_by IS '更新人';


--
-- TOC entry 239 (class 1259 OID 28629)
-- Name: sys_codegen_plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_codegen_plan (
    id character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    gen_type character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    author character varying(64) NOT NULL,
    description text,
    main_table character varying(128) NOT NULL,
    main_pk character varying(128) NOT NULL,
    main_entity_name character varying(128) NOT NULL,
    main_module_path character varying(255) NOT NULL,
    main_business_name character varying(128) NOT NULL,
    api_prefix character varying(255) NOT NULL,
    permission_prefix character varying(128) NOT NULL,
    resource_module_id character varying(64),
    parent_resource_id character varying(64),
    menu_name character varying(64) NOT NULL,
    menu_path character varying(255) NOT NULL,
    component_path character varying(255) NOT NULL,
    icon character varying(255),
    sort integer NOT NULL,
    tree_parent_field character varying(128),
    tree_label_field character varying(128),
    sub_table character varying(128),
    sub_pk character varying(128),
    sub_foreign_key character varying(128),
    sub_entity_name character varying(128),
    sub_business_name character varying(128),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_codegen_plan OWNER TO postgres;

--
-- TOC entry 4060 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.id IS '主键';


--
-- TOC entry 4061 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.name IS '方案名称';


--
-- TOC entry 4062 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.gen_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.gen_type IS '生成类型';


--
-- TOC entry 4063 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.status IS '状态';


--
-- TOC entry 4064 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.author; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.author IS '作者';


--
-- TOC entry 4065 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.description IS '描述';


--
-- TOC entry 4066 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.main_table; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.main_table IS '主表名';


--
-- TOC entry 4067 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.main_pk; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.main_pk IS '主表主键';


--
-- TOC entry 4068 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.main_entity_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.main_entity_name IS '主实体类名';


--
-- TOC entry 4069 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.main_module_path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.main_module_path IS '后端模块路径';


--
-- TOC entry 4070 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.main_business_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.main_business_name IS '主业务名称';


--
-- TOC entry 4071 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.api_prefix; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.api_prefix IS '接口前缀';


--
-- TOC entry 4072 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.permission_prefix; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.permission_prefix IS '权限前缀';


--
-- TOC entry 4073 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.resource_module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.resource_module_id IS '资源模块ID';


--
-- TOC entry 4074 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.parent_resource_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.parent_resource_id IS '父资源ID';


--
-- TOC entry 4075 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.menu_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.menu_name IS '菜单名称';


--
-- TOC entry 4076 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.menu_path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.menu_path IS '菜单路径';


--
-- TOC entry 4077 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.component_path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.component_path IS '组件路径';


--
-- TOC entry 4078 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.icon IS '菜单图标';


--
-- TOC entry 4079 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.sort IS '排序';


--
-- TOC entry 4080 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.tree_parent_field; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.tree_parent_field IS '树父级字段';


--
-- TOC entry 4081 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.tree_label_field; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.tree_label_field IS '树展示字段';


--
-- TOC entry 4082 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.sub_table; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.sub_table IS '子表名';


--
-- TOC entry 4083 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.sub_pk; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.sub_pk IS '子表主键';


--
-- TOC entry 4084 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.sub_foreign_key; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.sub_foreign_key IS '子表外键';


--
-- TOC entry 4085 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.sub_entity_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.sub_entity_name IS '子实体类名';


--
-- TOC entry 4086 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.sub_business_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.sub_business_name IS '子业务名称';


--
-- TOC entry 4087 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.created_at IS '创建时间';


--
-- TOC entry 4088 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.created_by IS '创建人';


--
-- TOC entry 4089 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.updated_at IS '更新时间';


--
-- TOC entry 4090 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_codegen_plan.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_codegen_plan.updated_by IS '更新人';


--
-- TOC entry 240 (class 1259 OID 28642)
-- Name: sys_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_config (
    id character varying(64) NOT NULL,
    config_key character varying(255) NOT NULL,
    config_value text,
    category character varying(255),
    remark character varying(255),
    sort_code integer NOT NULL,
    ext_json json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_config OWNER TO postgres;

--
-- TOC entry 4091 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.id IS '主键';


--
-- TOC entry 4092 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.config_key; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.config_key IS '配置键';


--
-- TOC entry 4093 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.config_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.config_value IS '配置值';


--
-- TOC entry 4094 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.category IS '分类';


--
-- TOC entry 4095 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.remark IS '备注';


--
-- TOC entry 4096 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.sort_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.sort_code IS '排序码';


--
-- TOC entry 4097 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.ext_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.ext_json IS '扩展信息';


--
-- TOC entry 4098 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.created_at IS '创建时间';


--
-- TOC entry 4099 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.created_by IS '创建人';


--
-- TOC entry 4100 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.updated_at IS '更新时间';


--
-- TOC entry 4101 (class 0 OID 0)
-- Dependencies: 240
-- Name: COLUMN sys_config.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_config.updated_by IS '更新人';


--
-- TOC entry 241 (class 1259 OID 28653)
-- Name: sys_dept; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_dept (
    id character varying(64) NOT NULL,
    parent_id character varying(64),
    master_id character varying(64),
    deputy_master_id character varying(64),
    name character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    category character varying(64) NOT NULL,
    sort integer NOT NULL,
    is_virtual boolean NOT NULL,
    status character varying(32) NOT NULL,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_dept OWNER TO postgres;

--
-- TOC entry 4102 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.id IS '主键';


--
-- TOC entry 4103 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.parent_id IS '父部门ID';


--
-- TOC entry 4104 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.master_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.master_id IS '主管ID';


--
-- TOC entry 4105 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.deputy_master_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.deputy_master_id IS '副主管ID';


--
-- TOC entry 4106 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.name IS '部门名称';


--
-- TOC entry 4107 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.code IS '部门编码';


--
-- TOC entry 4108 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.category IS '部门类别';


--
-- TOC entry 4109 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.sort IS '排序';


--
-- TOC entry 4110 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.is_virtual; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.is_virtual IS '是否虚拟部门';


--
-- TOC entry 4111 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.status IS '状态';


--
-- TOC entry 4112 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.extra IS '扩展信息';


--
-- TOC entry 4113 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.created_at IS '创建时间';


--
-- TOC entry 4114 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.created_by IS '创建人';


--
-- TOC entry 4115 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.updated_at IS '更新时间';


--
-- TOC entry 4116 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dept.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.updated_by IS '更新人';


--
-- TOC entry 242 (class 1259 OID 28664)
-- Name: sys_dict; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_dict (
    id character varying(32) NOT NULL,
    code character varying(50) NOT NULL,
    label character varying(255),
    value character varying(255),
    color character varying(32),
    category character varying(64),
    parent_id character varying(32),
    status character varying(16) NOT NULL,
    sort integer NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_dict OWNER TO postgres;

--
-- TOC entry 4117 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.id IS '主键';


--
-- TOC entry 4118 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.code IS '编码';


--
-- TOC entry 4119 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.label; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.label IS '标签';


--
-- TOC entry 4120 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.value IS '值';


--
-- TOC entry 4121 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.color IS '颜色';


--
-- TOC entry 4122 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.category IS '系统/业务分类：
系统/业务分类
';


--
-- TOC entry 4123 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.parent_id IS '父级ID';


--
-- TOC entry 4124 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.status IS '状态';


--
-- TOC entry 4125 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.sort IS '排序';


--
-- TOC entry 4126 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.created_at IS '创建时间';


--
-- TOC entry 4127 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.created_by IS '创建人';


--
-- TOC entry 4128 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.updated_at IS '更新时间';


--
-- TOC entry 4129 (class 0 OID 0)
-- Dependencies: 242
-- Name: COLUMN sys_dict.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.updated_by IS '更新人';


--
-- TOC entry 243 (class 1259 OID 28676)
-- Name: sys_file; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_file (
    id character varying(64) NOT NULL,
    object_name character varying(255) NOT NULL,
    original_name character varying(255) NOT NULL,
    storage_provider character varying(32) NOT NULL,
    bucket character varying(128),
    content_type character varying(128) NOT NULL,
    size bigint NOT NULL,
    url character varying(1024) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_file OWNER TO postgres;

--
-- TOC entry 4130 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.id IS '主键';


--
-- TOC entry 4131 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.object_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.object_name IS '对象存储路径';


--
-- TOC entry 4132 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.original_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.original_name IS '原始文件名';


--
-- TOC entry 4133 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.storage_provider; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.storage_provider IS '存储服务商';


--
-- TOC entry 4134 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.bucket; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.bucket IS '存储桶';


--
-- TOC entry 4135 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.content_type IS '文件类型';


--
-- TOC entry 4136 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.size; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.size IS '文件大小';


--
-- TOC entry 4137 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.url IS '访问地址';


--
-- TOC entry 4138 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.created_at IS '创建时间';


--
-- TOC entry 4139 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.created_by IS '创建人';


--
-- TOC entry 4140 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.updated_at IS '更新时间';


--
-- TOC entry 4141 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN sys_file.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.updated_by IS '更新人';


--
-- TOC entry 244 (class 1259 OID 28687)
-- Name: sys_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_group (
    id character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    owner_dept_id character varying(64),
    description text,
    status character varying(32) NOT NULL,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_group OWNER TO postgres;

--
-- TOC entry 4142 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.id IS '主键';


--
-- TOC entry 4143 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.name IS '账户组名称';


--
-- TOC entry 4144 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.owner_dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.owner_dept_id IS '所属部门ID';


--
-- TOC entry 4145 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.description IS '描述';


--
-- TOC entry 4146 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.status IS '状态';


--
-- TOC entry 4147 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.extra IS '扩展信息';


--
-- TOC entry 4148 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.created_at IS '创建时间';


--
-- TOC entry 4149 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.created_by IS '创建人';


--
-- TOC entry 4150 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.updated_at IS '更新时间';


--
-- TOC entry 4151 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN sys_group.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.updated_by IS '更新人';


--
-- TOC entry 245 (class 1259 OID 28698)
-- Name: sys_iam_relation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_iam_relation (
    id character varying(64) NOT NULL,
    subject_type character varying(32) NOT NULL,
    subject_id character varying(64) NOT NULL,
    relation_type character varying(64) NOT NULL,
    target_type character varying(32) NOT NULL,
    target_id character varying(64) NOT NULL,
    target_key character varying(128) NOT NULL,
    grant_mode character varying(32) NOT NULL,
    effect character varying(32) NOT NULL,
    data_scope character varying(32) NOT NULL,
    custom_scope_dept_ids json NOT NULL,
    is_primary boolean NOT NULL,
    sort integer NOT NULL,
    status character varying(32) NOT NULL,
    description text,
    reason text,
    expired_at timestamp with time zone,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_iam_relation OWNER TO postgres;

--
-- TOC entry 4152 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.id IS '主键';


--
-- TOC entry 4153 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.subject_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.subject_type IS '主体类型';


--
-- TOC entry 4154 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.subject_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.subject_id IS '主体ID';


--
-- TOC entry 4155 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.relation_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.relation_type IS '关系类型';


--
-- TOC entry 4156 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.target_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.target_type IS '目标类型';


--
-- TOC entry 4157 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.target_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.target_id IS '目标ID';


--
-- TOC entry 4158 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.target_key; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.target_key IS '目标标识';


--
-- TOC entry 4159 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.grant_mode; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.grant_mode IS '授权模式';


--
-- TOC entry 4160 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.effect; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.effect IS '授权效果';


--
-- TOC entry 4161 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.data_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.data_scope IS '数据范围';


--
-- TOC entry 4162 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.custom_scope_dept_ids; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.custom_scope_dept_ids IS '自定义数据范围部门ID列表';


--
-- TOC entry 4163 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.is_primary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.is_primary IS '主关系';


--
-- TOC entry 4164 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.sort IS '排序';


--
-- TOC entry 4165 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.status IS '状态';


--
-- TOC entry 4166 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.description IS '描述';


--
-- TOC entry 4167 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.reason IS '授权原因';


--
-- TOC entry 4168 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.expired_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.expired_at IS '失效时间';


--
-- TOC entry 4169 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.extra IS '扩展信息';


--
-- TOC entry 4170 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.created_at IS '创建时间';


--
-- TOC entry 4171 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.created_by IS '创建人';


--
-- TOC entry 4172 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.updated_at IS '更新时间';


--
-- TOC entry 4173 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN sys_iam_relation.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.updated_by IS '更新人';


--
-- TOC entry 246 (class 1259 OID 28711)
-- Name: sys_operation_audit_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_operation_audit_log (
    id character varying(64) NOT NULL,
    module character varying(64) NOT NULL,
    resource_type character varying(128),
    resource_id character varying(128),
    action character varying(64) NOT NULL,
    summary character varying(255),
    before_data json,
    after_data json,
    account_id character varying(64),
    account_type character varying(32),
    request_id character varying(64),
    ip character varying(64),
    user_agent character varying(512),
    success boolean NOT NULL,
    error_message text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.sys_operation_audit_log OWNER TO postgres;

--
-- TOC entry 4174 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.id IS '主键';


--
-- TOC entry 4175 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.module; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.module IS '模块';


--
-- TOC entry 4176 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.resource_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.resource_type IS '资源类型';


--
-- TOC entry 4177 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.resource_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.resource_id IS '资源ID';


--
-- TOC entry 4178 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.action; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.action IS '操作';


--
-- TOC entry 4179 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.summary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.summary IS '摘要';


--
-- TOC entry 4180 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.before_data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.before_data IS '变更前数据';


--
-- TOC entry 4181 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.after_data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.after_data IS '变更后数据';


--
-- TOC entry 4182 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.account_id IS '操作账号ID';


--
-- TOC entry 4183 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.account_type IS '操作账号类型';


--
-- TOC entry 4184 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.request_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.request_id IS '请求ID';


--
-- TOC entry 4185 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.ip IS '客户端IP';


--
-- TOC entry 4186 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.user_agent; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.user_agent IS 'User-Agent';


--
-- TOC entry 4187 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.success; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.success IS '是否成功';


--
-- TOC entry 4188 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.error_message; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.error_message IS '错误信息';


--
-- TOC entry 4189 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN sys_operation_audit_log.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.created_at IS '创建时间';


--
-- TOC entry 247 (class 1259 OID 28723)
-- Name: sys_position; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_position (
    id character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    category character varying(32) NOT NULL,
    owner_dept_id character varying(64),
    sort integer NOT NULL,
    is_virtual boolean NOT NULL,
    status character varying(32) NOT NULL,
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_position OWNER TO postgres;

--
-- TOC entry 4190 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.id IS '主键';


--
-- TOC entry 4191 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.name IS '职位名称';


--
-- TOC entry 4192 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.code IS '职位编码';


--
-- TOC entry 4193 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.category IS '职位类别';


--
-- TOC entry 4194 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.owner_dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.owner_dept_id IS '所属部门ID';


--
-- TOC entry 4195 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.sort IS '排序';


--
-- TOC entry 4196 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.is_virtual; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.is_virtual IS '是否虚拟职位';


--
-- TOC entry 4197 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.status IS '状态';


--
-- TOC entry 4198 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.description IS '职位描述';


--
-- TOC entry 4199 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.extra IS '扩展信息';


--
-- TOC entry 4200 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.created_at IS '创建时间';


--
-- TOC entry 4201 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.created_by IS '创建人';


--
-- TOC entry 4202 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.updated_at IS '更新时间';


--
-- TOC entry 4203 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN sys_position.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.updated_by IS '更新人';


--
-- TOC entry 248 (class 1259 OID 28734)
-- Name: sys_resource; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_resource (
    id character varying(64) NOT NULL,
    parent_id character varying(64),
    code character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    resource_type character varying(32) NOT NULL,
    module_id character varying(64),
    path character varying(255),
    component character varying(255),
    redirect character varying(255),
    icon character varying(255),
    color character varying(32),
    href character varying(255),
    sort integer NOT NULL,
    is_visible boolean NOT NULL,
    is_cache boolean NOT NULL,
    is_affix boolean NOT NULL,
    status character varying(32) NOT NULL,
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_resource OWNER TO postgres;

--
-- TOC entry 4204 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.id IS '主键';


--
-- TOC entry 4205 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.parent_id IS '父资源ID';


--
-- TOC entry 4206 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.code IS '资源编码';


--
-- TOC entry 4207 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.name IS '资源名称';


--
-- TOC entry 4208 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.resource_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.resource_type IS '资源类型';


--
-- TOC entry 4209 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.module_id IS '所属资源模块ID';


--
-- TOC entry 4210 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.path IS '路由路径';


--
-- TOC entry 4211 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.component; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.component IS '前端组件';


--
-- TOC entry 4212 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.redirect; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.redirect IS '重定向地址';


--
-- TOC entry 4213 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.icon IS '图标';


--
-- TOC entry 4214 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.color IS '颜色';


--
-- TOC entry 4215 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.href; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.href IS '外链地址';


--
-- TOC entry 4216 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.sort IS '排序';


--
-- TOC entry 4217 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.is_visible; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.is_visible IS '是否可见';


--
-- TOC entry 4218 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.is_cache; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.is_cache IS '是否缓存';


--
-- TOC entry 4219 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.is_affix; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.is_affix IS '是否固定标签';


--
-- TOC entry 4220 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.status IS '状态';


--
-- TOC entry 4221 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.description IS '描述';


--
-- TOC entry 4222 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.extra IS '扩展信息';


--
-- TOC entry 4223 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.created_at IS '创建时间';


--
-- TOC entry 4224 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.created_by IS '创建人';


--
-- TOC entry 4225 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.updated_at IS '更新时间';


--
-- TOC entry 4226 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN sys_resource.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.updated_by IS '更新人';


--
-- TOC entry 249 (class 1259 OID 28745)
-- Name: sys_resource_module; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_resource_module (
    id character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    client character varying(32) NOT NULL,
    icon character varying(255),
    color character varying(32),
    sort integer NOT NULL,
    status character varying(32) NOT NULL,
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_resource_module OWNER TO postgres;

--
-- TOC entry 4227 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.id IS '主键';


--
-- TOC entry 4228 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.name IS '模块名称';


--
-- TOC entry 4229 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.code IS '模块编码';


--
-- TOC entry 4230 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.client; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.client IS '所属端';


--
-- TOC entry 4231 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.icon IS '图标';


--
-- TOC entry 4232 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.color IS '颜色';


--
-- TOC entry 4233 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.sort IS '排序';


--
-- TOC entry 4234 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.status IS '状态';


--
-- TOC entry 4235 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.description IS '描述';


--
-- TOC entry 4236 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.extra IS '扩展信息';


--
-- TOC entry 4237 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.created_at IS '创建时间';


--
-- TOC entry 4238 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.created_by IS '创建人';


--
-- TOC entry 4239 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.updated_at IS '更新时间';


--
-- TOC entry 4240 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN sys_resource_module.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.updated_by IS '更新人';


--
-- TOC entry 250 (class 1259 OID 28756)
-- Name: sys_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_role (
    id character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    category character varying(64) NOT NULL,
    scope_type character varying(32) NOT NULL,
    owner_dept_id character varying(64),
    sort integer NOT NULL,
    status character varying(32) NOT NULL,
    is_builtin boolean NOT NULL,
    description text,
    extra json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_role OWNER TO postgres;

--
-- TOC entry 4241 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.id IS '主键';


--
-- TOC entry 4242 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.code IS '角色编码';


--
-- TOC entry 4243 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.name IS '角色名称';


--
-- TOC entry 4244 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.category IS '角色分类';


--
-- TOC entry 4245 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.scope_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.scope_type IS '角色作用域类型';


--
-- TOC entry 4246 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.owner_dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.owner_dept_id IS '所属部门ID';


--
-- TOC entry 4247 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.sort IS '排序';


--
-- TOC entry 4248 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.status IS '状态';


--
-- TOC entry 4249 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.is_builtin; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.is_builtin IS '是否内置角色';


--
-- TOC entry 4250 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.description IS '描述';


--
-- TOC entry 4251 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.extra IS '扩展信息';


--
-- TOC entry 4252 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.created_at IS '创建时间';


--
-- TOC entry 4253 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.created_by IS '创建人';


--
-- TOC entry 4254 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.updated_at IS '更新时间';


--
-- TOC entry 4255 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN sys_role.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.updated_by IS '更新人';


--
-- TOC entry 3680 (class 0 OID 28408)
-- Dependencies: 215
-- Data for Name: admin_user_profile; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.admin_user_profile VALUES ('1', '超级管理员', '超管', NULL, NULL, NULL, NULL, 'Super Admin', 'SA-0001', '系统内置超管账户', '2026-07-19 04:21:34.655885+00', NULL, '2026-07-19 04:21:34.655885+00', NULL);


--
-- TOC entry 3679 (class 0 OID 28403)
-- Dependencies: 214
-- Data for Name: alembic_version; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.alembic_version VALUES ('20e3c1a790ce');


--
-- TOC entry 3681 (class 0 OID 28417)
-- Dependencies: 216
-- Data for Name: cg_test_activity; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3682 (class 0 OID 28426)
-- Dependencies: 217
-- Data for Name: cg_test_catalog; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3683 (class 0 OID 28435)
-- Dependencies: 218
-- Data for Name: cg_test_knowledge_category; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3684 (class 0 OID 28444)
-- Dependencies: 219
-- Data for Name: cg_test_knowledge_doc; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3685 (class 0 OID 28453)
-- Dependencies: 220
-- Data for Name: cg_test_order; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3686 (class 0 OID 28462)
-- Dependencies: 221
-- Data for Name: cg_test_order_item; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3687 (class 0 OID 28471)
-- Dependencies: 222
-- Data for Name: msg_group; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3688 (class 0 OID 28480)
-- Dependencies: 223
-- Data for Name: msg_group_member; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3689 (class 0 OID 28488)
-- Dependencies: 224
-- Data for Name: msg_message; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3690 (class 0 OID 28499)
-- Dependencies: 225
-- Data for Name: msg_message_attachment; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3691 (class 0 OID 28507)
-- Dependencies: 226
-- Data for Name: msg_message_reaction; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3692 (class 0 OID 28515)
-- Dependencies: 227
-- Data for Name: msg_message_receipt; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3693 (class 0 OID 28523)
-- Dependencies: 228
-- Data for Name: msg_notification; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3694 (class 0 OID 28534)
-- Dependencies: 229
-- Data for Name: msg_notification_read; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3695 (class 0 OID 28542)
-- Dependencies: 230
-- Data for Name: msg_thread; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3696 (class 0 OID 28553)
-- Dependencies: 231
-- Data for Name: msg_thread_participant; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3697 (class 0 OID 28561)
-- Dependencies: 232
-- Data for Name: msg_todo; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3698 (class 0 OID 28572)
-- Dependencies: 233
-- Data for Name: msg_todo_assignee; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3699 (class 0 OID 28580)
-- Dependencies: 234
-- Data for Name: portal_user_profile; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3700 (class 0 OID 28589)
-- Dependencies: 235
-- Data for Name: sys_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_account VALUES ('1', '$2b$12$0FMrmFfrIJNuavQoDiSW7.HlyqcAHLaqPxGlGYWLv37HN2WTp..my', 'ADMIN', 'ENABLED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-19 04:21:34.655885+00', NULL, '2026-07-19 04:21:34.655885+00', NULL);


--
-- TOC entry 3701 (class 0 OID 28598)
-- Dependencies: 236
-- Data for Name: sys_account_identity; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_account_identity VALUES ('1', '1', 'ACCOUNT', 'superadmin', true, true, 'BOUND', '2026-07-19 04:21:34.655885+00', NULL, '2026-07-19 04:21:34.655885+00', NULL);


--
-- TOC entry 3702 (class 0 OID 28607)
-- Dependencies: 237
-- Data for Name: sys_banner; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3703 (class 0 OID 28617)
-- Dependencies: 238
-- Data for Name: sys_codegen_field; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3704 (class 0 OID 28629)
-- Dependencies: 239
-- Data for Name: sys_codegen_plan; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3705 (class 0 OID 28642)
-- Dependencies: 240
-- Data for Name: sys_config; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3706 (class 0 OID 28653)
-- Dependencies: 241
-- Data for Name: sys_dept; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3707 (class 0 OID 28664)
-- Dependencies: 242
-- Data for Name: sys_dict; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_dict VALUES ('100001', 'COMMON_STATUS', '状态', 'COMMON_STATUS', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100002', 'COMMON_STATUS_ENABLED', '启用', 'ENABLED', '#18a058', 'SYS', '100001', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100003', 'COMMON_STATUS_DISABLED', '禁用', 'DISABLED', '#d03050', 'SYS', '100001', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100004', 'SYS_BIZ_CATEGORY', '系统/业务分类', 'SYS_BIZ_CATEGORY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100005', 'SYS_BIZ_CATEGORY_SYS', '系统', 'SYS', '#2080f0', 'SYS', '100004', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100006', 'SYS_BIZ_CATEGORY_BIZ', '业务', 'BIZ', '#f0a020', 'SYS', '100004', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100007', 'ACCOUNT_TYPE', '账号类型', 'ACCOUNT_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100008', 'ACCOUNT_TYPE_ADMIN', '管理端', 'ADMIN', '#722ed1', 'SYS', '100007', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100009', 'ACCOUNT_TYPE_PORTAL', '门户端', 'PORTAL', '#18a058', 'SYS', '100007', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100010', 'ACCOUNT_STATUS', '账号状态', 'ACCOUNT_STATUS', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100011', 'ACCOUNT_STATUS_ENABLED', '启用', 'ENABLED', '#18a058', 'SYS', '100010', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100012', 'ACCOUNT_STATUS_DISABLED', '禁用', 'DISABLED', '#d03050', 'SYS', '100010', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100013', 'ACCOUNT_STATUS_CANCELLED', '已注销', 'CANCELLED', '#909399', 'SYS', '100010', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100014', 'ROLE_SCOPE_TYPE', '角色范围类型', 'ROLE_SCOPE_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100015', 'ROLE_SCOPE_TYPE_PLATFORM', '平台', 'PLATFORM', '#2080f0', 'SYS', '100014', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100016', 'ROLE_SCOPE_TYPE_DEPT', '部门', 'DEPT', '#18a058', 'SYS', '100014', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100017', 'RESOURCE_TYPE', '资源类型', 'RESOURCE_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100018', 'RESOURCE_TYPE_CATALOG', '目录', 'CATALOG', '#722ed1', 'SYS', '100017', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100019', 'RESOURCE_TYPE_MENU', '菜单', 'MENU', '#2080f0', 'SYS', '100017', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100020', 'RESOURCE_TYPE_PAGE', '页面', 'PAGE', '#18a058', 'SYS', '100017', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100021', 'RESOURCE_TYPE_BUTTON', '按钮', 'BUTTON', '#f0a020', 'SYS', '100017', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100022', 'RESOURCE_TYPE_ACTION', '操作', 'ACTION', '#d03050', 'SYS', '100017', 'ENABLED', 5, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100023', 'RESOURCE_TYPE_API_GROUP', '接口组', 'API_GROUP', '#1677ff', 'SYS', '100017', 'ENABLED', 6, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100024', 'DATA_SCOPE', '数据范围', 'DATA_SCOPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100025', 'DATA_SCOPE_ALL', '全部', 'ALL', '#18a058', 'SYS', '100024', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100026', 'DATA_SCOPE_DEPT_AND_CHILD', '本部门及子部门', 'DEPT_AND_CHILD', '#2080f0', 'SYS', '100024', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100027', 'DATA_SCOPE_DEPT', '本部门', 'DEPT', '#2db7f5', 'SYS', '100024', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100028', 'DATA_SCOPE_SELF', '本人', 'SELF', '#f0a020', 'SYS', '100024', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100029', 'DATA_SCOPE_CUSTOM', '自定义部门', 'CUSTOM', '#722ed1', 'SYS', '100024', 'ENABLED', 5, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100030', 'GRANT_SUBJECT_TYPE', '授权主体类型', 'GRANT_SUBJECT_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100031', 'GRANT_SUBJECT_TYPE_ROLE', '角色', 'ROLE', '#2080f0', 'SYS', '100030', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100032', 'GRANT_SUBJECT_TYPE_ACCOUNT', '账号', 'ACCOUNT', '#18a058', 'SYS', '100030', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100033', 'GRANT_SUBJECT_TYPE_GROUP', '用户组', 'GROUP', '#f0a020', 'SYS', '100030', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100034', 'GRANT_MODE', '授权模式', 'GRANT_MODE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100035', 'GRANT_MODE_DIRECT', '直接授权', 'DIRECT', '#2080f0', 'SYS', '100034', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100036', 'GRANT_MODE_CASCADE', '级联授权', 'CASCADE', '#18a058', 'SYS', '100034', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100037', 'GRANT_EFFECT', '授权效果', 'GRANT_EFFECT', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100038', 'GRANT_EFFECT_ALLOW', '允许', 'ALLOW', '#18a058', 'SYS', '100037', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100039', 'GRANT_EFFECT_DENY', '拒绝', 'DENY', '#d03050', 'SYS', '100037', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100040', 'DEPT_CATEGORY', '部门分类', 'DEPT_CATEGORY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100041', 'DEPT_CATEGORY_COMPANY', '公司', 'COMPANY', '#2080f0', 'SYS', '100040', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100042', 'DEPT_CATEGORY_DEPARTMENT', '部门', 'DEPARTMENT', '#18a058', 'SYS', '100040', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100043', 'DEPT_CATEGORY_TEAM', '团队', 'TEAM', '#f0a020', 'SYS', '100040', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100044', 'DEPT_CATEGORY_VIRTUAL', '虚拟组织', 'VIRTUAL', '#909399', 'SYS', '100040', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100045', 'POSITION_CATEGORY', '岗位分类', 'POSITION_CATEGORY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100046', 'POSITION_CATEGORY_MANAGEMENT', '管理', 'MANAGEMENT', '#2080f0', 'SYS', '100045', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100047', 'POSITION_CATEGORY_TECHNICAL', '技术', 'TECHNICAL', '#18a058', 'SYS', '100045', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100048', 'POSITION_CATEGORY_OPERATION', '运营', 'OPERATION', '#f0a020', 'SYS', '100045', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100049', 'POSITION_CATEGORY_SUPPORT', '支持', 'SUPPORT', '#909399', 'SYS', '100045', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100050', 'BANNER_DISPLAY_SCOPE', '展示图展示范围', 'BANNER_DISPLAY_SCOPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100051', 'BANNER_DISPLAY_SCOPE_PORTAL', '门户端', 'PORTAL', '#18a058', 'SYS', '100050', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100052', 'BANNER_DISPLAY_SCOPE_ADMIN', '管理端', 'ADMIN', '#2080f0', 'SYS', '100050', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100053', 'BANNER_DISPLAY_SCOPE_APP', '移动端', 'APP', '#f0a020', 'SYS', '100050', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100054', 'BANNER_CATEGORY', '展示图分类', 'BANNER_CATEGORY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100055', 'BANNER_CATEGORY_HOME', '首页', 'HOME', '#18a058', 'SYS', '100054', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100056', 'BANNER_CATEGORY_LOGIN', '登录', 'LOGIN', '#2080f0', 'SYS', '100054', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100057', 'BANNER_CATEGORY_WORKPLACE', '工作台', 'WORKPLACE', '#722ed1', 'SYS', '100054', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100058', 'BANNER_CATEGORY_NOTICE', '公告', 'NOTICE', '#f0a020', 'SYS', '100054', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100059', 'BANNER_CATEGORY_ADMIN_DASHBOARD', '管理端仪表盘', 'ADMIN_DASHBOARD', '#2080f0', 'SYS', '100054', 'ENABLED', 5, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100060', 'BANNER_CATEGORY_SYSTEM_UPGRADE', '系统升级', 'SYSTEM_UPGRADE', '#d03050', 'SYS', '100054', 'ENABLED', 6, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100061', 'BANNER_TYPE', '展示图类型', 'BANNER_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100062', 'BANNER_TYPE_CAROUSEL', '轮播图', 'CAROUSEL', '#18a058', 'SYS', '100061', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100063', 'BANNER_TYPE_HERO', '主视觉', 'HERO', '#2080f0', 'SYS', '100061', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100064', 'BANNER_TYPE_NOTICE', '公告', 'NOTICE', '#f0a020', 'SYS', '100061', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100065', 'BANNER_TYPE_CARD', '卡片', 'CARD', '#722ed1', 'SYS', '100061', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100066', 'BANNER_TYPE_POPUP', '弹窗', 'POPUP', '#d03050', 'SYS', '100061', 'ENABLED', 5, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100067', 'BANNER_TYPE_SIDEBAR', '侧边栏', 'SIDEBAR', '#2080f0', 'SYS', '100061', 'ENABLED', 6, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100068', 'BANNER_POSITION', '展示图位置', 'BANNER_POSITION', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100069', 'BANNER_POSITION_HOME_TOP', '首页顶部', 'HOME_TOP', '#18a058', 'SYS', '100068', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100070', 'BANNER_POSITION_HOME_MIDDLE', '首页中部', 'HOME_MIDDLE', '#18a058', 'SYS', '100068', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100071', 'BANNER_POSITION_HOME_BOTTOM', '首页底部', 'HOME_BOTTOM', '#18a058', 'SYS', '100068', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100072', 'BANNER_POSITION_LOGIN_SIDE', '登录侧边', 'LOGIN_SIDE', '#2080f0', 'SYS', '100068', 'ENABLED', 4, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100073', 'BANNER_POSITION_WORKPLACE_TOP', '工作台顶部', 'WORKPLACE_TOP', '#722ed1', 'SYS', '100068', 'ENABLED', 5, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100074', 'BANNER_POSITION_NOTICE_AREA', '公告区域', 'NOTICE_AREA', '#f0a020', 'SYS', '100068', 'ENABLED', 6, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100075', 'BANNER_POSITION_ADMIN_TOP', '管理端顶部', 'ADMIN_TOP', '#2080f0', 'SYS', '100068', 'ENABLED', 7, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100076', 'BANNER_POSITION_ADMIN_SIDEBAR', '管理端侧边栏', 'ADMIN_SIDEBAR', '#2080f0', 'SYS', '100068', 'ENABLED', 8, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100077', 'BANNER_LINK_TYPE', '展示图链接类型', 'BANNER_LINK_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100078', 'BANNER_LINK_TYPE_URL', '外部链接', 'URL', '#18a058', 'SYS', '100077', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100079', 'BANNER_LINK_TYPE_ROUTE', '路由', 'ROUTE', '#2080f0', 'SYS', '100077', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100080', 'BANNER_LINK_TYPE_NONE', '无链接', 'NONE', '#909399', 'SYS', '100077', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100081', 'ACCOUNT_IDENTITY_TYPE', '账号身份类型', 'ACCOUNT_IDENTITY_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100082', 'ACCOUNT_IDENTITY_TYPE_ACCOUNT', '登录账号', 'ACCOUNT', '#2080f0', 'SYS', '100081', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100083', 'ACCOUNT_IDENTITY_TYPE_EMAIL', '邮箱', 'EMAIL', '#18a058', 'SYS', '100081', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100084', 'ACCOUNT_IDENTITY_TYPE_PHONE', '手机号', 'PHONE', '#f0a020', 'SYS', '100081', 'ENABLED', 3, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100085', 'ACCOUNT_IDENTITY_BIND_STATUS', '账号身份绑定状态', 'ACCOUNT_IDENTITY_BIND_STATUS', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100086', 'ACCOUNT_IDENTITY_BIND_STATUS_BOUND', '已绑定', 'BOUND', '#18a058', 'SYS', '100085', 'ENABLED', 1, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100087', 'ACCOUNT_IDENTITY_BIND_STATUS_UNBOUND', '未绑定', 'UNBOUND', '#909399', 'SYS', '100085', 'ENABLED', 2, '2026-06-29 00:00:00+00', NULL, '2026-06-29 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100088', 'MESSAGE_TARGET_SCOPE', '消息目标范围', 'MESSAGE_TARGET_SCOPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100089', 'MESSAGE_TARGET_SCOPE_ALL', '全部', 'ALL', '#18a058', 'SYS', '100088', 'ENABLED', 1, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100090', 'MESSAGE_TARGET_SCOPE_SPECIFIC', '指定账号', 'SPECIFIC', '#2080f0', 'SYS', '100088', 'ENABLED', 2, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100091', 'NOTIFICATION_STATUS', '通知状态', 'NOTIFICATION_STATUS', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100092', 'NOTIFICATION_STATUS_DRAFT', '草稿', 'DRAFT', '#909399', 'SYS', '100091', 'ENABLED', 1, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100093', 'NOTIFICATION_STATUS_PUBLISHED', '已发布', 'PUBLISHED', '#18a058', 'SYS', '100091', 'ENABLED', 2, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100094', 'NOTIFICATION_STATUS_REVOKED', '已撤回', 'REVOKED', '#d03050', 'SYS', '100091', 'ENABLED', 3, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100095', 'NOTIFICATION_SEVERITY', '通知严重级别', 'NOTIFICATION_SEVERITY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100096', 'NOTIFICATION_SEVERITY_INFO', '信息', 'INFO', '#2080f0', 'SYS', '100095', 'ENABLED', 1, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100097', 'NOTIFICATION_SEVERITY_SUCCESS', '成功', 'SUCCESS', '#18a058', 'SYS', '100095', 'ENABLED', 2, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100098', 'NOTIFICATION_SEVERITY_WARNING', '警告', 'WARNING', '#f0a020', 'SYS', '100095', 'ENABLED', 3, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100099', 'NOTIFICATION_SEVERITY_ERROR', '错误', 'ERROR', '#d03050', 'SYS', '100095', 'ENABLED', 4, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100100', 'MESSAGE_THREAD_TYPE', '消息会话类型', 'MESSAGE_THREAD_TYPE', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100101', 'MESSAGE_THREAD_TYPE_DIRECT', '单聊', 'DIRECT', '#2080f0', 'SYS', '100100', 'ENABLED', 1, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100102', 'MESSAGE_THREAD_TYPE_GROUP', '群聊', 'GROUP', '#18a058', 'SYS', '100100', 'ENABLED', 2, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100103', 'MESSAGE_THREAD_TYPE_SYSTEM', '系统', 'SYSTEM', '#722ed1', 'SYS', '100100', 'ENABLED', 3, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100104', 'TODO_PRIORITY', '待办优先级', 'TODO_PRIORITY', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100105', 'TODO_PRIORITY_LOW', '低', 'LOW', '#909399', 'SYS', '100104', 'ENABLED', 1, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100106', 'TODO_PRIORITY_NORMAL', '普通', 'NORMAL', '#2080f0', 'SYS', '100104', 'ENABLED', 2, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100107', 'TODO_PRIORITY_HIGH', '高', 'HIGH', '#f0a020', 'SYS', '100104', 'ENABLED', 3, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100108', 'TODO_PRIORITY_URGENT', '紧急', 'URGENT', '#d03050', 'SYS', '100104', 'ENABLED', 4, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100109', 'TODO_STATUS', '待办状态', 'TODO_STATUS', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100110', 'TODO_STATUS_PENDING', '待处理', 'PENDING', '#909399', 'SYS', '100109', 'ENABLED', 1, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100111', 'TODO_STATUS_IN_PROGRESS', '进行中', 'IN_PROGRESS', '#2080f0', 'SYS', '100109', 'ENABLED', 2, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100112', 'TODO_STATUS_COMPLETED', '已完成', 'COMPLETED', '#18a058', 'SYS', '100109', 'ENABLED', 3, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100113', 'TODO_STATUS_CANCELLED', '已注销', 'CANCELLED', '#d03050', 'SYS', '100109', 'ENABLED', 4, '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100114', 'STORAGE_PROVIDER', '存储提供商', 'STORAGE_PROVIDER', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-07-02 00:00:00+00', NULL, '2026-07-02 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100115', 'STORAGE_PROVIDER_LOCAL', '本地', 'local', '#18a058', 'SYS', '100114', 'ENABLED', 1, '2026-07-02 00:00:00+00', NULL, '2026-07-02 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100116', 'STORAGE_PROVIDER_MINIO', 'MinIO', 'minio', '#2080f0', 'SYS', '100114', 'ENABLED', 2, '2026-07-02 00:00:00+00', NULL, '2026-07-02 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100117', 'STORAGE_PROVIDER_S3', '亚马逊 S3', 's3', '#722ed1', 'SYS', '100114', 'ENABLED', 3, '2026-07-02 00:00:00+00', NULL, '2026-07-02 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100118', 'STORAGE_PROVIDER_OSS', '阿里云 OSS', 'oss', '#f0a020', 'SYS', '100114', 'ENABLED', 4, '2026-07-02 00:00:00+00', NULL, '2026-07-02 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100119', 'RESOURCE_MODULE_CLIENT', '资源模块客户端', 'RESOURCE_MODULE_CLIENT', '#2080f0', 'SYS', NULL, 'ENABLED', 0, '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100120', 'RESOURCE_MODULE_CLIENT_ADMIN', '管理端', 'ADMIN', '#722ed1', 'SYS', '100119', 'ENABLED', 1, '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_dict VALUES ('100121', 'RESOURCE_MODULE_CLIENT_PORTAL', '门户端', 'PORTAL', '#18a058', 'SYS', '100119', 'ENABLED', 2, '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);


--
-- TOC entry 3708 (class 0 OID 28676)
-- Dependencies: 243
-- Data for Name: sys_file; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3709 (class 0 OID 28687)
-- Dependencies: 244
-- Data for Name: sys_group; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3710 (class 0 OID 28698)
-- Dependencies: 245
-- Data for Name: sys_iam_relation; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_iam_relation VALUES ('1', 'ACCOUNT', '1', 'ACCOUNT_ROLE', 'ROLE', '1', '', 'CASCADE', 'ALLOW', 'SELF', '[]', false, 99, 'ENABLED', NULL, NULL, NULL, '{}', '2026-07-19 04:21:34.655885+00', NULL, '2026-07-19 04:21:34.655885+00', NULL);


--
-- TOC entry 3711 (class 0 OID 28711)
-- Dependencies: 246
-- Data for Name: sys_operation_audit_log; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_operation_audit_log VALUES ('7484467984686256128', 'auth', 'account', '1', 'login', 'ADMIN login succeeded', 'null', 'null', '1', 'ADMIN', 'ae6c738e31b44a1ba58017c3e25931e0', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', true, NULL, '2026-07-19 04:43:41.764099+00');
INSERT INTO public.sys_operation_audit_log VALUES ('7484468212579569664', 'resource', 'resources', NULL, 'update', 'POST /api/v1/admin/sys/resources/update', 'null', 'null', NULL, NULL, NULL, NULL, NULL, true, NULL, '2026-07-19 04:44:36.576484+00');
INSERT INTO public.sys_operation_audit_log VALUES ('7484468249464279040', 'resource', 'resources', NULL, 'update', 'POST /api/v1/admin/sys/resources/update', 'null', 'null', NULL, NULL, NULL, NULL, NULL, true, NULL, '2026-07-19 04:44:45.370352+00');
INSERT INTO public.sys_operation_audit_log VALUES ('7484487756991827968', 'auth', 'account', '1', 'login', 'ADMIN login succeeded', 'null', 'null', '1', 'ADMIN', '8dfd7d7fe55d461da95c32328315615b', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', true, NULL, '2026-07-19 06:02:15.729352+00');


--
-- TOC entry 3712 (class 0 OID 28723)
-- Dependencies: 247
-- Data for Name: sys_position; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3713 (class 0 OID 28734)
-- Dependencies: 248
-- Data for Name: sys_resource; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_resource VALUES ('200001', NULL, 'dashboard', '运营工作台', 'MENU', '210001', '/dashboard', '/dashboard/index.vue', NULL, 'icon-park-outline:analysis', NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200003', NULL, 'sys', '系统', 'CATALOG', '210001', '/sys', NULL, NULL, 'icon-park-outline:setting-two', NULL, NULL, 10, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200004', '200003', 'sys-dict', '字典管理', 'MENU', '210001', '/sys/dict', '/sys/dict/index.vue', NULL, 'icon-park-outline:file-search', NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200005', '200003', 'sys-banner', '展示图管理', 'MENU', '210001', '/sys/banner', '/sys/banner/index.vue', NULL, 'icon-park-outline:ad-product', NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200006', NULL, 'iam', '身份与权限', 'CATALOG', '210001', '/iam', NULL, NULL, 'icon-park-outline:permissions', NULL, NULL, 15, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200007', '200006', 'iam-account', '账号管理', 'MENU', '210001', '/iam/account', '/iam/account/index.vue', NULL, 'icon-park-outline:people', NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200008', '200006', 'iam-dept', '部门管理', 'MENU', '210001', '/iam/dept', '/iam/dept/index.vue', NULL, 'icon-park-outline:tree-diagram', NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200009', '200006', 'iam-group', '用户组管理', 'MENU', '210001', '/iam/group', '/iam/group/index.vue', NULL, 'icon-park-outline:group', NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200010', '200006', 'iam-position', '岗位管理', 'MENU', '210001', '/iam/position', '/iam/position/index.vue', NULL, 'icon-park-outline:people-bottom', NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200011', '200006', 'iam-role', '角色管理', 'MENU', '210001', '/iam/role', '/iam/role/index.vue', NULL, 'icon-park-outline:peoples', NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200012', '200006', 'iam-resource', '资源管理', 'MENU', '210001', '/iam/resource', '/iam/resource/index.vue', NULL, 'icon-park-outline:all-application', NULL, NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200018', '200006', 'iam-resourcemodule', '资源模块管理', 'MENU', '210001', '/iam/resource_module', '/iam/resource_module/index.vue', NULL, 'icon-park-outline:blocks-and-arrows', NULL, NULL, 7, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200019', NULL, 'message', '消息中心', 'CATALOG', '210001', '/message', NULL, NULL, 'icon-park-outline:message', NULL, NULL, 18, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200020', '200019', 'message-notification', '通知管理', 'MENU', '210001', '/message/notification', '/message/notification/index.vue', NULL, 'icon-park-outline:tips-one', NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200022', '200019', 'message-todo', '待办管理', 'MENU', '210001', '/message/todo', '/message/todo/index.vue', NULL, 'icon-park-outline:checklist', NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200023', '200003', 'sys-file', '文件管理', 'MENU', '210001', '/sys/file', '/sys/file/index.vue', NULL, 'icon-park-outline:file-code', NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200024', NULL, 'security', '认证', 'CATALOG', '210001', '/security', NULL, NULL, 'icon-park-outline:lock', NULL, NULL, 12, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200025', '200024', 'security-session', '在线会话', 'MENU', '210001', '/security/session', '/auth/session/index.vue', NULL, 'icon-park-outline:connection', NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200026', NULL, 'portal-demo', '示例页面', 'MENU', '210002', '/demo', '/demo/index.vue', NULL, 'icon-park-outline:experiment-one', NULL, NULL, 1, true, false, false, 'ENABLED', '门户端公开示例菜单', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('200027', '200003', 'sys-audit-api', '操作审计接口', 'API_GROUP', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 9, true, false, false, 'ENABLED', '操作审计后端权限组', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201011', '200004', 'sys-dict-create', '新增字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201012', '200004', 'sys-dict-detail', '查看字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201013', '200004', 'sys-dict-update', '编辑字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201014', '200004', 'sys-dict-delete', '删除字典', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201021', '200005', 'sys-banner-create', '新增展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201022', '200005', 'sys-banner-detail', '查看展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201023', '200005', 'sys-banner-update', '编辑展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201024', '200005', 'sys-banner-delete', '删除展示图', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201031', '200023', 'sys-file-upload', '上传文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201032', '200023', 'sys-file-detail', '查看文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201033', '200023', 'sys-file-update', '编辑文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201034', '200023', 'sys-file-url', '打开文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201035', '200023', 'sys-file-delete', '删除文件', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201041', '200025', 'auth-session-tokenlist', '查看令牌', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201042', '200025', 'auth-session-exit', '强退账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201043', '200025', 'auth-session-tokenexit', '强退令牌', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201051', '305', 'sys-codegen-create', '新增生成方案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201052', '305', 'sys-codegen-detail', '查看生成方案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201053', '305', 'sys-codegen-update', '编辑生成方案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201054', '305', 'sys-codegen-delete', '删除生成方案', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201055', '305', 'sys-codegen-tables', '读取数据库表', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201056', '305', 'sys-codegen-preview', '预览代码', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 60, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201057', '305', 'sys-codegen-download', '下载代码', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 70, false, false, false, 'ENABLED', NULL, '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-18 16:10:45.206881+00', NULL);
INSERT INTO public.sys_resource VALUES ('201101', '200007', 'iam-account-create', '新增账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201102', '200007', 'iam-account-detail', '查看账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201103', '200007', 'iam-account-update', '编辑账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201104', '200007', 'iam-account-delete', '删除账号', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201105', '200007', 'iam-account-grant-role', '分配角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201106', '200007', 'iam-account-grant-group', '分配用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201107', '200007', 'iam-account-grant-dept', '分配部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 7, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201108', '200007', 'iam-account-grant-resource', '分配资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 8, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201121', '200008', 'iam-dept-create', '新增部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201122', '200008', 'iam-dept-detail', '查看部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201123', '200008', 'iam-dept-update', '编辑部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201124', '200008', 'iam-dept-delete', '删除部门', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201131', '200009', 'iam-group-create', '新增用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201132', '200009', 'iam-group-detail', '查看用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201133', '200009', 'iam-group-update', '编辑用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201134', '200009', 'iam-group-delete', '删除用户组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201135', '200009', 'iam-group-grant-user', '分配用户', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201136', '200009', 'iam-group-grant-role', '分配角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201137', '200009', 'iam-group-grant-resource', '分配资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 7, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201151', '200010', 'iam-position-create', '新增岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201152', '200010', 'iam-position-detail', '查看岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201153', '200010', 'iam-position-update', '编辑岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201154', '200010', 'iam-position-delete', '删除岗位', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201161', '200011', 'iam-role-create', '新增角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201162', '200011', 'iam-role-detail', '查看角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201163', '200011', 'iam-role-update', '编辑角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201164', '200011', 'iam-role-delete', '删除角色', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201165', '200011', 'iam-role-grant-resource', '分配资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201167', '200011', 'iam-role-grant-user', '分配用户', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 7, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201181', '200012', 'iam-resource-create', '新增资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201182', '200012', 'iam-resource-detail', '查看资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201183', '200012', 'iam-resource-update', '编辑资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201184', '200012', 'iam-resource-delete', '删除资源', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201185', '200012', 'iam-resource-grant', '绑定权限', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201191', '200018', 'iam-resourcemodule-create', '新增资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201192', '200018', 'iam-resourcemodule-detail', '查看资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201193', '200018', 'iam-resourcemodule-update', '编辑资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201194', '200018', 'iam-resourcemodule-delete', '删除资源模块', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201201', '200020', 'message-notification-create', '新增通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201202', '200020', 'message-notification-detail', '查看通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201203', '200020', 'message-notification-update', '编辑通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201204', '200020', 'message-notification-delete', '删除通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201205', '200020', 'message-notification-publish', '发布通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201206', '200020', 'message-notification-revoke', '撤回通知', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201221', '200021', 'message-thread-detail', '查看会话', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201222', '200021', 'message-thread-send', '发送站内信', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201223', '200021', 'message-group-create', '新增消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201224', '200021', 'message-group-detail', '查看消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201225', '200021', 'message-group-update', '编辑消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201226', '200021', 'message-group-delete', '删除消息组', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 6, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201241', '200022', 'message-todo-create', '新增待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201242', '200022', 'message-todo-detail', '查看待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201243', '200022', 'message-todo-update', '编辑待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201244', '200022', 'message-todo-delete', '删除待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('201245', '200022', 'message-todo-cancel', '取消待办', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 5, true, false, false, 'ENABLED', NULL, '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-03 00:00:00+00', NULL);
INSERT INTO public.sys_resource VALUES ('202001', NULL, 'system-test', '测试目录', 'CATALOG', '210001', '/test', NULL, '/test/editor', 'icon-park-outline:experiment-one', NULL, NULL, 30, true, false, false, 'ENABLED', '系统模块测试页面目录', '{}', '2026-07-18 12:39:16.472705+00', NULL, '2026-07-18 12:39:16.472705+00', NULL);
INSERT INTO public.sys_resource VALUES ('202002', '202001', 'system-test-editor', '编辑器测试', 'MENU', '210001', '/test/editor', '/test/editor/index.vue', NULL, 'icon-park-outline:edit', NULL, NULL, 1, true, false, false, 'ENABLED', 'Markdown、富文本和代码编辑器组件测试页面', '{}', '2026-07-18 12:39:16.472705+00', NULL, '2026-07-18 12:39:16.472705+00', NULL);
INSERT INTO public.sys_resource VALUES ('202003', '202001', 'system-test-icon', '图标选择器测试', 'MENU', '210001', '/test/icon', '/test/icon/index.vue', NULL, 'icon-park-outline:all-application', NULL, NULL, 2, true, false, false, 'ENABLED', 'Iconify 离线图标选择器测试页面', '{}', '2026-07-18 12:49:42.562935+00', NULL, '2026-07-18 12:49:42.562935+00', NULL);
INSERT INTO public.sys_resource VALUES ('202010', '200003', 'system-config', '系统配置', 'MENU', '210001', '/sys/config', '/sys/config/index.vue', NULL, 'icon-park-outline:setting-config', NULL, NULL, 5, true, false, false, 'ENABLED', '系统配置管理页面', '{}', '2026-07-18 14:07:48.814138+00', NULL, '2026-07-18 14:07:48.814138+00', NULL);
INSERT INTO public.sys_resource VALUES ('202011', '202010', 'system-config-create', '新增系统配置', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 1, true, false, false, 'ENABLED', NULL, '{}', '2026-07-18 14:07:48.899867+00', NULL, '2026-07-18 14:07:48.899867+00', NULL);
INSERT INTO public.sys_resource VALUES ('202012', '202010', 'system-config-detail', '查看系统配置', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-07-18 14:07:48.899867+00', NULL, '2026-07-18 14:07:48.899867+00', NULL);
INSERT INTO public.sys_resource VALUES ('202013', '202010', 'system-config-update', '编辑系统配置', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 3, true, false, false, 'ENABLED', NULL, '{}', '2026-07-18 14:07:48.899867+00', NULL, '2026-07-18 14:07:48.899867+00', NULL);
INSERT INTO public.sys_resource VALUES ('202014', '202010', 'system-config-delete', '删除系统配置', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 4, true, false, false, 'ENABLED', NULL, '{}', '2026-07-18 14:07:48.899867+00', NULL, '2026-07-18 14:07:48.899867+00', NULL);
INSERT INTO public.sys_resource VALUES ('7481617843012898816', NULL, 'test', 'test', 'MENU', '7481609907767218176', 'test', NULL, NULL, NULL, NULL, NULL, 0, true, false, false, 'ENABLED', 'testtest', '{}', '2026-07-11 07:58:15.841688+00', '1', '2026-07-11 07:59:08.611726+00', '1');
INSERT INTO public.sys_resource VALUES ('7484420970250375168', '202001', 'biz_cgtestactivity', 'CgTestActivity', 'MENU', '210001', '/biz/cg-test-activity', '/biz/cg-test-activity/index.vue', NULL, 'icon-park-outline:code', NULL, NULL, 99, true, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375169', '7484420970250375168', 'biz_cgtestactivity_page', '分页CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375171', '7484420970250375168', 'biz_cgtestactivity_create', '新增CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375173', '7484420970250375168', 'biz_cgtestactivity_detail', '详情CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375175', '7484420970250375168', 'biz_cgtestactivity_update', '编辑CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375177', '7484420970250375168', 'biz_cgtestactivity_delete', '删除CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375179', '7484420970250375168', 'biz_cgtestactivity_tables', '读取数据表CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 60, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375181', '7484420970250375168', 'biz_cgtestactivity_preview', '预览CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 70, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484420970250375183', '7484420970250375168', 'biz_cgtestactivity_download', '下载CgTestActivity', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 80, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:38:31.17267+00', NULL, '2026-07-19 01:38:31.17267+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308224', '202001', 'biz_cgtestcatalog', 'CgTestCatalog', 'MENU', '210001', '/biz/cg-test-catalog', '/biz/cg-test-catalog/index.vue', NULL, 'icon-park-outline:code', NULL, NULL, 99, true, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308225', '7484421838320308224', 'biz_cgtestcatalog_page', '分页CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308227', '7484421838320308224', 'biz_cgtestcatalog_create', '新增CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308229', '7484421838320308224', 'biz_cgtestcatalog_detail', '详情CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308231', '7484421838320308224', 'biz_cgtestcatalog_update', '编辑CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308233', '7484421838320308224', 'biz_cgtestcatalog_delete', '删除CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308235', '7484421838320308224', 'biz_cgtestcatalog_tables', '读取数据表CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 60, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308237', '7484421838320308224', 'biz_cgtestcatalog_preview', '预览CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 70, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308239', '7484421838320308224', 'biz_cgtestcatalog_download', '下载CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 80, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484421838320308241', '7484421838320308224', 'biz_cgtestcatalog_list', '树列表CgTestCatalog', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 90, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:42:18.517662+00', NULL, '2026-07-19 01:42:18.517662+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572224', '202001', 'biz_cgtestorder', 'CgTestOrder', 'MENU', '210001', '/biz/cg-test-order', '/biz/cg-test-order/index.vue', NULL, 'icon-park-outline:code', NULL, NULL, 99, true, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572225', '7484422652539572224', 'biz_cgtestorder_page', '分页CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572227', '7484422652539572224', 'biz_cgtestorder_create', '新增CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572229', '7484422652539572224', 'biz_cgtestorder_detail', '详情CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572231', '7484422652539572224', 'biz_cgtestorder_update', '编辑CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572233', '7484422652539572224', 'biz_cgtestorder_delete', '删除CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572235', '7484422652539572224', 'biz_cgtestorder_tables', '读取数据表CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 60, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572237', '7484422652539572224', 'biz_cgtestorder_preview', '预览CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 70, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484422652539572239', '7484422652539572224', 'biz_cgtestorder_download', '下载CgTestOrder', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 80, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:45:18.378155+00', NULL, '2026-07-19 01:45:18.378155+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265600', '202001', 'biz_cgtestknowledgecategory', 'CgTestKnowledgeCategory', 'MENU', '210001', '/biz/cg-test-knowledge-category', '/biz/cg-test-knowledge-category/index.vue', NULL, 'icon-park-outline:code', NULL, NULL, 99, true, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265601', '7484424062010265600', 'biz_cgtestknowledgecategory_page', '分页CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 10, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265603', '7484424062010265600', 'biz_cgtestknowledgecategory_create', '新增CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 20, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265605', '7484424062010265600', 'biz_cgtestknowledgecategory_detail', '详情CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 30, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265607', '7484424062010265600', 'biz_cgtestknowledgecategory_update', '编辑CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 40, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265609', '7484424062010265600', 'biz_cgtestknowledgecategory_delete', '删除CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 50, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265611', '7484424062010265600', 'biz_cgtestknowledgecategory_tables', '读取数据表CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 60, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265613', '7484424062010265600', 'biz_cgtestknowledgecategory_preview', '预览CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 70, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265615', '7484424062010265600', 'biz_cgtestknowledgecategory_download', '下载CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 80, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('7484424062010265617', '7484424062010265600', 'biz_cgtestknowledgecategory_list', '树列表CgTestKnowledgeCategory', 'BUTTON', '210001', NULL, NULL, NULL, NULL, NULL, NULL, 90, false, false, false, 'ENABLED', NULL, '{}', '2026-07-19 01:50:52.447895+00', NULL, '2026-07-19 01:50:52.447895+00', NULL);
INSERT INTO public.sys_resource VALUES ('202015', NULL, 'sys-codegen', '代码生成', 'MENU', '210001', '/sys/codegen', '/sys/codegen/index.vue', NULL, 'icon-park-outline:code', NULL, NULL, 999, true, false, false, 'ENABLED', '代码生成管理', '{}', '2026-07-18 16:10:45.206881+00', NULL, '2026-07-19 04:44:45.316341+00', '1');
INSERT INTO public.sys_resource VALUES ('200021', '200019', 'message-im', '站内信', 'MENU', '210001', '/message/im', '/message/im/index.vue', NULL, 'icon-park-outline:message', NULL, NULL, 2, true, false, false, 'ENABLED', NULL, '{}', '2026-06-30 00:00:00+00', NULL, '2026-06-30 00:00:00+00', NULL);


--
-- TOC entry 3714 (class 0 OID 28745)
-- Dependencies: 249
-- Data for Name: sys_resource_module; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_resource_module VALUES ('210001', '系统', 'system', 'ADMIN', 'icon-park-outline:adjacent-item', '#2563eb', 1, 'ENABLED', '系统内置资源模块', '{}', '2026-06-30 00:00:00+00', NULL, '2026-07-18 13:41:56.152581+00', '1');
INSERT INTO public.sys_resource_module VALUES ('210002', '门户', 'HEADER', 'PORTAL', 'icon-park-outline:browser', '#18a058', 2, 'ENABLED', '门户端公开资源模块', '{}', '2026-07-03 00:00:00+00', NULL, '2026-07-12 12:00:01.484173+00', '1');


--
-- TOC entry 3715 (class 0 OID 28756)
-- Dependencies: 250
-- Data for Name: sys_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sys_role VALUES ('1', 'SUPER_ADMIN', '超级管理员', 'SYS', 'PLATFORM', NULL, 1, 'ENABLED', true, '系统内置超级管理员角色', '{}', '2026-07-19 04:21:34.655885+00', NULL, '2026-07-19 04:21:34.655885+00', NULL);


--
-- TOC entry 3400 (class 2606 OID 28407)
-- Name: alembic_version alembic_version_pkc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alembic_version
    ADD CONSTRAINT alembic_version_pkc PRIMARY KEY (version_num);


--
-- TOC entry 3402 (class 2606 OID 28416)
-- Name: admin_user_profile pk_admin_user_profile; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin_user_profile
    ADD CONSTRAINT pk_admin_user_profile PRIMARY KEY (account_id);


--
-- TOC entry 3404 (class 2606 OID 28425)
-- Name: cg_test_activity pk_cg_test_activity; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cg_test_activity
    ADD CONSTRAINT pk_cg_test_activity PRIMARY KEY (id);


--
-- TOC entry 3406 (class 2606 OID 28434)
-- Name: cg_test_catalog pk_cg_test_catalog; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cg_test_catalog
    ADD CONSTRAINT pk_cg_test_catalog PRIMARY KEY (id);


--
-- TOC entry 3408 (class 2606 OID 28443)
-- Name: cg_test_knowledge_category pk_cg_test_knowledge_category; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cg_test_knowledge_category
    ADD CONSTRAINT pk_cg_test_knowledge_category PRIMARY KEY (id);


--
-- TOC entry 3410 (class 2606 OID 28452)
-- Name: cg_test_knowledge_doc pk_cg_test_knowledge_doc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cg_test_knowledge_doc
    ADD CONSTRAINT pk_cg_test_knowledge_doc PRIMARY KEY (id);


--
-- TOC entry 3412 (class 2606 OID 28461)
-- Name: cg_test_order pk_cg_test_order; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cg_test_order
    ADD CONSTRAINT pk_cg_test_order PRIMARY KEY (id);


--
-- TOC entry 3414 (class 2606 OID 28470)
-- Name: cg_test_order_item pk_cg_test_order_item; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cg_test_order_item
    ADD CONSTRAINT pk_cg_test_order_item PRIMARY KEY (id);


--
-- TOC entry 3416 (class 2606 OID 28479)
-- Name: msg_group pk_msg_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_group
    ADD CONSTRAINT pk_msg_group PRIMARY KEY (id);


--
-- TOC entry 3419 (class 2606 OID 28484)
-- Name: msg_group_member pk_msg_group_member; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_group_member
    ADD CONSTRAINT pk_msg_group_member PRIMARY KEY (id);


--
-- TOC entry 3425 (class 2606 OID 28496)
-- Name: msg_message pk_msg_message; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message
    ADD CONSTRAINT pk_msg_message PRIMARY KEY (id);


--
-- TOC entry 3428 (class 2606 OID 28505)
-- Name: msg_message_attachment pk_msg_message_attachment; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_attachment
    ADD CONSTRAINT pk_msg_message_attachment PRIMARY KEY (id);


--
-- TOC entry 3431 (class 2606 OID 28511)
-- Name: msg_message_reaction pk_msg_message_reaction; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_reaction
    ADD CONSTRAINT pk_msg_message_reaction PRIMARY KEY (id);


--
-- TOC entry 3436 (class 2606 OID 28519)
-- Name: msg_message_receipt pk_msg_message_receipt; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_receipt
    ADD CONSTRAINT pk_msg_message_receipt PRIMARY KEY (id);


--
-- TOC entry 3442 (class 2606 OID 28531)
-- Name: msg_notification pk_msg_notification; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_notification
    ADD CONSTRAINT pk_msg_notification PRIMARY KEY (id);


--
-- TOC entry 3445 (class 2606 OID 28538)
-- Name: msg_notification_read pk_msg_notification_read; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_notification_read
    ADD CONSTRAINT pk_msg_notification_read PRIMARY KEY (id);


--
-- TOC entry 3451 (class 2606 OID 28550)
-- Name: msg_thread pk_msg_thread; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_thread
    ADD CONSTRAINT pk_msg_thread PRIMARY KEY (id);


--
-- TOC entry 3454 (class 2606 OID 28557)
-- Name: msg_thread_participant pk_msg_thread_participant; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_thread_participant
    ADD CONSTRAINT pk_msg_thread_participant PRIMARY KEY (id);


--
-- TOC entry 3460 (class 2606 OID 28569)
-- Name: msg_todo pk_msg_todo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_todo
    ADD CONSTRAINT pk_msg_todo PRIMARY KEY (id);


--
-- TOC entry 3463 (class 2606 OID 28576)
-- Name: msg_todo_assignee pk_msg_todo_assignee; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_todo_assignee
    ADD CONSTRAINT pk_msg_todo_assignee PRIMARY KEY (id);


--
-- TOC entry 3467 (class 2606 OID 28588)
-- Name: portal_user_profile pk_portal_user_profile; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.portal_user_profile
    ADD CONSTRAINT pk_portal_user_profile PRIMARY KEY (account_id);


--
-- TOC entry 3469 (class 2606 OID 28597)
-- Name: sys_account pk_sys_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_account
    ADD CONSTRAINT pk_sys_account PRIMARY KEY (id);


--
-- TOC entry 3471 (class 2606 OID 28604)
-- Name: sys_account_identity pk_sys_account_identity; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_account_identity
    ADD CONSTRAINT pk_sys_account_identity PRIMARY KEY (id);


--
-- TOC entry 3476 (class 2606 OID 28615)
-- Name: sys_banner pk_sys_banner; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_banner
    ADD CONSTRAINT pk_sys_banner PRIMARY KEY (id);


--
-- TOC entry 3479 (class 2606 OID 28625)
-- Name: sys_codegen_field pk_sys_codegen_field; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_codegen_field
    ADD CONSTRAINT pk_sys_codegen_field PRIMARY KEY (id);


--
-- TOC entry 3485 (class 2606 OID 28637)
-- Name: sys_codegen_plan pk_sys_codegen_plan; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_codegen_plan
    ADD CONSTRAINT pk_sys_codegen_plan PRIMARY KEY (id);


--
-- TOC entry 3491 (class 2606 OID 28650)
-- Name: sys_config pk_sys_config; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_config
    ADD CONSTRAINT pk_sys_config PRIMARY KEY (id);


--
-- TOC entry 3493 (class 2606 OID 28661)
-- Name: sys_dept pk_sys_dept; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT pk_sys_dept PRIMARY KEY (id);


--
-- TOC entry 3500 (class 2606 OID 28672)
-- Name: sys_dict pk_sys_dict; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict
    ADD CONSTRAINT pk_sys_dict PRIMARY KEY (id);


--
-- TOC entry 3502 (class 2606 OID 28684)
-- Name: sys_file pk_sys_file; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_file
    ADD CONSTRAINT pk_sys_file PRIMARY KEY (id);


--
-- TOC entry 3506 (class 2606 OID 28695)
-- Name: sys_group pk_sys_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_group
    ADD CONSTRAINT pk_sys_group PRIMARY KEY (id);


--
-- TOC entry 3512 (class 2606 OID 28706)
-- Name: sys_iam_relation pk_sys_iam_relation; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_iam_relation
    ADD CONSTRAINT pk_sys_iam_relation PRIMARY KEY (id);


--
-- TOC entry 3520 (class 2606 OID 28718)
-- Name: sys_operation_audit_log pk_sys_operation_audit_log; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_operation_audit_log
    ADD CONSTRAINT pk_sys_operation_audit_log PRIMARY KEY (id);


--
-- TOC entry 3522 (class 2606 OID 28731)
-- Name: sys_position pk_sys_position; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_position
    ADD CONSTRAINT pk_sys_position PRIMARY KEY (id);


--
-- TOC entry 3526 (class 2606 OID 28742)
-- Name: sys_resource pk_sys_resource; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource
    ADD CONSTRAINT pk_sys_resource PRIMARY KEY (id);


--
-- TOC entry 3530 (class 2606 OID 28753)
-- Name: sys_resource_module pk_sys_resource_module; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource_module
    ADD CONSTRAINT pk_sys_resource_module PRIMARY KEY (id);


--
-- TOC entry 3534 (class 2606 OID 28764)
-- Name: sys_role pk_sys_role; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT pk_sys_role PRIMARY KEY (id);


--
-- TOC entry 3421 (class 2606 OID 28486)
-- Name: msg_group_member uq_msg_group_member_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_group_member
    ADD CONSTRAINT uq_msg_group_member_account UNIQUE (group_id, account_type, account_id);


--
-- TOC entry 3433 (class 2606 OID 28513)
-- Name: msg_message_reaction uq_msg_message_reaction_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_reaction
    ADD CONSTRAINT uq_msg_message_reaction_account UNIQUE (message_id, account_type, account_id, reaction);


--
-- TOC entry 3438 (class 2606 OID 28521)
-- Name: msg_message_receipt uq_msg_message_receipt_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_receipt
    ADD CONSTRAINT uq_msg_message_receipt_account UNIQUE (message_id, account_type, account_id);


--
-- TOC entry 3447 (class 2606 OID 28540)
-- Name: msg_notification_read uq_msg_notification_read_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_notification_read
    ADD CONSTRAINT uq_msg_notification_read_account UNIQUE (notification_id, account_type, account_id);


--
-- TOC entry 3456 (class 2606 OID 28559)
-- Name: msg_thread_participant uq_msg_thread_participant_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_thread_participant
    ADD CONSTRAINT uq_msg_thread_participant_account UNIQUE (thread_id, account_type, account_id);


--
-- TOC entry 3465 (class 2606 OID 28578)
-- Name: msg_todo_assignee uq_msg_todo_assignee_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_todo_assignee
    ADD CONSTRAINT uq_msg_todo_assignee_account UNIQUE (todo_id, account_type, account_id);


--
-- TOC entry 3473 (class 2606 OID 28606)
-- Name: sys_account_identity uq_sys_account_identity_type_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_account_identity
    ADD CONSTRAINT uq_sys_account_identity_type_identifier UNIQUE (identity_type, identifier);


--
-- TOC entry 3481 (class 2606 OID 28627)
-- Name: sys_codegen_field uq_sys_codegen_field_plan_role_column; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_codegen_field
    ADD CONSTRAINT uq_sys_codegen_field_plan_role_column UNIQUE (plan_id, table_role, column_name);


--
-- TOC entry 3487 (class 2606 OID 28639)
-- Name: sys_codegen_plan uq_sys_codegen_plan_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_codegen_plan
    ADD CONSTRAINT uq_sys_codegen_plan_name UNIQUE (name);


--
-- TOC entry 3495 (class 2606 OID 28663)
-- Name: sys_dept uq_sys_dept_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT uq_sys_dept_code UNIQUE (code);


--
-- TOC entry 3504 (class 2606 OID 28686)
-- Name: sys_file uq_sys_file_object_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_file
    ADD CONSTRAINT uq_sys_file_object_name UNIQUE (object_name);


--
-- TOC entry 3508 (class 2606 OID 28697)
-- Name: sys_group uq_sys_group_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_group
    ADD CONSTRAINT uq_sys_group_name UNIQUE (name);


--
-- TOC entry 3514 (class 2606 OID 28708)
-- Name: sys_iam_relation uq_sys_iam_relation_subject_relation_target; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_iam_relation
    ADD CONSTRAINT uq_sys_iam_relation_subject_relation_target UNIQUE (subject_type, subject_id, relation_type, target_type, target_id, target_key);


--
-- TOC entry 3524 (class 2606 OID 28733)
-- Name: sys_position uq_sys_position_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_position
    ADD CONSTRAINT uq_sys_position_code UNIQUE (code);


--
-- TOC entry 3532 (class 2606 OID 28755)
-- Name: sys_resource_module uq_sys_resource_module_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource_module
    ADD CONSTRAINT uq_sys_resource_module_code UNIQUE (code);


--
-- TOC entry 3528 (class 2606 OID 28744)
-- Name: sys_resource uq_sys_resource_module_id_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource
    ADD CONSTRAINT uq_sys_resource_module_id_code UNIQUE (module_id, code);


--
-- TOC entry 3536 (class 2606 OID 28766)
-- Name: sys_role uq_sys_role_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT uq_sys_role_code UNIQUE (code);


--
-- TOC entry 3488 (class 1259 OID 28651)
-- Name: idx_sys_config_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_config_category ON public.sys_config USING btree (category);


--
-- TOC entry 3489 (class 1259 OID 28652)
-- Name: idx_sys_config_key; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_sys_config_key ON public.sys_config USING btree (config_key);


--
-- TOC entry 3496 (class 1259 OID 28673)
-- Name: idx_sys_dict_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_dict_category ON public.sys_dict USING btree (category);


--
-- TOC entry 3497 (class 1259 OID 28674)
-- Name: idx_sys_dict_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_sys_dict_code ON public.sys_dict USING btree (code);


--
-- TOC entry 3498 (class 1259 OID 28675)
-- Name: idx_sys_dict_parent_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_dict_parent_id ON public.sys_dict USING btree (parent_id);


--
-- TOC entry 3515 (class 1259 OID 28719)
-- Name: idx_sys_operation_audit_account_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_account_id ON public.sys_operation_audit_log USING btree (account_id);


--
-- TOC entry 3516 (class 1259 OID 28720)
-- Name: idx_sys_operation_audit_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_created_at ON public.sys_operation_audit_log USING btree (created_at);


--
-- TOC entry 3517 (class 1259 OID 28721)
-- Name: idx_sys_operation_audit_module_action; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_module_action ON public.sys_operation_audit_log USING btree (module, action);


--
-- TOC entry 3518 (class 1259 OID 28722)
-- Name: idx_sys_operation_audit_resource; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_resource ON public.sys_operation_audit_log USING btree (resource_type, resource_id);


--
-- TOC entry 3417 (class 1259 OID 28487)
-- Name: ix_msg_group_member_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_group_member_account ON public.msg_group_member USING btree (account_type, account_id);


--
-- TOC entry 3426 (class 1259 OID 28506)
-- Name: ix_msg_message_attachment_message; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_attachment_message ON public.msg_message_attachment USING btree (message_id, sort);


--
-- TOC entry 3422 (class 1259 OID 28497)
-- Name: ix_msg_message_parent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_parent ON public.msg_message USING btree (parent_id);


--
-- TOC entry 3429 (class 1259 OID 28514)
-- Name: ix_msg_message_reaction_message; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_reaction_message ON public.msg_message_reaction USING btree (message_id);


--
-- TOC entry 3434 (class 1259 OID 28522)
-- Name: ix_msg_message_receipt_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_receipt_account ON public.msg_message_receipt USING btree (account_type, account_id);


--
-- TOC entry 3423 (class 1259 OID 28498)
-- Name: ix_msg_message_thread_created; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_thread_created ON public.msg_message USING btree (thread_id, created_at);


--
-- TOC entry 3443 (class 1259 OID 28541)
-- Name: ix_msg_notification_read_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_notification_read_account ON public.msg_notification_read USING btree (account_type, account_id);


--
-- TOC entry 3439 (class 1259 OID 28532)
-- Name: ix_msg_notification_status_scope_publish; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_notification_status_scope_publish ON public.msg_notification USING btree (status, target_scope, publish_at);


--
-- TOC entry 3440 (class 1259 OID 28533)
-- Name: ix_msg_notification_target_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_notification_target_account ON public.msg_notification USING btree (target_account_type, target_account_id);


--
-- TOC entry 3448 (class 1259 OID 28551)
-- Name: ix_msg_thread_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_thread_group ON public.msg_thread USING btree (group_id);


--
-- TOC entry 3452 (class 1259 OID 28560)
-- Name: ix_msg_thread_participant_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_thread_participant_account ON public.msg_thread_participant USING btree (account_type, account_id);


--
-- TOC entry 3449 (class 1259 OID 28552)
-- Name: ix_msg_thread_type_status_last; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_thread_type_status_last ON public.msg_thread USING btree (thread_type, status, last_message_at);


--
-- TOC entry 3461 (class 1259 OID 28579)
-- Name: ix_msg_todo_assignee_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_todo_assignee_account ON public.msg_todo_assignee USING btree (account_type, account_id, status);


--
-- TOC entry 3457 (class 1259 OID 28570)
-- Name: ix_msg_todo_status_scope_due; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_todo_status_scope_due ON public.msg_todo USING btree (status, target_scope, due_at);


--
-- TOC entry 3458 (class 1259 OID 28571)
-- Name: ix_msg_todo_target_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_todo_target_account ON public.msg_todo USING btree (target_account_type, target_account_id);


--
-- TOC entry 3474 (class 1259 OID 28616)
-- Name: ix_sys_banner_scope_position_status_sort; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_banner_scope_position_status_sort ON public.sys_banner USING btree (display_scope, "position", status, sort);


--
-- TOC entry 3477 (class 1259 OID 28628)
-- Name: ix_sys_codegen_field_plan_role_sort; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_codegen_field_plan_role_sort ON public.sys_codegen_field USING btree (plan_id, table_role, sort);


--
-- TOC entry 3482 (class 1259 OID 28640)
-- Name: ix_sys_codegen_plan_gen_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_codegen_plan_gen_type ON public.sys_codegen_plan USING btree (gen_type);


--
-- TOC entry 3483 (class 1259 OID 28641)
-- Name: ix_sys_codegen_plan_main_table; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_codegen_plan_main_table ON public.sys_codegen_plan USING btree (main_table);


--
-- TOC entry 3509 (class 1259 OID 28709)
-- Name: ix_sys_iam_relation_subject; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_iam_relation_subject ON public.sys_iam_relation USING btree (subject_type, subject_id, relation_type);


--
-- TOC entry 3510 (class 1259 OID 28710)
-- Name: ix_sys_iam_relation_target; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_iam_relation_target ON public.sys_iam_relation USING btree (target_type, target_id, target_key);


--
-- TOC entry 3723 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


-- Completed on 2026-07-19 15:52:05

--
-- PostgreSQL database dump complete
--

\unrestrict KtbxZA41zp5p8LP4uaMVhCTCfd3Jl9czpMszF79P2P9FSC6TAhhoRmRggG8qaMu


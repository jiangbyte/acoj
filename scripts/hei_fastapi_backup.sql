--
-- PostgreSQL database dump
--

\restrict cnnHCZ6eYcN90fyHoJfKBqw2TN3AU4VvpLuuiRmzVr5bGecOcbyARcH3fsM4MSB

-- Dumped from database version 17.10 (Debian 17.10-1.pgdg12+1)
-- Dumped by pg_dump version 17.10 (Debian 17.10-1.pgdg12+1)

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
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
-- Name: COLUMN admin_user_profile.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.account_id IS '账户ID';


--
-- Name: COLUMN admin_user_profile.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.name IS '姓名';


--
-- Name: COLUMN admin_user_profile.nickname; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.nickname IS '昵称';


--
-- Name: COLUMN admin_user_profile.avatar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.avatar IS '头像';


--
-- Name: COLUMN admin_user_profile.signature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.signature IS '个性签名';


--
-- Name: COLUMN admin_user_profile.phone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.phone IS '手机号';


--
-- Name: COLUMN admin_user_profile.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.email IS '邮箱';


--
-- Name: COLUMN admin_user_profile.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.title IS '岗位头衔';


--
-- Name: COLUMN admin_user_profile.employee_no; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.employee_no IS '员工编号';


--
-- Name: COLUMN admin_user_profile.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.remark IS '备注';


--
-- Name: COLUMN admin_user_profile.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.created_at IS '创建时间';


--
-- Name: COLUMN admin_user_profile.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.created_by IS '创建人';


--
-- Name: COLUMN admin_user_profile.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.updated_at IS '更新时间';


--
-- Name: COLUMN admin_user_profile.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.admin_user_profile.updated_by IS '更新人';


--
-- Name: alembic_version; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alembic_version (
    version_num character varying(32) NOT NULL
);


ALTER TABLE public.alembic_version OWNER TO postgres;

--
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
-- Name: COLUMN msg_group.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.id IS '主键';


--
-- Name: COLUMN msg_group.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.name IS '群组名称';


--
-- Name: COLUMN msg_group.owner_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.owner_account_type IS '群主账户类型';


--
-- Name: COLUMN msg_group.owner_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.owner_account_id IS '群主账户ID';


--
-- Name: COLUMN msg_group.avatar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.avatar IS '群头像';


--
-- Name: COLUMN msg_group.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.status IS '状态';


--
-- Name: COLUMN msg_group.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.description IS '描述';


--
-- Name: COLUMN msg_group.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.extra IS '扩展信息';


--
-- Name: COLUMN msg_group.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.created_at IS '创建时间';


--
-- Name: COLUMN msg_group.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.created_by IS '创建人';


--
-- Name: COLUMN msg_group.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.updated_at IS '更新时间';


--
-- Name: COLUMN msg_group.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group.updated_by IS '更新人';


--
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
-- Name: COLUMN msg_group_member.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.id IS '主键';


--
-- Name: COLUMN msg_group_member.group_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.group_id IS '群组ID';


--
-- Name: COLUMN msg_group_member.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.account_type IS '账户类型';


--
-- Name: COLUMN msg_group_member.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.account_id IS '账户ID';


--
-- Name: COLUMN msg_group_member.nickname; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.nickname IS '群昵称';


--
-- Name: COLUMN msg_group_member.is_muted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.is_muted IS '是否免打扰';


--
-- Name: COLUMN msg_group_member.joined_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.joined_at IS '加入时间';


--
-- Name: COLUMN msg_group_member.left_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_group_member.left_at IS '退出时间';


--
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
-- Name: COLUMN msg_message.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.id IS '主键';


--
-- Name: COLUMN msg_message.thread_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.thread_id IS '会话ID';


--
-- Name: COLUMN msg_message.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.parent_id IS '回复消息ID';


--
-- Name: COLUMN msg_message.sender_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_type IS '发送方类型';


--
-- Name: COLUMN msg_message.sender_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_account_type IS '发送账户类型';


--
-- Name: COLUMN msg_message.sender_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_account_id IS '发送账户ID';


--
-- Name: COLUMN msg_message.sender_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.sender_name IS '发送方快照名称';


--
-- Name: COLUMN msg_message.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.content IS '内容';


--
-- Name: COLUMN msg_message.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.content_type IS '内容格式';


--
-- Name: COLUMN msg_message.reply_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.reply_count IS '回复数';


--
-- Name: COLUMN msg_message.is_revoked; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.is_revoked IS '是否撤回';


--
-- Name: COLUMN msg_message.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.extra IS '扩展信息';


--
-- Name: COLUMN msg_message.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.created_at IS '创建时间';


--
-- Name: COLUMN msg_message.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.created_by IS '创建人';


--
-- Name: COLUMN msg_message.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.updated_at IS '更新时间';


--
-- Name: COLUMN msg_message.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message.updated_by IS '更新人';


--
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
-- Name: COLUMN msg_message_attachment.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.id IS '主键';


--
-- Name: COLUMN msg_message_attachment.message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.message_id IS '消息ID';


--
-- Name: COLUMN msg_message_attachment.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.name IS '文件名';


--
-- Name: COLUMN msg_message_attachment.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.url IS '文件地址';


--
-- Name: COLUMN msg_message_attachment.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.content_type IS '文件类型';


--
-- Name: COLUMN msg_message_attachment.size; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.size IS '文件大小';


--
-- Name: COLUMN msg_message_attachment.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.sort IS '排序';


--
-- Name: COLUMN msg_message_attachment.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_attachment.extra IS '扩展信息';


--
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
-- Name: COLUMN msg_message_reaction.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.id IS '主键';


--
-- Name: COLUMN msg_message_reaction.message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.message_id IS '消息ID';


--
-- Name: COLUMN msg_message_reaction.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.account_type IS '账户类型';


--
-- Name: COLUMN msg_message_reaction.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.account_id IS '账户ID';


--
-- Name: COLUMN msg_message_reaction.reaction; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.reaction IS '反应';


--
-- Name: COLUMN msg_message_reaction.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_reaction.created_at IS '创建时间';


--
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
-- Name: COLUMN msg_message_receipt.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.id IS '主键';


--
-- Name: COLUMN msg_message_receipt.message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.message_id IS '消息ID';


--
-- Name: COLUMN msg_message_receipt.thread_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.thread_id IS '会话ID';


--
-- Name: COLUMN msg_message_receipt.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.account_type IS '账户类型';


--
-- Name: COLUMN msg_message_receipt.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.account_id IS '账户ID';


--
-- Name: COLUMN msg_message_receipt.read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_message_receipt.read_at IS '阅读时间';


--
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
-- Name: COLUMN msg_notification.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.id IS '主键';


--
-- Name: COLUMN msg_notification.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.title IS '标题';


--
-- Name: COLUMN msg_notification.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.content IS '内容';


--
-- Name: COLUMN msg_notification.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.content_type IS '内容格式';


--
-- Name: COLUMN msg_notification.severity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.severity IS '等级';


--
-- Name: COLUMN msg_notification.target_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.target_scope IS '目标范围';


--
-- Name: COLUMN msg_notification.target_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.target_account_type IS '目标账户类型';


--
-- Name: COLUMN msg_notification.target_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.target_account_id IS '目标账户ID';


--
-- Name: COLUMN msg_notification.sender_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.sender_account_type IS '发送账户类型';


--
-- Name: COLUMN msg_notification.sender_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.sender_account_id IS '发送账户ID';


--
-- Name: COLUMN msg_notification.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.status IS '状态';


--
-- Name: COLUMN msg_notification.publish_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.publish_at IS '发布时间';


--
-- Name: COLUMN msg_notification.revoked_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.revoked_at IS '撤回时间';


--
-- Name: COLUMN msg_notification.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.extra IS '扩展信息';


--
-- Name: COLUMN msg_notification.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.created_at IS '创建时间';


--
-- Name: COLUMN msg_notification.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.created_by IS '创建人';


--
-- Name: COLUMN msg_notification.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.updated_at IS '更新时间';


--
-- Name: COLUMN msg_notification.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification.updated_by IS '更新人';


--
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
-- Name: COLUMN msg_notification_read.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.id IS '主键';


--
-- Name: COLUMN msg_notification_read.notification_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.notification_id IS '通知ID';


--
-- Name: COLUMN msg_notification_read.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.account_type IS '账户类型';


--
-- Name: COLUMN msg_notification_read.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.account_id IS '账户ID';


--
-- Name: COLUMN msg_notification_read.read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_notification_read.read_at IS '阅读时间';


--
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
-- Name: COLUMN msg_thread.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.id IS '主键';


--
-- Name: COLUMN msg_thread.thread_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.thread_type IS '会话类型';


--
-- Name: COLUMN msg_thread.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.title IS '会话标题';


--
-- Name: COLUMN msg_thread.group_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.group_id IS '消息群组ID';


--
-- Name: COLUMN msg_thread.created_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_account_type IS '创建账户类型';


--
-- Name: COLUMN msg_thread.created_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_account_id IS '创建账户ID';


--
-- Name: COLUMN msg_thread.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.status IS '状态';


--
-- Name: COLUMN msg_thread.last_message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.last_message_id IS '最后消息ID';


--
-- Name: COLUMN msg_thread.last_message_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.last_message_at IS '最后消息时间';


--
-- Name: COLUMN msg_thread.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.extra IS '扩展信息';


--
-- Name: COLUMN msg_thread.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_at IS '创建时间';


--
-- Name: COLUMN msg_thread.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.created_by IS '创建人';


--
-- Name: COLUMN msg_thread.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.updated_at IS '更新时间';


--
-- Name: COLUMN msg_thread.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread.updated_by IS '更新人';


--
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
-- Name: COLUMN msg_thread_participant.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.id IS '主键';


--
-- Name: COLUMN msg_thread_participant.thread_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.thread_id IS '会话ID';


--
-- Name: COLUMN msg_thread_participant.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.account_type IS '账户类型';


--
-- Name: COLUMN msg_thread_participant.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.account_id IS '账户ID';


--
-- Name: COLUMN msg_thread_participant.unread_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.unread_count IS '未读数';


--
-- Name: COLUMN msg_thread_participant.last_read_message_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.last_read_message_id IS '最后已读消息ID';


--
-- Name: COLUMN msg_thread_participant.last_read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.last_read_at IS '最后阅读时间';


--
-- Name: COLUMN msg_thread_participant.is_muted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.is_muted IS '是否免打扰';


--
-- Name: COLUMN msg_thread_participant.joined_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.joined_at IS '加入时间';


--
-- Name: COLUMN msg_thread_participant.left_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_thread_participant.left_at IS '退出时间';


--
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
-- Name: COLUMN msg_todo.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.id IS '主键';


--
-- Name: COLUMN msg_todo.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.title IS '标题';


--
-- Name: COLUMN msg_todo.content; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.content IS '内容';


--
-- Name: COLUMN msg_todo.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.content_type IS '内容格式';


--
-- Name: COLUMN msg_todo.priority; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.priority IS '优先级';


--
-- Name: COLUMN msg_todo.target_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.target_scope IS '目标范围';


--
-- Name: COLUMN msg_todo.target_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.target_account_type IS '目标账户类型';


--
-- Name: COLUMN msg_todo.target_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.target_account_id IS '目标账户ID';


--
-- Name: COLUMN msg_todo.creator_account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.creator_account_type IS '创建账户类型';


--
-- Name: COLUMN msg_todo.creator_account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.creator_account_id IS '创建账户ID';


--
-- Name: COLUMN msg_todo.source_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.source_type IS '来源类型';


--
-- Name: COLUMN msg_todo.source_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.source_id IS '来源ID';


--
-- Name: COLUMN msg_todo.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.status IS '状态';


--
-- Name: COLUMN msg_todo.due_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.due_at IS '截止时间';


--
-- Name: COLUMN msg_todo.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.extra IS '扩展信息';


--
-- Name: COLUMN msg_todo.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.created_at IS '创建时间';


--
-- Name: COLUMN msg_todo.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.created_by IS '创建人';


--
-- Name: COLUMN msg_todo.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.updated_at IS '更新时间';


--
-- Name: COLUMN msg_todo.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo.updated_by IS '更新人';


--
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
-- Name: COLUMN msg_todo_assignee.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.id IS '主键';


--
-- Name: COLUMN msg_todo_assignee.todo_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.todo_id IS '待办ID';


--
-- Name: COLUMN msg_todo_assignee.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.account_type IS '账户类型';


--
-- Name: COLUMN msg_todo_assignee.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.account_id IS '账户ID';


--
-- Name: COLUMN msg_todo_assignee.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.status IS '处理状态';


--
-- Name: COLUMN msg_todo_assignee.read_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.read_at IS '阅读时间';


--
-- Name: COLUMN msg_todo_assignee.started_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.started_at IS '开始时间';


--
-- Name: COLUMN msg_todo_assignee.completed_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.completed_at IS '完成时间';


--
-- Name: COLUMN msg_todo_assignee.cancelled_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.msg_todo_assignee.cancelled_at IS '取消时间';


--
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
-- Name: COLUMN portal_user_profile.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.account_id IS '账户ID';


--
-- Name: COLUMN portal_user_profile.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.name IS '姓名';


--
-- Name: COLUMN portal_user_profile.nickname; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.nickname IS '昵称';


--
-- Name: COLUMN portal_user_profile.avatar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.avatar IS '头像';


--
-- Name: COLUMN portal_user_profile.signature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.signature IS '个性签名';


--
-- Name: COLUMN portal_user_profile.phone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.phone IS '手机号';


--
-- Name: COLUMN portal_user_profile.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.email IS '邮箱';


--
-- Name: COLUMN portal_user_profile.bio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.bio IS '个人简介';


--
-- Name: COLUMN portal_user_profile.level; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.level IS '门户等级';


--
-- Name: COLUMN portal_user_profile.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.created_at IS '创建时间';


--
-- Name: COLUMN portal_user_profile.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.created_by IS '创建人';


--
-- Name: COLUMN portal_user_profile.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.updated_at IS '更新时间';


--
-- Name: COLUMN portal_user_profile.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.portal_user_profile.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_account.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.id IS '主键';


--
-- Name: COLUMN sys_account.password_hash; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.password_hash IS '密码哈希';


--
-- Name: COLUMN sys_account.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.account_type IS '账户类型';


--
-- Name: COLUMN sys_account.account_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.account_status IS '账户状态';


--
-- Name: COLUMN sys_account.cancelled_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.cancelled_at IS '注销时间';


--
-- Name: COLUMN sys_account.cancelled_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.cancelled_by IS '注销人';


--
-- Name: COLUMN sys_account.cancel_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.cancel_reason IS '注销原因';


--
-- Name: COLUMN sys_account.last_login_ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_ip IS '上次登录IP';


--
-- Name: COLUMN sys_account.last_login_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_address IS '上次登录地点';


--
-- Name: COLUMN sys_account.last_login_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_time IS '上次登录时间';


--
-- Name: COLUMN sys_account.last_login_device; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.last_login_device IS '上次登录设备';


--
-- Name: COLUMN sys_account.latest_login_ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_ip IS '最新登录IP';


--
-- Name: COLUMN sys_account.latest_login_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_address IS '最新登录地点';


--
-- Name: COLUMN sys_account.latest_login_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_time IS '最新登录时间';


--
-- Name: COLUMN sys_account.latest_login_device; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.latest_login_device IS '最新登录设备';


--
-- Name: COLUMN sys_account.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.created_at IS '创建时间';


--
-- Name: COLUMN sys_account.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.created_by IS '创建人';


--
-- Name: COLUMN sys_account.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.updated_at IS '更新时间';


--
-- Name: COLUMN sys_account.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_account_identity.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.id IS '主键';


--
-- Name: COLUMN sys_account_identity.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.account_id IS '账户ID';


--
-- Name: COLUMN sys_account_identity.identity_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.identity_type IS '登录标识类型';


--
-- Name: COLUMN sys_account_identity.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.identifier IS '登录标识';


--
-- Name: COLUMN sys_account_identity.verified; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.verified IS '是否已验证';


--
-- Name: COLUMN sys_account_identity.is_primary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.is_primary IS '是否主标识';


--
-- Name: COLUMN sys_account_identity.bind_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.bind_status IS '绑定状态';


--
-- Name: COLUMN sys_account_identity.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.created_at IS '创建时间';


--
-- Name: COLUMN sys_account_identity.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.created_by IS '创建人';


--
-- Name: COLUMN sys_account_identity.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.updated_at IS '更新时间';


--
-- Name: COLUMN sys_account_identity.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_account_identity.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_banner.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.id IS '主键';


--
-- Name: COLUMN sys_banner.title; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.title IS '标题';


--
-- Name: COLUMN sys_banner.image; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.image IS '图片地址';


--
-- Name: COLUMN sys_banner.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.url IS '跳转地址';


--
-- Name: COLUMN sys_banner.link_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.link_type IS '链接类型：展示图链接类型，对应 BANNER_LINK_TYPE 字典组子项 value。';


--
-- Name: COLUMN sys_banner.summary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.summary IS '摘要';


--
-- Name: COLUMN sys_banner.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.description IS '描述';


--
-- Name: COLUMN sys_banner.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.category IS '分类：展示图分类，对应 BANNER_CATEGORY 字典组子项 value。';


--
-- Name: COLUMN sys_banner.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.type IS '类型：展示图类型，对应 BANNER_TYPE 字典组子项 value。';


--
-- Name: COLUMN sys_banner."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner."position" IS '显示位置：展示图显示位置，对应 BANNER_POSITION 字典组子项 value。';


--
-- Name: COLUMN sys_banner.display_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.display_scope IS '显示端：展示图显示端，对应 BANNER_DISPLAY_SCOPE 字典组子项 value。';


--
-- Name: COLUMN sys_banner.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.sort IS '排序';


--
-- Name: COLUMN sys_banner.interaction_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.interaction_count IS '交互次数';


--
-- Name: COLUMN sys_banner.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.status IS '状态';


--
-- Name: COLUMN sys_banner.start_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.start_at IS '开始展示时间';


--
-- Name: COLUMN sys_banner.end_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.end_at IS '结束展示时间';


--
-- Name: COLUMN sys_banner.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.created_at IS '创建时间';


--
-- Name: COLUMN sys_banner.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.created_by IS '创建人';


--
-- Name: COLUMN sys_banner.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.updated_at IS '更新时间';


--
-- Name: COLUMN sys_banner.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_banner.updated_by IS '更新人';


--
-- Name: sys_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_config (
    id character varying(64) NOT NULL,
    config_key character varying(255) NOT NULL,
    config_value text,
    category character varying(255),
    remark character varying(255),
    sort_code integer DEFAULT 0 NOT NULL,
    ext_json json DEFAULT '{}'::json NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_config OWNER TO postgres;

--
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
-- Name: COLUMN sys_dept.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.id IS '主键';


--
-- Name: COLUMN sys_dept.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.parent_id IS '父部门ID';


--
-- Name: COLUMN sys_dept.master_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.master_id IS '主管ID';


--
-- Name: COLUMN sys_dept.deputy_master_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.deputy_master_id IS '副主管ID';


--
-- Name: COLUMN sys_dept.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.name IS '部门名称';


--
-- Name: COLUMN sys_dept.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.code IS '部门编码';


--
-- Name: COLUMN sys_dept.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.category IS '部门类别';


--
-- Name: COLUMN sys_dept.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.sort IS '排序';


--
-- Name: COLUMN sys_dept.is_virtual; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.is_virtual IS '是否虚拟部门';


--
-- Name: COLUMN sys_dept.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.status IS '状态';


--
-- Name: COLUMN sys_dept.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.extra IS '扩展信息';


--
-- Name: COLUMN sys_dept.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.created_at IS '创建时间';


--
-- Name: COLUMN sys_dept.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.created_by IS '创建人';


--
-- Name: COLUMN sys_dept.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.updated_at IS '更新时间';


--
-- Name: COLUMN sys_dept.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.updated_by IS '更新人';


--
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
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_dict OWNER TO postgres;

--
-- Name: COLUMN sys_dict.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.id IS '主键';


--
-- Name: COLUMN sys_dict.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.code IS '编码';


--
-- Name: COLUMN sys_dict.label; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.label IS '标签';


--
-- Name: COLUMN sys_dict.value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.value IS '值';


--
-- Name: COLUMN sys_dict.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.color IS '颜色';


--
-- Name: COLUMN sys_dict.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.category IS '系统/业务分类：对应 SYS_BIZ_CATEGORY 字典组的取值。';


--
-- Name: COLUMN sys_dict.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.parent_id IS '父级ID';


--
-- Name: COLUMN sys_dict.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.status IS '状态';


--
-- Name: COLUMN sys_dict.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.sort IS '排序';


--
-- Name: COLUMN sys_dict.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.created_at IS '创建时间';


--
-- Name: COLUMN sys_dict.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.created_by IS '创建人';


--
-- Name: COLUMN sys_dict.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.updated_at IS '更新时间';


--
-- Name: COLUMN sys_dict.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_file.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.id IS '主键';


--
-- Name: COLUMN sys_file.object_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.object_name IS '对象存储路径';


--
-- Name: COLUMN sys_file.original_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.original_name IS '原始文件名';


--
-- Name: COLUMN sys_file.storage_provider; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.storage_provider IS '存储服务商';


--
-- Name: COLUMN sys_file.bucket; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.bucket IS '存储桶';


--
-- Name: COLUMN sys_file.content_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.content_type IS '文件类型';


--
-- Name: COLUMN sys_file.size; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.size IS '文件大小';


--
-- Name: COLUMN sys_file.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.url IS '访问地址';


--
-- Name: COLUMN sys_file.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.created_at IS '创建时间';


--
-- Name: COLUMN sys_file.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.created_by IS '创建人';


--
-- Name: COLUMN sys_file.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.updated_at IS '更新时间';


--
-- Name: COLUMN sys_file.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_file.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_group.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.id IS '主键';


--
-- Name: COLUMN sys_group.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.name IS '账户组名称';


--
-- Name: COLUMN sys_group.owner_dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.owner_dept_id IS '所属部门ID';


--
-- Name: COLUMN sys_group.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.description IS '描述';


--
-- Name: COLUMN sys_group.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.status IS '状态';


--
-- Name: COLUMN sys_group.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.extra IS '扩展信息';


--
-- Name: COLUMN sys_group.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.created_at IS '创建时间';


--
-- Name: COLUMN sys_group.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.created_by IS '创建人';


--
-- Name: COLUMN sys_group.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.updated_at IS '更新时间';


--
-- Name: COLUMN sys_group.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_group.updated_by IS '更新人';


--
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
    expired_at timestamp(6) with time zone,
    extra json NOT NULL,
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_iam_relation OWNER TO postgres;

--
-- Name: COLUMN sys_iam_relation.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.id IS '主键';


--
-- Name: COLUMN sys_iam_relation.subject_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.subject_type IS '主体类型';


--
-- Name: COLUMN sys_iam_relation.subject_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.subject_id IS '主体ID';


--
-- Name: COLUMN sys_iam_relation.relation_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.relation_type IS '关系类型';


--
-- Name: COLUMN sys_iam_relation.target_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.target_type IS '目标类型';


--
-- Name: COLUMN sys_iam_relation.target_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.target_id IS '目标ID';


--
-- Name: COLUMN sys_iam_relation.target_key; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.target_key IS '目标标识';


--
-- Name: COLUMN sys_iam_relation.grant_mode; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.grant_mode IS '授权模式';


--
-- Name: COLUMN sys_iam_relation.effect; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.effect IS '授权效果';


--
-- Name: COLUMN sys_iam_relation.data_scope; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.data_scope IS '数据范围';


--
-- Name: COLUMN sys_iam_relation.custom_scope_dept_ids; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.custom_scope_dept_ids IS '自定义数据范围部门ID列表';


--
-- Name: COLUMN sys_iam_relation.is_primary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.is_primary IS '主关系';


--
-- Name: COLUMN sys_iam_relation.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.sort IS '排序';


--
-- Name: COLUMN sys_iam_relation.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.status IS '状态';


--
-- Name: COLUMN sys_iam_relation.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.description IS '描述';


--
-- Name: COLUMN sys_iam_relation.reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.reason IS '授权原因';


--
-- Name: COLUMN sys_iam_relation.expired_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.expired_at IS '失效时间';


--
-- Name: COLUMN sys_iam_relation.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.extra IS '扩展信息';


--
-- Name: COLUMN sys_iam_relation.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.created_at IS '创建时间';


--
-- Name: COLUMN sys_iam_relation.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.created_by IS '创建人';


--
-- Name: COLUMN sys_iam_relation.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.updated_at IS '更新时间';


--
-- Name: COLUMN sys_iam_relation.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_iam_relation.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_operation_audit_log.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.id IS '主键';


--
-- Name: COLUMN sys_operation_audit_log.module; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.module IS '模块';


--
-- Name: COLUMN sys_operation_audit_log.resource_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.resource_type IS '资源类型';


--
-- Name: COLUMN sys_operation_audit_log.resource_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.resource_id IS '资源ID';


--
-- Name: COLUMN sys_operation_audit_log.action; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.action IS '操作';


--
-- Name: COLUMN sys_operation_audit_log.summary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.summary IS '摘要';


--
-- Name: COLUMN sys_operation_audit_log.before_data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.before_data IS '变更前数据';


--
-- Name: COLUMN sys_operation_audit_log.after_data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.after_data IS '变更后数据';


--
-- Name: COLUMN sys_operation_audit_log.account_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.account_id IS '操作账号ID';


--
-- Name: COLUMN sys_operation_audit_log.account_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.account_type IS '操作账号类型';


--
-- Name: COLUMN sys_operation_audit_log.request_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.request_id IS '请求ID';


--
-- Name: COLUMN sys_operation_audit_log.ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.ip IS '客户端IP';


--
-- Name: COLUMN sys_operation_audit_log.user_agent; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.user_agent IS 'User-Agent';


--
-- Name: COLUMN sys_operation_audit_log.success; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.success IS '是否成功';


--
-- Name: COLUMN sys_operation_audit_log.error_message; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.error_message IS '错误信息';


--
-- Name: COLUMN sys_operation_audit_log.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_operation_audit_log.created_at IS '创建时间';


--
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
-- Name: COLUMN sys_position.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.id IS '主键';


--
-- Name: COLUMN sys_position.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.name IS '职位名称';


--
-- Name: COLUMN sys_position.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.code IS '职位编码';


--
-- Name: COLUMN sys_position.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.category IS '职位类别';


--
-- Name: COLUMN sys_position.owner_dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.owner_dept_id IS '所属部门ID';


--
-- Name: COLUMN sys_position.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.sort IS '排序';


--
-- Name: COLUMN sys_position.is_virtual; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.is_virtual IS '是否虚拟职位';


--
-- Name: COLUMN sys_position.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.status IS '状态';


--
-- Name: COLUMN sys_position.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.description IS '职位描述';


--
-- Name: COLUMN sys_position.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.extra IS '扩展信息';


--
-- Name: COLUMN sys_position.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.created_at IS '创建时间';


--
-- Name: COLUMN sys_position.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.created_by IS '创建人';


--
-- Name: COLUMN sys_position.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.updated_at IS '更新时间';


--
-- Name: COLUMN sys_position.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_position.updated_by IS '更新人';


--
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
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_resource OWNER TO postgres;

--
-- Name: COLUMN sys_resource.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.id IS '主键';


--
-- Name: COLUMN sys_resource.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.parent_id IS '父资源ID';


--
-- Name: COLUMN sys_resource.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.code IS '资源编码';


--
-- Name: COLUMN sys_resource.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.name IS '资源名称';


--
-- Name: COLUMN sys_resource.resource_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.resource_type IS '资源类型';


--
-- Name: COLUMN sys_resource.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.module_id IS '所属资源模块ID';


--
-- Name: COLUMN sys_resource.path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.path IS '路由路径';


--
-- Name: COLUMN sys_resource.component; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.component IS '前端组件';


--
-- Name: COLUMN sys_resource.redirect; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.redirect IS '重定向地址';


--
-- Name: COLUMN sys_resource.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.icon IS '图标';


--
-- Name: COLUMN sys_resource.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.color IS '颜色';


--
-- Name: COLUMN sys_resource.href; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.href IS '外链地址';


--
-- Name: COLUMN sys_resource.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.sort IS '排序';


--
-- Name: COLUMN sys_resource.is_visible; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.is_visible IS '是否可见';


--
-- Name: COLUMN sys_resource.is_cache; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.is_cache IS '是否缓存';


--
-- Name: COLUMN sys_resource.is_affix; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.is_affix IS '是否固定标签';


--
-- Name: COLUMN sys_resource.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.status IS '状态';


--
-- Name: COLUMN sys_resource.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.description IS '描述';


--
-- Name: COLUMN sys_resource.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.extra IS '扩展信息';


--
-- Name: COLUMN sys_resource.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.created_at IS '创建时间';


--
-- Name: COLUMN sys_resource.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.created_by IS '创建人';


--
-- Name: COLUMN sys_resource.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.updated_at IS '更新时间';


--
-- Name: COLUMN sys_resource.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource.updated_by IS '更新人';


--
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
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    updated_by character varying(64)
);


ALTER TABLE public.sys_resource_module OWNER TO postgres;

--
-- Name: COLUMN sys_resource_module.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.id IS '主键';


--
-- Name: COLUMN sys_resource_module.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.name IS '模块名称';


--
-- Name: COLUMN sys_resource_module.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.code IS '模块编码';


--
-- Name: COLUMN sys_resource_module.client; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.client IS '所属端';


--
-- Name: COLUMN sys_resource_module.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.icon IS '图标';


--
-- Name: COLUMN sys_resource_module.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.color IS '颜色';


--
-- Name: COLUMN sys_resource_module.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.sort IS '排序';


--
-- Name: COLUMN sys_resource_module.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.status IS '状态';


--
-- Name: COLUMN sys_resource_module.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.description IS '描述';


--
-- Name: COLUMN sys_resource_module.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.extra IS '扩展信息';


--
-- Name: COLUMN sys_resource_module.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.created_at IS '创建时间';


--
-- Name: COLUMN sys_resource_module.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.created_by IS '创建人';


--
-- Name: COLUMN sys_resource_module.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.updated_at IS '更新时间';


--
-- Name: COLUMN sys_resource_module.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_resource_module.updated_by IS '更新人';


--
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
-- Name: COLUMN sys_role.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.id IS '主键';


--
-- Name: COLUMN sys_role.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.code IS '角色编码';


--
-- Name: COLUMN sys_role.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.name IS '角色名称';


--
-- Name: COLUMN sys_role.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.category IS '角色分类';


--
-- Name: COLUMN sys_role.scope_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.scope_type IS '角色作用域类型';


--
-- Name: COLUMN sys_role.owner_dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.owner_dept_id IS '所属部门ID';


--
-- Name: COLUMN sys_role.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.sort IS '排序';


--
-- Name: COLUMN sys_role.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.status IS '状态';


--
-- Name: COLUMN sys_role.is_builtin; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.is_builtin IS '是否内置角色';


--
-- Name: COLUMN sys_role.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.description IS '描述';


--
-- Name: COLUMN sys_role.extra; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.extra IS '扩展信息';


--
-- Name: COLUMN sys_role.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.created_at IS '创建时间';


--
-- Name: COLUMN sys_role.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.created_by IS '创建人';


--
-- Name: COLUMN sys_role.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.updated_at IS '更新时间';


--
-- Name: COLUMN sys_role.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.updated_by IS '更新人';


--
-- Data for Name: admin_user_profile; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.admin_user_profile (account_id, name, nickname, avatar, signature, phone, email, title, employee_no, remark, created_at, created_by, updated_at, updated_by) FROM stdin;
1	超级管理员	超管	\N	\N	\N	\N	Super Admin	SA-0001	系统内置超管账户	2026-07-04 03:00:01.332229+00	\N	2026-07-04 03:00:01.332229+00	\N
\.


--
-- Data for Name: alembic_version; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.alembic_version (version_num) FROM stdin;
c12e5b0797c1
\.


--
-- Data for Name: msg_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_group (id, name, owner_account_type, owner_account_id, avatar, status, description, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: msg_group_member; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_group_member (id, group_id, account_type, account_id, nickname, is_muted, joined_at, left_at) FROM stdin;
\.


--
-- Data for Name: msg_message; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_message (id, thread_id, parent_id, sender_type, sender_account_type, sender_account_id, sender_name, content, content_type, reply_count, is_revoked, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: msg_message_attachment; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_message_attachment (id, message_id, name, url, content_type, size, sort, extra) FROM stdin;
\.


--
-- Data for Name: msg_message_reaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_message_reaction (id, message_id, account_type, account_id, reaction, created_at) FROM stdin;
\.


--
-- Data for Name: msg_message_receipt; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_message_receipt (id, message_id, thread_id, account_type, account_id, read_at) FROM stdin;
\.


--
-- Data for Name: msg_notification; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_notification (id, title, content, content_type, severity, target_scope, target_account_type, target_account_id, sender_account_type, sender_account_id, status, publish_at, revoked_at, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
7481646581863813120	的561230.	8561230.	TEXT	INFO	ALL	ADMIN	\N	ADMIN	1	PUBLISHED	2026-07-11 09:53:06.405046+00	\N	{}	2026-07-11 09:52:28.781985+00	1	2026-07-12 12:22:17.005363+00	1
\.


--
-- Data for Name: msg_notification_read; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_notification_read (id, notification_id, account_type, account_id, read_at) FROM stdin;
7482024020376948736	7481646581863813120	ADMIN	1	2026-07-12 10:52:15.739172+00
\.


--
-- Data for Name: msg_thread; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_thread (id, thread_type, title, group_id, created_account_type, created_account_id, status, last_message_id, last_message_at, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: msg_thread_participant; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_thread_participant (id, thread_id, account_type, account_id, unread_count, last_read_message_id, last_read_at, is_muted, joined_at, left_at) FROM stdin;
\.


--
-- Data for Name: msg_todo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_todo (id, title, content, content_type, priority, target_scope, target_account_type, target_account_id, creator_account_type, creator_account_id, source_type, source_id, status, due_at, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: msg_todo_assignee; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.msg_todo_assignee (id, todo_id, account_type, account_id, status, read_at, started_at, completed_at, cancelled_at) FROM stdin;
\.


--
-- Data for Name: portal_user_profile; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.portal_user_profile (account_id, name, nickname, avatar, signature, phone, email, bio, level, created_at, created_by, updated_at, updated_by) FROM stdin;
7481668347524943872	\N	user123	avatars/portal/7481668347524943872/avatar-20260711112538-6c2517731f69460c8658773da659382f.png	\N	\N	user123@163.com	\N	\N	2026-07-11 11:18:56.477172+00	\N	2026-07-11 11:25:39.469494+00	7481668347524943872
\.


--
-- Data for Name: sys_account; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_account (id, password_hash, account_type, account_status, cancelled_at, cancelled_by, cancel_reason, last_login_ip, last_login_address, last_login_time, last_login_device, latest_login_ip, latest_login_address, latest_login_time, latest_login_device, created_at, created_by, updated_at, updated_by) FROM stdin;
1	$2b$12$XuU0Hu/.pBTVhwyp73hHU.3lrSLZM0avYsGzjAOW/ERMnq2fxSS1G	ADMIN	ENABLED	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-07-04 03:00:01.332229+00	\N	2026-07-04 03:00:01.332229+00	\N
7481668347524943872	$2b$12$FLT.bU5o/6zLzoMn5zBE/OIaX48wHBQuqS6SBRMMNygBnG2I.b3Oe	PORTAL	ENABLED	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-07-11 11:18:56.477172+00	\N	2026-07-11 11:18:56.477172+00	\N
\.


--
-- Data for Name: sys_account_identity; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_account_identity (id, account_id, identity_type, identifier, verified, is_primary, bind_status, created_at, created_by, updated_at, updated_by) FROM stdin;
1	1	ACCOUNT	superadmin	t	t	BOUND	2026-07-04 03:00:01.332229+00	\N	2026-07-04 03:00:01.332229+00	\N
7481668349685010432	7481668347524943872	ACCOUNT	user123	t	t	BOUND	2026-07-11 11:18:56.477172+00	\N	2026-07-11 11:18:56.477172+00	\N
7481668349685010433	7481668347524943872	EMAIL	user123@163.com	t	f	BOUND	2026-07-11 11:18:56.477172+00	\N	2026-07-11 11:18:56.477172+00	\N
\.


--
-- Data for Name: sys_banner; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_banner (id, title, image, url, link_type, summary, description, category, type, "position", display_scope, sort, interaction_count, status, start_at, end_at, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: sys_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_config (id, config_key, config_value, category, remark, sort_code, ext_json, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: sys_dept; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_dept (id, parent_id, master_id, deputy_master_id, name, code, category, sort, is_virtual, status, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: sys_dict; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_dict (id, code, label, value, color, category, parent_id, status, sort, created_at, created_by, updated_at, updated_by) FROM stdin;
100001	COMMON_STATUS	状态	COMMON_STATUS	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100002	COMMON_STATUS_ENABLED	启用	ENABLED	#18a058	SYS	100001	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100003	COMMON_STATUS_DISABLED	禁用	DISABLED	#d03050	SYS	100001	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100004	SYS_BIZ_CATEGORY	系统/业务分类	SYS_BIZ_CATEGORY	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100005	SYS_BIZ_CATEGORY_SYS	系统	SYS	#2080f0	SYS	100004	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100006	SYS_BIZ_CATEGORY_BIZ	业务	BIZ	#f0a020	SYS	100004	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100007	ACCOUNT_TYPE	账号类型	ACCOUNT_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100008	ACCOUNT_TYPE_ADMIN	管理端	ADMIN	#722ed1	SYS	100007	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100009	ACCOUNT_TYPE_PORTAL	门户端	PORTAL	#18a058	SYS	100007	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100010	ACCOUNT_STATUS	账号状态	ACCOUNT_STATUS	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100011	ACCOUNT_STATUS_ENABLED	启用	ENABLED	#18a058	SYS	100010	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100012	ACCOUNT_STATUS_DISABLED	禁用	DISABLED	#d03050	SYS	100010	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100013	ACCOUNT_STATUS_CANCELLED	已注销	CANCELLED	#909399	SYS	100010	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100014	ROLE_SCOPE_TYPE	角色范围类型	ROLE_SCOPE_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100015	ROLE_SCOPE_TYPE_PLATFORM	平台	PLATFORM	#2080f0	SYS	100014	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100016	ROLE_SCOPE_TYPE_DEPT	部门	DEPT	#18a058	SYS	100014	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100017	RESOURCE_TYPE	资源类型	RESOURCE_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100018	RESOURCE_TYPE_CATALOG	目录	CATALOG	#722ed1	SYS	100017	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100019	RESOURCE_TYPE_MENU	菜单	MENU	#2080f0	SYS	100017	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100020	RESOURCE_TYPE_PAGE	页面	PAGE	#18a058	SYS	100017	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100021	RESOURCE_TYPE_BUTTON	按钮	BUTTON	#f0a020	SYS	100017	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100022	RESOURCE_TYPE_ACTION	操作	ACTION	#d03050	SYS	100017	ENABLED	5	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100023	RESOURCE_TYPE_API_GROUP	接口组	API_GROUP	#1677ff	SYS	100017	ENABLED	6	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100024	DATA_SCOPE	数据范围	DATA_SCOPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100025	DATA_SCOPE_ALL	全部	ALL	#18a058	SYS	100024	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100026	DATA_SCOPE_DEPT_AND_CHILD	本部门及子部门	DEPT_AND_CHILD	#2080f0	SYS	100024	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100027	DATA_SCOPE_DEPT	本部门	DEPT	#2db7f5	SYS	100024	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100028	DATA_SCOPE_SELF	本人	SELF	#f0a020	SYS	100024	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100029	DATA_SCOPE_CUSTOM	自定义部门	CUSTOM	#722ed1	SYS	100024	ENABLED	5	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100030	GRANT_SUBJECT_TYPE	授权主体类型	GRANT_SUBJECT_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100031	GRANT_SUBJECT_TYPE_ROLE	角色	ROLE	#2080f0	SYS	100030	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100032	GRANT_SUBJECT_TYPE_ACCOUNT	账号	ACCOUNT	#18a058	SYS	100030	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100033	GRANT_SUBJECT_TYPE_GROUP	用户组	GROUP	#f0a020	SYS	100030	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100034	GRANT_MODE	授权模式	GRANT_MODE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100035	GRANT_MODE_DIRECT	直接授权	DIRECT	#2080f0	SYS	100034	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100036	GRANT_MODE_CASCADE	级联授权	CASCADE	#18a058	SYS	100034	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100037	GRANT_EFFECT	授权效果	GRANT_EFFECT	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100038	GRANT_EFFECT_ALLOW	允许	ALLOW	#18a058	SYS	100037	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100039	GRANT_EFFECT_DENY	拒绝	DENY	#d03050	SYS	100037	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100040	DEPT_CATEGORY	部门分类	DEPT_CATEGORY	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100041	DEPT_CATEGORY_COMPANY	公司	COMPANY	#2080f0	SYS	100040	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100042	DEPT_CATEGORY_DEPARTMENT	部门	DEPARTMENT	#18a058	SYS	100040	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100043	DEPT_CATEGORY_TEAM	团队	TEAM	#f0a020	SYS	100040	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100044	DEPT_CATEGORY_VIRTUAL	虚拟组织	VIRTUAL	#909399	SYS	100040	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100045	POSITION_CATEGORY	岗位分类	POSITION_CATEGORY	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100046	POSITION_CATEGORY_MANAGEMENT	管理	MANAGEMENT	#2080f0	SYS	100045	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100047	POSITION_CATEGORY_TECHNICAL	技术	TECHNICAL	#18a058	SYS	100045	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100048	POSITION_CATEGORY_OPERATION	运营	OPERATION	#f0a020	SYS	100045	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100049	POSITION_CATEGORY_SUPPORT	支持	SUPPORT	#909399	SYS	100045	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100050	BANNER_DISPLAY_SCOPE	展示图展示范围	BANNER_DISPLAY_SCOPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100051	BANNER_DISPLAY_SCOPE_PORTAL	门户端	PORTAL	#18a058	SYS	100050	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100052	BANNER_DISPLAY_SCOPE_ADMIN	管理端	ADMIN	#2080f0	SYS	100050	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100053	BANNER_DISPLAY_SCOPE_APP	移动端	APP	#f0a020	SYS	100050	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100054	BANNER_CATEGORY	展示图分类	BANNER_CATEGORY	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100055	BANNER_CATEGORY_HOME	首页	HOME	#18a058	SYS	100054	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100056	BANNER_CATEGORY_LOGIN	登录	LOGIN	#2080f0	SYS	100054	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100057	BANNER_CATEGORY_WORKPLACE	工作台	WORKPLACE	#722ed1	SYS	100054	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100058	BANNER_CATEGORY_NOTICE	公告	NOTICE	#f0a020	SYS	100054	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100059	BANNER_CATEGORY_ADMIN_DASHBOARD	管理端仪表盘	ADMIN_DASHBOARD	#2080f0	SYS	100054	ENABLED	5	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100060	BANNER_CATEGORY_SYSTEM_UPGRADE	系统升级	SYSTEM_UPGRADE	#d03050	SYS	100054	ENABLED	6	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100061	BANNER_TYPE	展示图类型	BANNER_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100062	BANNER_TYPE_CAROUSEL	轮播图	CAROUSEL	#18a058	SYS	100061	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100063	BANNER_TYPE_HERO	主视觉	HERO	#2080f0	SYS	100061	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100064	BANNER_TYPE_NOTICE	公告	NOTICE	#f0a020	SYS	100061	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100065	BANNER_TYPE_CARD	卡片	CARD	#722ed1	SYS	100061	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100066	BANNER_TYPE_POPUP	弹窗	POPUP	#d03050	SYS	100061	ENABLED	5	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100067	BANNER_TYPE_SIDEBAR	侧边栏	SIDEBAR	#2080f0	SYS	100061	ENABLED	6	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100068	BANNER_POSITION	展示图位置	BANNER_POSITION	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100069	BANNER_POSITION_HOME_TOP	首页顶部	HOME_TOP	#18a058	SYS	100068	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100070	BANNER_POSITION_HOME_MIDDLE	首页中部	HOME_MIDDLE	#18a058	SYS	100068	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100071	BANNER_POSITION_HOME_BOTTOM	首页底部	HOME_BOTTOM	#18a058	SYS	100068	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100072	BANNER_POSITION_LOGIN_SIDE	登录侧边	LOGIN_SIDE	#2080f0	SYS	100068	ENABLED	4	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100073	BANNER_POSITION_WORKPLACE_TOP	工作台顶部	WORKPLACE_TOP	#722ed1	SYS	100068	ENABLED	5	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100074	BANNER_POSITION_NOTICE_AREA	公告区域	NOTICE_AREA	#f0a020	SYS	100068	ENABLED	6	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100075	BANNER_POSITION_ADMIN_TOP	管理端顶部	ADMIN_TOP	#2080f0	SYS	100068	ENABLED	7	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100076	BANNER_POSITION_ADMIN_SIDEBAR	管理端侧边栏	ADMIN_SIDEBAR	#2080f0	SYS	100068	ENABLED	8	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100077	BANNER_LINK_TYPE	展示图链接类型	BANNER_LINK_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100078	BANNER_LINK_TYPE_URL	外部链接	URL	#18a058	SYS	100077	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100079	BANNER_LINK_TYPE_ROUTE	路由	ROUTE	#2080f0	SYS	100077	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100080	BANNER_LINK_TYPE_NONE	无链接	NONE	#909399	SYS	100077	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100081	ACCOUNT_IDENTITY_TYPE	账号身份类型	ACCOUNT_IDENTITY_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100082	ACCOUNT_IDENTITY_TYPE_ACCOUNT	登录账号	ACCOUNT	#2080f0	SYS	100081	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100083	ACCOUNT_IDENTITY_TYPE_EMAIL	邮箱	EMAIL	#18a058	SYS	100081	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100084	ACCOUNT_IDENTITY_TYPE_PHONE	手机号	PHONE	#f0a020	SYS	100081	ENABLED	3	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100085	ACCOUNT_IDENTITY_BIND_STATUS	账号身份绑定状态	ACCOUNT_IDENTITY_BIND_STATUS	#2080f0	SYS	\N	ENABLED	0	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100086	ACCOUNT_IDENTITY_BIND_STATUS_BOUND	已绑定	BOUND	#18a058	SYS	100085	ENABLED	1	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100087	ACCOUNT_IDENTITY_BIND_STATUS_UNBOUND	未绑定	UNBOUND	#909399	SYS	100085	ENABLED	2	2026-06-29 00:00:00+00	\N	2026-06-29 00:00:00+00	\N
100088	MESSAGE_TARGET_SCOPE	消息目标范围	MESSAGE_TARGET_SCOPE	#2080f0	SYS	\N	ENABLED	0	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100089	MESSAGE_TARGET_SCOPE_ALL	全部	ALL	#18a058	SYS	100088	ENABLED	1	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100090	MESSAGE_TARGET_SCOPE_SPECIFIC	指定账号	SPECIFIC	#2080f0	SYS	100088	ENABLED	2	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100091	NOTIFICATION_STATUS	通知状态	NOTIFICATION_STATUS	#2080f0	SYS	\N	ENABLED	0	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100092	NOTIFICATION_STATUS_DRAFT	草稿	DRAFT	#909399	SYS	100091	ENABLED	1	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100093	NOTIFICATION_STATUS_PUBLISHED	已发布	PUBLISHED	#18a058	SYS	100091	ENABLED	2	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100094	NOTIFICATION_STATUS_REVOKED	已撤回	REVOKED	#d03050	SYS	100091	ENABLED	3	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100095	NOTIFICATION_SEVERITY	通知严重级别	NOTIFICATION_SEVERITY	#2080f0	SYS	\N	ENABLED	0	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100096	NOTIFICATION_SEVERITY_INFO	信息	INFO	#2080f0	SYS	100095	ENABLED	1	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100097	NOTIFICATION_SEVERITY_SUCCESS	成功	SUCCESS	#18a058	SYS	100095	ENABLED	2	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100098	NOTIFICATION_SEVERITY_WARNING	警告	WARNING	#f0a020	SYS	100095	ENABLED	3	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100099	NOTIFICATION_SEVERITY_ERROR	错误	ERROR	#d03050	SYS	100095	ENABLED	4	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100100	MESSAGE_THREAD_TYPE	消息会话类型	MESSAGE_THREAD_TYPE	#2080f0	SYS	\N	ENABLED	0	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100101	MESSAGE_THREAD_TYPE_DIRECT	单聊	DIRECT	#2080f0	SYS	100100	ENABLED	1	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100102	MESSAGE_THREAD_TYPE_GROUP	群聊	GROUP	#18a058	SYS	100100	ENABLED	2	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100103	MESSAGE_THREAD_TYPE_SYSTEM	系统	SYSTEM	#722ed1	SYS	100100	ENABLED	3	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100104	TODO_PRIORITY	待办优先级	TODO_PRIORITY	#2080f0	SYS	\N	ENABLED	0	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100105	TODO_PRIORITY_LOW	低	LOW	#909399	SYS	100104	ENABLED	1	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100106	TODO_PRIORITY_NORMAL	普通	NORMAL	#2080f0	SYS	100104	ENABLED	2	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100107	TODO_PRIORITY_HIGH	高	HIGH	#f0a020	SYS	100104	ENABLED	3	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100108	TODO_PRIORITY_URGENT	紧急	URGENT	#d03050	SYS	100104	ENABLED	4	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100109	TODO_STATUS	待办状态	TODO_STATUS	#2080f0	SYS	\N	ENABLED	0	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100110	TODO_STATUS_PENDING	待处理	PENDING	#909399	SYS	100109	ENABLED	1	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100111	TODO_STATUS_IN_PROGRESS	进行中	IN_PROGRESS	#2080f0	SYS	100109	ENABLED	2	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100112	TODO_STATUS_COMPLETED	已完成	COMPLETED	#18a058	SYS	100109	ENABLED	3	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100113	TODO_STATUS_CANCELLED	已注销	CANCELLED	#d03050	SYS	100109	ENABLED	4	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
100114	STORAGE_PROVIDER	存储提供商	STORAGE_PROVIDER	#2080f0	SYS	\N	ENABLED	0	2026-07-02 00:00:00+00	\N	2026-07-02 00:00:00+00	\N
100115	STORAGE_PROVIDER_LOCAL	本地	local	#18a058	SYS	100114	ENABLED	1	2026-07-02 00:00:00+00	\N	2026-07-02 00:00:00+00	\N
100116	STORAGE_PROVIDER_MINIO	MinIO	minio	#2080f0	SYS	100114	ENABLED	2	2026-07-02 00:00:00+00	\N	2026-07-02 00:00:00+00	\N
100117	STORAGE_PROVIDER_S3	亚马逊 S3	s3	#722ed1	SYS	100114	ENABLED	3	2026-07-02 00:00:00+00	\N	2026-07-02 00:00:00+00	\N
100118	STORAGE_PROVIDER_OSS	阿里云 OSS	oss	#f0a020	SYS	100114	ENABLED	4	2026-07-02 00:00:00+00	\N	2026-07-02 00:00:00+00	\N
100119	RESOURCE_MODULE_CLIENT	资源模块客户端	RESOURCE_MODULE_CLIENT	#2080f0	SYS	\N	ENABLED	0	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
100120	RESOURCE_MODULE_CLIENT_ADMIN	管理端	ADMIN	#722ed1	SYS	100119	ENABLED	1	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
100121	RESOURCE_MODULE_CLIENT_PORTAL	门户端	PORTAL	#18a058	SYS	100119	ENABLED	2	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
\.


--
-- Data for Name: sys_file; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_file (id, object_name, original_name, storage_provider, bucket, content_type, size, url, created_at, created_by, updated_at, updated_by) FROM stdin;
7481646308676210688	uploads/2026/07/11/nano-ai-1781800719925-54c6c4a4fc7647818ee78eb6c39560b6.png	nano-ai-1781800719925.png	local	\N	image/png	1330442	/api/v1/files/uploads/2026/07/11/nano-ai-1781800719925-54c6c4a4fc7647818ee78eb6c39560b6.png	2026-07-11 09:51:23.624802+00	1	2026-07-11 09:51:23.624802+00	1
7481670034289463296	avatars/portal/7481668347524943872/avatar-20260711112538-6c2517731f69460c8658773da659382f.png	avatar-20260711112538-6c2517731f69460c8658773da659382f.png	local	\N	image/png	851224	/api/v1/files/avatars/portal/7481668347524943872/avatar-20260711112538-6c2517731f69460c8658773da659382f.png	2026-07-11 11:25:38.673476+00	7481668347524943872	2026-07-11 11:25:38.673476+00	7481668347524943872
7484225859625291776	uploads/2026/07/18/【哲风壁纸】原神-芙芙-e4c179ebb2154e219b5cfc33c47881d2.png	【哲风壁纸】原神-芙芙.png	local	\N	image/png	8411606	/api/v1/files/uploads/2026/07/18/%E3%80%90%E5%93%B2%E9%A3%8E%E5%A3%81%E7%BA%B8%E3%80%91%E5%8E%9F%E7%A5%9E-%E8%8A%99%E8%8A%99-e4c179ebb2154e219b5cfc33c47881d2.png	2026-07-18 12:41:33.658725+00	1	2026-07-18 12:41:33.658725+00	1
7484226037195345920	uploads/2026/07/18/【哲风壁纸】原神-芙芙-5610a658d71745468385b538611ae725.png	【哲风壁纸】原神-芙芙.png	local	\N	image/png	8411606	/api/v1/files/uploads/2026/07/18/%E3%80%90%E5%93%B2%E9%A3%8E%E5%A3%81%E7%BA%B8%E3%80%91%E5%8E%9F%E7%A5%9E-%E8%8A%99%E8%8A%99-5610a658d71745468385b538611ae725.png	2026-07-18 12:42:16.003308+00	1	2026-07-18 12:42:16.003308+00	1
7484236400376811520	uploads/2026/07/18/120153703_touxiang_bobopic-c4bd66eb5bbb416b9b6b893afa954e03.jpg	120153703_touxiang_bobopic.jpg	local	\N	image/jpeg	65451	/api/v1/files/uploads/2026/07/18/120153703_touxiang_bobopic-c4bd66eb5bbb416b9b6b893afa954e03.jpg	2026-07-18 13:23:26.808802+00	1	2026-07-18 13:23:26.808802+00	1
7484239276931158016	uploads/2026/07/18/120153703_touxiang_bobopic-05cbe4a5ff7544a8a02cf21f05c5e3a5.jpg	120153703_touxiang_bobopic.jpg	local	\N	image/jpeg	65451	/api/v1/files/uploads/2026/07/18/120153703_touxiang_bobopic-05cbe4a5ff7544a8a02cf21f05c5e3a5.jpg	2026-07-18 13:34:52.6381+00	1	2026-07-18 13:34:52.6381+00	1
\.


--
-- Data for Name: sys_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_group (id, name, owner_dept_id, description, status, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
7482027972501835776	才对	\N	吃撒	ENABLED	{}	2026-07-12 11:07:58.231526+00	1	2026-07-12 11:07:58.231526+00	1
\.


--
-- Data for Name: sys_iam_relation; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_iam_relation (id, subject_type, subject_id, relation_type, target_type, target_id, target_key, grant_mode, effect, data_scope, custom_scope_dept_ids, is_primary, sort, status, description, reason, expired_at, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
400001	RESOURCE	200001	RESOURCE_PERMISSION	PERMISSION		dashboard:overview:view	CASCADE	ALLOW	ALL	[]	f	1	ENABLED	工作台查看	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400002	RESOURCE	200004	RESOURCE_PERMISSION	PERMISSION		sys:dict:page	CASCADE	ALLOW	ALL	[]	f	10	ENABLED	字典分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400003	RESOURCE	200027	RESOURCE_PERMISSION	PERMISSION		sys:audit:page	CASCADE	ALLOW	ALL	[]	f	11	ENABLED	操作审计分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400004	RESOURCE	200027	RESOURCE_PERMISSION	PERMISSION		sys:audit:detail	CASCADE	ALLOW	ALL	[]	f	12	ENABLED	操作审计详情	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400005	RESOURCE	201011	RESOURCE_PERMISSION	PERMISSION		sys:dict:create	CASCADE	ALLOW	ALL	[]	f	20	ENABLED	新增字典	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400006	RESOURCE	201012	RESOURCE_PERMISSION	PERMISSION		sys:dict:detail	CASCADE	ALLOW	ALL	[]	f	21	ENABLED	查看字典	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400007	RESOURCE	201013	RESOURCE_PERMISSION	PERMISSION		sys:dict:update	CASCADE	ALLOW	ALL	[]	f	22	ENABLED	编辑字典	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400008	RESOURCE	201014	RESOURCE_PERMISSION	PERMISSION		sys:dict:delete	CASCADE	ALLOW	ALL	[]	f	23	ENABLED	删除字典	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400009	RESOURCE	200005	RESOURCE_PERMISSION	PERMISSION		sys:banner:page	CASCADE	ALLOW	ALL	[]	f	30	ENABLED	展示图分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400010	RESOURCE	201021	RESOURCE_PERMISSION	PERMISSION		sys:banner:create	CASCADE	ALLOW	ALL	[]	f	40	ENABLED	新增展示图	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400011	RESOURCE	201022	RESOURCE_PERMISSION	PERMISSION		sys:banner:detail	CASCADE	ALLOW	ALL	[]	f	41	ENABLED	查看展示图	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400012	RESOURCE	201023	RESOURCE_PERMISSION	PERMISSION		sys:banner:update	CASCADE	ALLOW	ALL	[]	f	42	ENABLED	编辑展示图	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400013	RESOURCE	201024	RESOURCE_PERMISSION	PERMISSION		sys:banner:delete	CASCADE	ALLOW	ALL	[]	f	43	ENABLED	删除展示图	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400014	RESOURCE	200023	RESOURCE_PERMISSION	PERMISSION		sys:file:page	CASCADE	ALLOW	ALL	[]	f	50	ENABLED	文件分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400015	RESOURCE	201031	RESOURCE_PERMISSION	PERMISSION		sys:file:upload	CASCADE	ALLOW	ALL	[]	f	60	ENABLED	上传文件	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400016	RESOURCE	201032	RESOURCE_PERMISSION	PERMISSION		sys:file:detail	CASCADE	ALLOW	ALL	[]	f	61	ENABLED	查看文件	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400017	RESOURCE	201033	RESOURCE_PERMISSION	PERMISSION		sys:file:update	CASCADE	ALLOW	ALL	[]	f	62	ENABLED	编辑文件	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400018	RESOURCE	201034	RESOURCE_PERMISSION	PERMISSION		sys:file:url	CASCADE	ALLOW	ALL	[]	f	63	ENABLED	打开文件	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400019	RESOURCE	201034	RESOURCE_PERMISSION	PERMISSION		sys:file:presignedurl	CASCADE	ALLOW	ALL	[]	f	64	ENABLED	获取文件预签名地址	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400020	RESOURCE	201035	RESOURCE_PERMISSION	PERMISSION		sys:file:delete	CASCADE	ALLOW	ALL	[]	f	65	ENABLED	删除文件	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400021	RESOURCE	200025	RESOURCE_PERMISSION	PERMISSION		auth:session:analysis	CASCADE	ALLOW	ALL	[]	f	70	ENABLED	会话分析	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400022	RESOURCE	200025	RESOURCE_PERMISSION	PERMISSION		auth:session:page	CASCADE	ALLOW	ALL	[]	f	71	ENABLED	会话分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400023	RESOURCE	201041	RESOURCE_PERMISSION	PERMISSION		auth:session:tokenlist	CASCADE	ALLOW	ALL	[]	f	80	ENABLED	查看令牌	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400024	RESOURCE	201042	RESOURCE_PERMISSION	PERMISSION		auth:session:exit	CASCADE	ALLOW	ALL	[]	f	81	ENABLED	强退账号	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400025	RESOURCE	201043	RESOURCE_PERMISSION	PERMISSION		auth:session:tokenexit	CASCADE	ALLOW	ALL	[]	f	82	ENABLED	强退令牌	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400026	RESOURCE	200007	RESOURCE_PERMISSION	PERMISSION		iam:account:page	CASCADE	ALLOW	ALL	[]	f	100	ENABLED	账号分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400027	RESOURCE	201101	RESOURCE_PERMISSION	PERMISSION		iam:account:create	CASCADE	ALLOW	ALL	[]	f	110	ENABLED	新增账号	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400028	RESOURCE	201102	RESOURCE_PERMISSION	PERMISSION		iam:account:detail	CASCADE	ALLOW	ALL	[]	f	111	ENABLED	查看账号	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400029	RESOURCE	201103	RESOURCE_PERMISSION	PERMISSION		iam:account:update	CASCADE	ALLOW	ALL	[]	f	112	ENABLED	编辑账号	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400030	RESOURCE	201104	RESOURCE_PERMISSION	PERMISSION		iam:account:delete	CASCADE	ALLOW	ALL	[]	f	113	ENABLED	删除账号	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400031	RESOURCE	201105	RESOURCE_PERMISSION	PERMISSION		iam:account:ownrole	CASCADE	ALLOW	ALL	[]	f	114	ENABLED	获取账号角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400032	RESOURCE	201105	RESOURCE_PERMISSION	PERMISSION		iam:account:grantrole	CASCADE	ALLOW	ALL	[]	f	115	ENABLED	分配账号角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400033	RESOURCE	201106	RESOURCE_PERMISSION	PERMISSION		iam:account:owngroup	CASCADE	ALLOW	ALL	[]	f	116	ENABLED	获取账号用户组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400034	RESOURCE	201106	RESOURCE_PERMISSION	PERMISSION		iam:account:grantgroup	CASCADE	ALLOW	ALL	[]	f	117	ENABLED	分配账号用户组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400035	RESOURCE	201107	RESOURCE_PERMISSION	PERMISSION		iam:account:owndept	CASCADE	ALLOW	ALL	[]	f	118	ENABLED	获取账号部门	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400036	RESOURCE	201107	RESOURCE_PERMISSION	PERMISSION		iam:account:grantdept	CASCADE	ALLOW	ALL	[]	f	119	ENABLED	分配账号部门	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400037	RESOURCE	201107	RESOURCE_PERMISSION	PERMISSION		iam:dept:list	CASCADE	ALLOW	ALL	[]	f	120	ENABLED	部门树选择	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400038	RESOURCE	201108	RESOURCE_PERMISSION	PERMISSION		iam:account:ownresource	CASCADE	ALLOW	ALL	[]	f	121	ENABLED	获取账号资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400039	RESOURCE	201108	RESOURCE_PERMISSION	PERMISSION		iam:account:grantresource	CASCADE	ALLOW	ALL	[]	f	122	ENABLED	分配账号资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400040	RESOURCE	201109	RESOURCE_PERMISSION	PERMISSION		iam:account:ownpermission	CASCADE	ALLOW	ALL	[]	f	123	ENABLED	获取账号权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400041	RESOURCE	201109	RESOURCE_PERMISSION	PERMISSION		iam:account:grantpermission	CASCADE	ALLOW	ALL	[]	f	124	ENABLED	分配账号权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400042	RESOURCE	200008	RESOURCE_PERMISSION	PERMISSION		iam:dept:page	CASCADE	ALLOW	ALL	[]	f	130	ENABLED	部门分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400043	RESOURCE	200008	RESOURCE_PERMISSION	PERMISSION		iam:dept:list	CASCADE	ALLOW	ALL	[]	f	131	ENABLED	部门列表	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400044	RESOURCE	201121	RESOURCE_PERMISSION	PERMISSION		iam:dept:create	CASCADE	ALLOW	ALL	[]	f	140	ENABLED	新增部门	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400045	RESOURCE	201122	RESOURCE_PERMISSION	PERMISSION		iam:dept:detail	CASCADE	ALLOW	ALL	[]	f	141	ENABLED	查看部门	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400046	RESOURCE	201123	RESOURCE_PERMISSION	PERMISSION		iam:dept:update	CASCADE	ALLOW	ALL	[]	f	142	ENABLED	编辑部门	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400047	RESOURCE	201124	RESOURCE_PERMISSION	PERMISSION		iam:dept:delete	CASCADE	ALLOW	ALL	[]	f	143	ENABLED	删除部门	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400048	RESOURCE	200009	RESOURCE_PERMISSION	PERMISSION		iam:group:page	CASCADE	ALLOW	ALL	[]	f	150	ENABLED	用户组分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400049	RESOURCE	201131	RESOURCE_PERMISSION	PERMISSION		iam:group:create	CASCADE	ALLOW	ALL	[]	f	160	ENABLED	新增用户组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400050	RESOURCE	201132	RESOURCE_PERMISSION	PERMISSION		iam:group:detail	CASCADE	ALLOW	ALL	[]	f	161	ENABLED	查看用户组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400051	RESOURCE	201133	RESOURCE_PERMISSION	PERMISSION		iam:group:update	CASCADE	ALLOW	ALL	[]	f	162	ENABLED	编辑用户组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400052	RESOURCE	201134	RESOURCE_PERMISSION	PERMISSION		iam:group:delete	CASCADE	ALLOW	ALL	[]	f	163	ENABLED	删除用户组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400053	RESOURCE	201135	RESOURCE_PERMISSION	PERMISSION		iam:group:ownuser	CASCADE	ALLOW	ALL	[]	f	164	ENABLED	获取用户组用户	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400054	RESOURCE	201135	RESOURCE_PERMISSION	PERMISSION		iam:group:grantuser	CASCADE	ALLOW	ALL	[]	f	165	ENABLED	分配用户组用户	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400055	RESOURCE	201136	RESOURCE_PERMISSION	PERMISSION		iam:group:ownrole	CASCADE	ALLOW	ALL	[]	f	166	ENABLED	获取用户组角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400056	RESOURCE	201136	RESOURCE_PERMISSION	PERMISSION		iam:group:grantrole	CASCADE	ALLOW	ALL	[]	f	167	ENABLED	分配用户组角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400057	RESOURCE	201137	RESOURCE_PERMISSION	PERMISSION		iam:group:ownresource	CASCADE	ALLOW	ALL	[]	f	168	ENABLED	获取用户组资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400058	RESOURCE	201137	RESOURCE_PERMISSION	PERMISSION		iam:group:grantresource	CASCADE	ALLOW	ALL	[]	f	169	ENABLED	分配用户组资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400059	RESOURCE	201138	RESOURCE_PERMISSION	PERMISSION		iam:group:ownpermission	CASCADE	ALLOW	ALL	[]	f	170	ENABLED	获取用户组权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400060	RESOURCE	201138	RESOURCE_PERMISSION	PERMISSION		iam:group:grantpermission	CASCADE	ALLOW	ALL	[]	f	171	ENABLED	分配用户组权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400061	RESOURCE	200010	RESOURCE_PERMISSION	PERMISSION		iam:position:page	CASCADE	ALLOW	ALL	[]	f	180	ENABLED	岗位分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400062	RESOURCE	201151	RESOURCE_PERMISSION	PERMISSION		iam:position:create	CASCADE	ALLOW	ALL	[]	f	190	ENABLED	新增岗位	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400063	RESOURCE	201152	RESOURCE_PERMISSION	PERMISSION		iam:position:detail	CASCADE	ALLOW	ALL	[]	f	191	ENABLED	查看岗位	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400064	RESOURCE	201153	RESOURCE_PERMISSION	PERMISSION		iam:position:update	CASCADE	ALLOW	ALL	[]	f	192	ENABLED	编辑岗位	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400065	RESOURCE	201154	RESOURCE_PERMISSION	PERMISSION		iam:position:delete	CASCADE	ALLOW	ALL	[]	f	193	ENABLED	删除岗位	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400066	RESOURCE	200011	RESOURCE_PERMISSION	PERMISSION		iam:role:page	CASCADE	ALLOW	ALL	[]	f	200	ENABLED	角色分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400067	RESOURCE	201161	RESOURCE_PERMISSION	PERMISSION		iam:role:create	CASCADE	ALLOW	ALL	[]	f	210	ENABLED	新增角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400068	RESOURCE	201162	RESOURCE_PERMISSION	PERMISSION		iam:role:detail	CASCADE	ALLOW	ALL	[]	f	211	ENABLED	查看角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400069	RESOURCE	201163	RESOURCE_PERMISSION	PERMISSION		iam:role:update	CASCADE	ALLOW	ALL	[]	f	212	ENABLED	编辑角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400070	RESOURCE	201164	RESOURCE_PERMISSION	PERMISSION		iam:role:delete	CASCADE	ALLOW	ALL	[]	f	213	ENABLED	删除角色	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400071	RESOURCE	201165	RESOURCE_PERMISSION	PERMISSION		iam:role:ownresource	CASCADE	ALLOW	ALL	[]	f	214	ENABLED	获取角色资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400072	RESOURCE	201165	RESOURCE_PERMISSION	PERMISSION		iam:role:grantresource	CASCADE	ALLOW	ALL	[]	f	215	ENABLED	分配角色资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400073	RESOURCE	201166	RESOURCE_PERMISSION	PERMISSION		iam:role:permissiontree	CASCADE	ALLOW	ALL	[]	f	216	ENABLED	角色权限树	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400074	RESOURCE	201166	RESOURCE_PERMISSION	PERMISSION		iam:role:ownpermission	CASCADE	ALLOW	ALL	[]	f	217	ENABLED	获取角色权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400075	RESOURCE	201166	RESOURCE_PERMISSION	PERMISSION		iam:role:grantpermission	CASCADE	ALLOW	ALL	[]	f	218	ENABLED	分配角色权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400076	RESOURCE	201166	RESOURCE_PERMISSION	PERMISSION		iam:dept:list	CASCADE	ALLOW	ALL	[]	f	219	ENABLED	部门树选择	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400077	RESOURCE	201167	RESOURCE_PERMISSION	PERMISSION		iam:role:ownuser	CASCADE	ALLOW	ALL	[]	f	220	ENABLED	获取角色用户	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400078	RESOURCE	201167	RESOURCE_PERMISSION	PERMISSION		iam:role:grantuser	CASCADE	ALLOW	ALL	[]	f	221	ENABLED	分配角色用户	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400079	RESOURCE	200012	RESOURCE_PERMISSION	PERMISSION		iam:resource:page	CASCADE	ALLOW	ALL	[]	f	230	ENABLED	资源分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400080	RESOURCE	200012	RESOURCE_PERMISSION	PERMISSION		iam:resource:list	CASCADE	ALLOW	ALL	[]	f	231	ENABLED	资源列表	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400081	RESOURCE	201181	RESOURCE_PERMISSION	PERMISSION		iam:resource:create	CASCADE	ALLOW	ALL	[]	f	240	ENABLED	新增资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400082	RESOURCE	201182	RESOURCE_PERMISSION	PERMISSION		iam:resource:detail	CASCADE	ALLOW	ALL	[]	f	241	ENABLED	查看资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400083	RESOURCE	201183	RESOURCE_PERMISSION	PERMISSION		iam:resource:update	CASCADE	ALLOW	ALL	[]	f	242	ENABLED	编辑资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400084	RESOURCE	201184	RESOURCE_PERMISSION	PERMISSION		iam:resource:delete	CASCADE	ALLOW	ALL	[]	f	243	ENABLED	删除资源	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400085	RESOURCE	201185	RESOURCE_PERMISSION	PERMISSION		iam:resource:grant	CASCADE	ALLOW	ALL	[]	f	244	ENABLED	绑定资源权限	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400086	RESOURCE	200018	RESOURCE_PERMISSION	PERMISSION		iam:resourcemodule:page	CASCADE	ALLOW	ALL	[]	f	250	ENABLED	资源模块分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400087	RESOURCE	201191	RESOURCE_PERMISSION	PERMISSION		iam:resourcemodule:create	CASCADE	ALLOW	ALL	[]	f	260	ENABLED	新增资源模块	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400088	RESOURCE	201192	RESOURCE_PERMISSION	PERMISSION		iam:resourcemodule:detail	CASCADE	ALLOW	ALL	[]	f	261	ENABLED	查看资源模块	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400089	RESOURCE	201193	RESOURCE_PERMISSION	PERMISSION		iam:resourcemodule:update	CASCADE	ALLOW	ALL	[]	f	262	ENABLED	编辑资源模块	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400090	RESOURCE	201194	RESOURCE_PERMISSION	PERMISSION		iam:resourcemodule:delete	CASCADE	ALLOW	ALL	[]	f	263	ENABLED	删除资源模块	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400091	RESOURCE	200020	RESOURCE_PERMISSION	PERMISSION		message:notification:page	CASCADE	ALLOW	ALL	[]	f	270	ENABLED	通知分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400092	RESOURCE	201201	RESOURCE_PERMISSION	PERMISSION		message:notification:create	CASCADE	ALLOW	ALL	[]	f	280	ENABLED	新增通知	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400093	RESOURCE	201202	RESOURCE_PERMISSION	PERMISSION		message:notification:detail	CASCADE	ALLOW	ALL	[]	f	281	ENABLED	查看通知	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400094	RESOURCE	201203	RESOURCE_PERMISSION	PERMISSION		message:notification:update	CASCADE	ALLOW	ALL	[]	f	282	ENABLED	编辑通知	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400095	RESOURCE	201204	RESOURCE_PERMISSION	PERMISSION		message:notification:delete	CASCADE	ALLOW	ALL	[]	f	283	ENABLED	删除通知	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400096	RESOURCE	201205	RESOURCE_PERMISSION	PERMISSION		message:notification:publish	CASCADE	ALLOW	ALL	[]	f	284	ENABLED	发布通知	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400097	RESOURCE	201206	RESOURCE_PERMISSION	PERMISSION		message:notification:revoke	CASCADE	ALLOW	ALL	[]	f	285	ENABLED	撤回通知	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400098	RESOURCE	200021	RESOURCE_PERMISSION	PERMISSION		message:thread:page	CASCADE	ALLOW	ALL	[]	f	290	ENABLED	站内信分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400099	RESOURCE	200021	RESOURCE_PERMISSION	PERMISSION		message:group:page	CASCADE	ALLOW	ALL	[]	f	291	ENABLED	消息组分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400100	RESOURCE	201221	RESOURCE_PERMISSION	PERMISSION		message:thread:detail	CASCADE	ALLOW	ALL	[]	f	300	ENABLED	查看站内信	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400101	RESOURCE	201222	RESOURCE_PERMISSION	PERMISSION		message:thread:send	CASCADE	ALLOW	ALL	[]	f	301	ENABLED	发送站内信	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400102	RESOURCE	201223	RESOURCE_PERMISSION	PERMISSION		message:group:create	CASCADE	ALLOW	ALL	[]	f	302	ENABLED	新增消息组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400103	RESOURCE	201224	RESOURCE_PERMISSION	PERMISSION		message:group:detail	CASCADE	ALLOW	ALL	[]	f	303	ENABLED	查看消息组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400104	RESOURCE	201225	RESOURCE_PERMISSION	PERMISSION		message:group:update	CASCADE	ALLOW	ALL	[]	f	304	ENABLED	编辑消息组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400105	RESOURCE	201226	RESOURCE_PERMISSION	PERMISSION		message:group:delete	CASCADE	ALLOW	ALL	[]	f	305	ENABLED	删除消息组	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400106	RESOURCE	200022	RESOURCE_PERMISSION	PERMISSION		message:todo:page	CASCADE	ALLOW	ALL	[]	f	310	ENABLED	待办分页	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400107	RESOURCE	201241	RESOURCE_PERMISSION	PERMISSION		message:todo:create	CASCADE	ALLOW	ALL	[]	f	320	ENABLED	新增待办	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400108	RESOURCE	201242	RESOURCE_PERMISSION	PERMISSION		message:todo:detail	CASCADE	ALLOW	ALL	[]	f	321	ENABLED	查看待办	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400109	RESOURCE	201243	RESOURCE_PERMISSION	PERMISSION		message:todo:update	CASCADE	ALLOW	ALL	[]	f	322	ENABLED	编辑待办	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400110	RESOURCE	201244	RESOURCE_PERMISSION	PERMISSION		message:todo:delete	CASCADE	ALLOW	ALL	[]	f	323	ENABLED	删除待办	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
400111	RESOURCE	201245	RESOURCE_PERMISSION	PERMISSION		message:todo:cancel	CASCADE	ALLOW	ALL	[]	f	324	ENABLED	取消待办	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300001	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200001		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300003	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200003		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300004	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200004		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300005	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200005		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300023	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200023		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300027	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200027		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300024	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200024		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300025	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200025		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300006	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200006		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300007	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200007		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300008	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200008		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300009	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200009		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300010	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200010		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300011	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200011		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300012	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200012		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300018	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200018		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300019	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200019		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300020	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200020		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300021	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200021		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
300022	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200022		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301011	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201011		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301012	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201012		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301013	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201013		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301014	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201014		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301021	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201021		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301022	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201022		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301023	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201023		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301024	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201024		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301031	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201031		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301032	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201032		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301033	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201033		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301034	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201034		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301035	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201035		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301041	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201041		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301042	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201042		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301043	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201043		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301101	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201101		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301102	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201102		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301103	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201103		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301104	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201104		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301105	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201105		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301106	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201106		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301107	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201107		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301108	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201108		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301109	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201109		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301121	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201121		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301122	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201122		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301123	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201123		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301124	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201124		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301131	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201131		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301132	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201132		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301133	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201133		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301134	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201134		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301135	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201135		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301136	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201136		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301137	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201137		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301138	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201138		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301151	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201151		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301152	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201152		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301153	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201153		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301154	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201154		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301161	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201161		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301162	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201162		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301163	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201163		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301164	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201164		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301165	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201165		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301166	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201166		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301167	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201167		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301181	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201181		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301182	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201182		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301183	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201183		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301184	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201184		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301185	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201185		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301191	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201191		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301192	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201192		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301193	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201193		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301194	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201194		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301201	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201201		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301202	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201202		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301203	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201203		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301204	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201204		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301205	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201205		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301206	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201206		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301221	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201221		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301222	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201222		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301223	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201223		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301224	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201224		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301225	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201225		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301226	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201226		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301241	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201241		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301242	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201242		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301243	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201243		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301244	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201244		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
301245	ROLE	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201245		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	超管角色默认资源授权	\N	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
7479007640320872448	ACCOUNT	1	ACCOUNT_ROLE	ROLE	1		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:14.812221+00	1	2026-07-04 03:06:14.812221+00	1
7479007787574497280	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200004		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497281	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200005		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497282	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200023		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497283	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200027		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497284	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200001		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497285	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200003		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497286	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200024		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497287	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200006		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497288	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200019		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497289	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200020		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497290	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200021		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497291	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200022		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497292	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200007		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497293	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200008		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497294	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200009		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497295	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200010		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497296	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200011		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497297	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200012		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497298	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200018		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497299	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	200025		DIRECT	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497300	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201011		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497301	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201012		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497302	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201013		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497303	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201014		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497304	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201021		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497305	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201022		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497306	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201023		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497307	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201024		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497308	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201031		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497309	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201032		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497310	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201033		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497311	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201034		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497312	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201035		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497313	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201201		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497314	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201202		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497315	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201203		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497316	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201204		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497317	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201205		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497318	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201206		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497319	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201221		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497320	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201222		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497321	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201223		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497322	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201224		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497323	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201225		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497324	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201226		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497325	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201241		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497326	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201242		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497327	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201243		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497328	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201244		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497329	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201245		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497330	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201101		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497331	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201102		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497332	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201103		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497333	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201104		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497334	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201105		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497335	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201106		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497336	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201107		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497337	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201166		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497338	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201108		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497339	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201109		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497340	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201121		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497341	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201122		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497342	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201123		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497343	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201124		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497344	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201131		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497345	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201132		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497346	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201133		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497347	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201134		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497348	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201135		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497349	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201136		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497350	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201137		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497351	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201138		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497352	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201151		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497353	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201152		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497354	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201153		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497355	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201154		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497356	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201161		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497357	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201162		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497358	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201163		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497359	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201164		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497360	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201165		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497361	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201167		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497362	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201181		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497363	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201182		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497364	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201183		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497365	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201184		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497366	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201185		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497367	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201191		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497368	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201192		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497369	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201193		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497370	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201194		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497371	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201041		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497372	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201042		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
7479007787574497373	ACCOUNT	1	SUBJECT_RESOURCE_GRANT	RESOURCE	201043		CASCADE	ALLOW	SELF	[]	f	99	ENABLED	\N	\N	\N	{}	2026-07-04 03:06:49.200257+00	1	2026-07-04 03:06:49.200257+00	1
402010	RESOURCE	202010	RESOURCE_PERMISSION	PERMISSION		sys:config:page	CASCADE	ALLOW	ALL	[]	f	90	ENABLED	系统配置分页	\N	\N	{}	2026-07-18 14:07:48.98965+00	\N	2026-07-18 14:07:48.98965+00	\N
402011	RESOURCE	202011	RESOURCE_PERMISSION	PERMISSION		sys:config:create	CASCADE	ALLOW	ALL	[]	f	91	ENABLED	新增系统配置	\N	\N	{}	2026-07-18 14:07:48.98965+00	\N	2026-07-18 14:07:48.98965+00	\N
402012	RESOURCE	202012	RESOURCE_PERMISSION	PERMISSION		sys:config:detail	CASCADE	ALLOW	ALL	[]	f	92	ENABLED	查看系统配置	\N	\N	{}	2026-07-18 14:07:48.98965+00	\N	2026-07-18 14:07:48.98965+00	\N
402013	RESOURCE	202013	RESOURCE_PERMISSION	PERMISSION		sys:config:update	CASCADE	ALLOW	ALL	[]	f	93	ENABLED	编辑系统配置	\N	\N	{}	2026-07-18 14:07:48.98965+00	\N	2026-07-18 14:07:48.98965+00	\N
402014	RESOURCE	202014	RESOURCE_PERMISSION	PERMISSION		sys:config:delete	CASCADE	ALLOW	ALL	[]	f	94	ENABLED	删除系统配置	\N	\N	{}	2026-07-18 14:07:48.98965+00	\N	2026-07-18 14:07:48.98965+00	\N
\.


--
-- Data for Name: sys_operation_audit_log; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_operation_audit_log (id, module, resource_type, resource_id, action, summary, before_data, after_data, account_id, account_type, request_id, ip, user_agent, success, error_message, created_at) FROM stdin;
7479006169864998912	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	c470f01a7fec465a8bbe6bd57f955b90	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-04 03:00:22.628717+00
7479007658016641024	iam	accounts	\N	grant_role	POST /api/v1/admin/sys/accounts/grant-role	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-04 03:06:19.708565+00
7479007816171261952	iam	accounts	\N	grant_resource	POST /api/v1/admin/sys/accounts/grant-resource	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-04 03:06:57.437187+00
7479008464077983744	auth	account	444	forgot_password	PORTAL password reset requested	null	null	\N	PORTAL	13caa24ed8674bf286845b61fee03579	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	f	\N	2026-07-04 03:09:31.528892+00
7481108564149407744	auth	account	IZBdGdEyYr6WKk9xqbB7SGTn7BKtz646lmvZu1NtdIQ	logout	Logout	null	null	1	ADMIN	55ef1be904f74b129879d19ca59434e1	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-09 22:14:33.083616+00
7481108743145525248	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	1695d037eeda4f009efcd0eab7d3b42d	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-09 22:15:08.055321+00
7481512786338648064	auth	account	ohD8lOXav2O4esk8iJUKesBAy0vN5xSEtOvrHAhI6DY	logout	Logout	null	null	1	ADMIN	7e817d3c74e74baf8fd74323719f930f	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-11 01:00:48.31731+00
7481609424990244864	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	f3052fed47564c34878fe35c803c3534	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-11 07:24:46.927294+00
7481609909717569536	iam	resource-modules	\N	create	POST /api/v1/admin/sys/resource-modules/create	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-11 07:26:44.92678+00
7481611848157106176	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-11 07:34:27.072448+00
7481613605440458752	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	484bcc10c8da42c2b22c6e91efd74ae8	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-11 07:41:21.657255+00
7481614738531684352	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-11 07:45:56.198337+00
7481614800842264576	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-11 07:46:11.056724+00
7481617845055524864	resource	resources	\N	create	POST /api/v1/admin/sys/resources/create	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-11 07:58:16.869667+00
7481618068100222976	resource	resources	\N	update	POST /api/v1/admin/sys/resources/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-11 07:59:10.052768+00
7481631922402234368	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	8ec437b8389a4729881dada1aa95d1e4	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-11 08:54:11.805441+00
7481635477972455424	auth	account	Lni4zC3SmMeyBQPyx0wayp6jFRmwgco0cfouq5cdHL8	logout	Logout	null	null	1	ADMIN	e6867ce83db94e188c6a8f5fc37bb293	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 09:08:21.068574+00
7481635593382924288	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	e7c1ed52065540d79423d16799d4b742	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 09:08:47.014739+00
7481643732555665408	auth	account	6iXAD_48zo3ng719rfYnJ5xufzne4LkAo-EhV4ou9D0	logout	Logout	null	null	1	ADMIN	2c79d88012f947b4ba3bc1b2b8438199	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 09:41:09.121935+00
7481645551415595008	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	13c111cc58834b7f935db5b550af4851	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 09:48:21.232977+00
7481645847776727040	auth	account	LZBcwWAZPKWtm_iTZcTZUxhvFwUFowQRyxaDsQZ0kZM	logout	Logout	null	null	1	ADMIN	cb530c77e51e46039f3659265a3f44a5	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 09:49:33.203359+00
7481646023664865280	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	721493d3fc534533a22fa559abc15532	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 09:50:13.95673+00
7481649567335845888	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	32393537e2c346c891131d0d35ec368c	127.0.0.1	Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1 wechatdevtools/2.01.2510290 MicroMessenger/8.0.5 Language/zh_CN webview/ hash/1412878345 sid/Q6cplUqIbB token/ff21074b4597361e58018fea0a5b6d9e	t	\N	2026-07-11 10:04:17.766144+00
7481654086476107776	auth	account	e9GAWaByZucIQ0oHZKmBcbrwyWjldAB7kRoxhmm25l8	logout	Logout	null	null	1	ADMIN	ffb97af7e973473ba67bdf10e2c95b83	127.0.0.1	Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Mobile Safari/537.36	t	\N	2026-07-11 10:22:17.279912+00
7481667955785338880	auth	account	superadmin	login	PORTAL login failed	null	null	\N	PORTAL	6f03040707d84d64940a497d1703c38d	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	f	Invalid or locked login attempt	2026-07-11 11:17:22.315141+00
7481668354080641024	auth	account	7481668347524943872	register	Portal account registered	null	null	7481668347524943872	PORTAL	b26fdec88aa94119b7b875bb848b47bb	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-11 11:18:58.326465+00
7481668414730276864	auth	account	7481668347524943872	login	PORTAL login succeeded	null	null	7481668347524943872	PORTAL	0eb5fa5396bb4076a7bfc69e3ebd1e3d	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-11 11:19:11.114887+00
7481891234546585600	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	9dbf516de04145b5849ad66defcc36de	127.0.0.1	Mozilla/5.0 (iPhone; CPU iPhone OS 18_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.5 Mobile/15E148 Safari/604.1	t	\N	2026-07-12 02:04:34.325455+00
7482027977086210048	iam	groups	\N	create	POST /api/v1/admin/sys/groups/create	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-12 11:07:59.641892+00
7482038137334665216	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	1e83b99c0c03444dbd075bc967495f92	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-12 11:48:19.166471+00
7482038422337622016	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	c14cc5930dfd4b76bdd9976bf55b94ec	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-12 11:49:27.428271+00
7482039429121576960	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	be08a0d22ce14bbfb7f6d5d62b1f5303	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-12 11:53:26.268137+00
7482041025985384448	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-12 11:59:50.76719+00
7482041074257629184	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-12 12:00:02.287019+00
7482041113159798784	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-12 12:00:11.548542+00
7482043364729294848	auth	account	uE18478S8t-C3az8fUU_hBWmm01RTDBh6-R6q0djOng	logout	Logout	null	null	1	ADMIN	66ede3ecf5a942858d4b044884231598	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-12 12:09:07.80373+00
7482043527124357120	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	e40203439f4249b783ae3f1ad2c4d9c0	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-12 12:09:44.203693+00
7482054030085197824	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	2ca9ec8e6b6a4fbab5341d1e34bb9a3d	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	t	\N	2026-07-12 12:51:28.5524+00
7484237926407213056	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	75d02dee689440ec8f5a3dfc06d12f85	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	t	\N	2026-07-18 13:29:28.437805+00
7484241057224462336	iam	resource-modules	\N	update	POST /api/v1/admin/sys/resource-modules/update	null	null	\N	\N	\N	\N	\N	t	\N	2026-07-18 13:41:57.26352+00
7484247805125595136	auth	account	1	login	ADMIN login succeeded	null	null	1	ADMIN	53a70650aaa1493cbe6fa061190365c1	127.0.0.1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36	t	\N	2026-07-18 14:08:43.721353+00
\.


--
-- Data for Name: sys_position; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_position (id, name, code, category, owner_dept_id, sort, is_virtual, status, description, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
\.


--
-- Data for Name: sys_resource; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_resource (id, parent_id, code, name, resource_type, module_id, path, component, redirect, icon, color, href, sort, is_visible, is_cache, is_affix, status, description, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
200004	200003	sys-dict	字典管理	MENU	210001	/sys/dict	/sys/dict/index.vue	\N	icon-park-outline:file-search	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200005	200003	sys-banner	展示图管理	MENU	210001	/sys/banner	/sys/banner/index.vue	\N	icon-park-outline:ad-product	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200012	200006	iam-resource	资源管理	MENU	210001	/iam/resource	/iam/resource/index.vue	\N	icon-park-outline:all-application	\N	\N	6	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200018	200006	iam-resourcemodule	资源模块管理	MENU	210001	/iam/resource_module	/iam/resource_module/index.vue	\N	icon-park-outline:blocks-and-arrows	\N	\N	7	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200019	\N	message	消息中心	CATALOG	210001	/message	\N	\N	icon-park-outline:message	\N	\N	18	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200020	200019	message-notification	通知管理	MENU	210001	/message/notification	/message/notification/index.vue	\N	icon-park-outline:tips-one	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200021	200019	message-message	站内信管理	MENU	210001	/message/message	/message/message/index.vue	\N	icon-park-outline:message	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200022	200019	message-todo	待办管理	MENU	210001	/message/todo	/message/todo/index.vue	\N	icon-park-outline:checklist	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200026	\N	portal-demo	示例页面	MENU	210002	/demo	/demo/index.vue	\N	icon-park-outline:experiment-one	\N	\N	1	t	f	f	ENABLED	门户端公开示例菜单	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201011	200004	sys-dict-create	新增字典	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201012	200004	sys-dict-detail	查看字典	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201122	200008	iam-dept-detail	查看部门	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201013	200004	sys-dict-update	编辑字典	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201014	200004	sys-dict-delete	删除字典	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201021	200005	sys-banner-create	新增展示图	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201022	200005	sys-banner-detail	查看展示图	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201023	200005	sys-banner-update	编辑展示图	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201024	200005	sys-banner-delete	删除展示图	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201031	200023	sys-file-upload	上传文件	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201032	200023	sys-file-detail	查看文件	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201033	200023	sys-file-update	编辑文件	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201034	200023	sys-file-url	打开文件	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201035	200023	sys-file-delete	删除文件	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201041	200025	auth-session-tokenlist	查看令牌	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201042	200025	auth-session-exit	强退账号	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201043	200025	auth-session-tokenexit	强退令牌	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201101	200007	iam-account-create	新增账号	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201123	200008	iam-dept-update	编辑部门	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201124	200008	iam-dept-delete	删除部门	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201131	200009	iam-group-create	新增用户组	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201132	200009	iam-group-detail	查看用户组	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201133	200009	iam-group-update	编辑用户组	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
200001	\N	dashboard	运营工作台	MENU	210001	/dashboard	/dashboard/index.vue	\N	icon-park-outline:analysis	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200003	\N	sys	系统	CATALOG	210001	/sys	\N	\N	icon-park-outline:setting-two	\N	\N	10	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200023	200003	sys-file	文件管理	MENU	210001	/sys/file	/sys/file/index.vue	\N	icon-park-outline:file-code	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200027	200003	sys-audit-api	操作审计接口	API_GROUP	210001	\N	\N	\N	\N	\N	\N	9	t	f	f	ENABLED	操作审计后端权限组	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
200024	\N	security	认证	CATALOG	210001	/security	\N	\N	icon-park-outline:lock	\N	\N	12	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200025	200024	security-session	在线会话	MENU	210001	/security/session	/auth/session/index.vue	\N	icon-park-outline:connection	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200006	\N	iam	身份与权限	CATALOG	210001	/iam	\N	\N	icon-park-outline:permissions	\N	\N	15	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200007	200006	iam-account	账号管理	MENU	210001	/iam/account	/iam/account/index.vue	\N	icon-park-outline:people	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200008	200006	iam-dept	部门管理	MENU	210001	/iam/dept	/iam/dept/index.vue	\N	icon-park-outline:tree-diagram	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200009	200006	iam-group	用户组管理	MENU	210001	/iam/group	/iam/group/index.vue	\N	icon-park-outline:group	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200010	200006	iam-position	岗位管理	MENU	210001	/iam/position	/iam/position/index.vue	\N	icon-park-outline:people-bottom	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
200011	200006	iam-role	角色管理	MENU	210001	/iam/role	/iam/role/index.vue	\N	icon-park-outline:peoples	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-06-30 00:00:00+00	\N	2026-06-30 00:00:00+00	\N
201102	200007	iam-account-detail	查看账号	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201103	200007	iam-account-update	编辑账号	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201182	200012	iam-resource-detail	查看资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201183	200012	iam-resource-update	编辑资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201184	200012	iam-resource-delete	删除资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201185	200012	iam-resource-grant	绑定权限	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201191	200018	iam-resourcemodule-create	新增资源模块	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201192	200018	iam-resourcemodule-detail	查看资源模块	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201193	200018	iam-resourcemodule-update	编辑资源模块	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201194	200018	iam-resourcemodule-delete	删除资源模块	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201201	200020	message-notification-create	新增通知	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201202	200020	message-notification-detail	查看通知	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201203	200020	message-notification-update	编辑通知	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201204	200020	message-notification-delete	删除通知	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201104	200007	iam-account-delete	删除账号	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201105	200007	iam-account-grant-role	分配角色	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201106	200007	iam-account-grant-group	分配用户组	BUTTON	210001	\N	\N	\N	\N	\N	\N	6	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201107	200007	iam-account-grant-dept	分配部门	BUTTON	210001	\N	\N	\N	\N	\N	\N	7	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201108	200007	iam-account-grant-resource	分配资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	8	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201121	200008	iam-dept-create	新增部门	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201134	200009	iam-group-delete	删除用户组	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201135	200009	iam-group-grant-user	分配用户	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201136	200009	iam-group-grant-role	分配角色	BUTTON	210001	\N	\N	\N	\N	\N	\N	6	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201137	200009	iam-group-grant-resource	分配资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	7	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201151	200010	iam-position-create	新增岗位	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201152	200010	iam-position-detail	查看岗位	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201153	200010	iam-position-update	编辑岗位	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201154	200010	iam-position-delete	删除岗位	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201161	200011	iam-role-create	新增角色	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201162	200011	iam-role-detail	查看角色	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201163	200011	iam-role-update	编辑角色	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201164	200011	iam-role-delete	删除角色	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201165	200011	iam-role-grant-resource	分配资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201167	200011	iam-role-grant-user	分配用户	BUTTON	210001	\N	\N	\N	\N	\N	\N	7	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201181	200012	iam-resource-create	新增资源	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201205	200020	message-notification-publish	发布通知	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201206	200020	message-notification-revoke	撤回通知	BUTTON	210001	\N	\N	\N	\N	\N	\N	6	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201221	200021	message-thread-detail	查看会话	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201222	200021	message-thread-send	发送站内信	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201223	200021	message-group-create	新增消息组	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201224	200021	message-group-detail	查看消息组	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201225	200021	message-group-update	编辑消息组	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201226	200021	message-group-delete	删除消息组	BUTTON	210001	\N	\N	\N	\N	\N	\N	6	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201241	200022	message-todo-create	新增待办	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201242	200022	message-todo-detail	查看待办	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201243	200022	message-todo-update	编辑待办	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201244	200022	message-todo-delete	删除待办	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
201245	200022	message-todo-cancel	取消待办	BUTTON	210001	\N	\N	\N	\N	\N	\N	5	t	f	f	ENABLED	\N	{}	2026-07-03 00:00:00+00	\N	2026-07-03 00:00:00+00	\N
7481617843012898816	\N	test	test	MENU	7481609907767218176	test	\N	\N	\N	\N	\N	0	t	f	f	ENABLED	testtest	{}	2026-07-11 07:58:15.841688+00	1	2026-07-11 07:59:08.611726+00	1
202001	\N	system-test	测试目录	CATALOG	210001	/test	\N	/test/editor	icon-park-outline:experiment-one	\N	\N	30	t	f	f	ENABLED	系统模块测试页面目录	{}	2026-07-18 12:39:16.472705+00	\N	2026-07-18 12:39:16.472705+00	\N
202002	202001	system-test-editor	编辑器测试	MENU	210001	/test/editor	/test/editor/index.vue	\N	icon-park-outline:edit	\N	\N	1	t	f	f	ENABLED	Markdown、富文本和代码编辑器组件测试页面	{}	2026-07-18 12:39:16.472705+00	\N	2026-07-18 12:39:16.472705+00	\N
202003	202001	system-test-icon	图标选择器测试	MENU	210001	/test/icon	/test/icon/index.vue	\N	icon-park-outline:all-application	\N	\N	2	t	f	f	ENABLED	Iconify 离线图标选择器测试页面	{}	2026-07-18 12:49:42.562935+00	\N	2026-07-18 12:49:42.562935+00	\N
202010	200003	system-config	系统配置	MENU	210001	/sys/config	/sys/config/index.vue	\N	icon-park-outline:setting-config	\N	\N	5	t	f	f	ENABLED	系统配置管理页面	{}	2026-07-18 14:07:48.814138+00	\N	2026-07-18 14:07:48.814138+00	\N
202011	202010	system-config-create	新增系统配置	BUTTON	210001	\N	\N	\N	\N	\N	\N	1	t	f	f	ENABLED	\N	{}	2026-07-18 14:07:48.899867+00	\N	2026-07-18 14:07:48.899867+00	\N
202012	202010	system-config-detail	查看系统配置	BUTTON	210001	\N	\N	\N	\N	\N	\N	2	t	f	f	ENABLED	\N	{}	2026-07-18 14:07:48.899867+00	\N	2026-07-18 14:07:48.899867+00	\N
202013	202010	system-config-update	编辑系统配置	BUTTON	210001	\N	\N	\N	\N	\N	\N	3	t	f	f	ENABLED	\N	{}	2026-07-18 14:07:48.899867+00	\N	2026-07-18 14:07:48.899867+00	\N
202014	202010	system-config-delete	删除系统配置	BUTTON	210001	\N	\N	\N	\N	\N	\N	4	t	f	f	ENABLED	\N	{}	2026-07-18 14:07:48.899867+00	\N	2026-07-18 14:07:48.899867+00	\N
\.


--
-- Data for Name: sys_resource_module; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_resource_module (id, name, code, client, icon, color, sort, status, description, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
210002	门户	HEADER	PORTAL	icon-park-outline:browser	#18a058	2	ENABLED	门户端公开资源模块	{}	2026-07-03 00:00:00+00	\N	2026-07-12 12:00:01.484173+00	1
7481609907767218176	测试	T	ADMIN	icon-park-outline:setting-two	#2080f0	2	ENABLED	ASDASD	{}	2026-07-11 07:26:44.129778+00	1	2026-07-12 12:00:10.673997+00	1
210001	系统	system	ADMIN	icon-park-outline:adjacent-item	#2563eb	1	ENABLED	系统内置资源模块	{}	2026-06-30 00:00:00+00	\N	2026-07-18 13:41:56.152581+00	1
\.


--
-- Data for Name: sys_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sys_role (id, code, name, category, scope_type, owner_dept_id, sort, status, is_builtin, description, extra, created_at, created_by, updated_at, updated_by) FROM stdin;
1	SUPER_ADMIN	超级管理员	SYS	PLATFORM	\N	1	ENABLED	t	系统内置超级管理员角色	{}	2026-07-04 03:00:01.332229+00	\N	2026-07-04 03:00:01.332229+00	\N
\.


--
-- Name: alembic_version alembic_version_pkc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alembic_version
    ADD CONSTRAINT alembic_version_pkc PRIMARY KEY (version_num);


--
-- Name: admin_user_profile pk_admin_user_profile; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin_user_profile
    ADD CONSTRAINT pk_admin_user_profile PRIMARY KEY (account_id);


--
-- Name: msg_group pk_msg_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_group
    ADD CONSTRAINT pk_msg_group PRIMARY KEY (id);


--
-- Name: msg_group_member pk_msg_group_member; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_group_member
    ADD CONSTRAINT pk_msg_group_member PRIMARY KEY (id);


--
-- Name: msg_message pk_msg_message; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message
    ADD CONSTRAINT pk_msg_message PRIMARY KEY (id);


--
-- Name: msg_message_attachment pk_msg_message_attachment; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_attachment
    ADD CONSTRAINT pk_msg_message_attachment PRIMARY KEY (id);


--
-- Name: msg_message_reaction pk_msg_message_reaction; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_reaction
    ADD CONSTRAINT pk_msg_message_reaction PRIMARY KEY (id);


--
-- Name: msg_message_receipt pk_msg_message_receipt; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_receipt
    ADD CONSTRAINT pk_msg_message_receipt PRIMARY KEY (id);


--
-- Name: msg_notification pk_msg_notification; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_notification
    ADD CONSTRAINT pk_msg_notification PRIMARY KEY (id);


--
-- Name: msg_notification_read pk_msg_notification_read; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_notification_read
    ADD CONSTRAINT pk_msg_notification_read PRIMARY KEY (id);


--
-- Name: msg_thread pk_msg_thread; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_thread
    ADD CONSTRAINT pk_msg_thread PRIMARY KEY (id);


--
-- Name: msg_thread_participant pk_msg_thread_participant; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_thread_participant
    ADD CONSTRAINT pk_msg_thread_participant PRIMARY KEY (id);


--
-- Name: msg_todo pk_msg_todo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_todo
    ADD CONSTRAINT pk_msg_todo PRIMARY KEY (id);


--
-- Name: msg_todo_assignee pk_msg_todo_assignee; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_todo_assignee
    ADD CONSTRAINT pk_msg_todo_assignee PRIMARY KEY (id);


--
-- Name: portal_user_profile pk_portal_user_profile; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.portal_user_profile
    ADD CONSTRAINT pk_portal_user_profile PRIMARY KEY (account_id);


--
-- Name: sys_account pk_sys_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_account
    ADD CONSTRAINT pk_sys_account PRIMARY KEY (id);


--
-- Name: sys_account_identity pk_sys_account_identity; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_account_identity
    ADD CONSTRAINT pk_sys_account_identity PRIMARY KEY (id);


--
-- Name: sys_banner pk_sys_banner; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_banner
    ADD CONSTRAINT pk_sys_banner PRIMARY KEY (id);


--
-- Name: sys_config pk_sys_config; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_config
    ADD CONSTRAINT pk_sys_config PRIMARY KEY (id);


--
-- Name: sys_dept pk_sys_dept; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT pk_sys_dept PRIMARY KEY (id);


--
-- Name: sys_dict pk_sys_dict; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict
    ADD CONSTRAINT pk_sys_dict PRIMARY KEY (id);


--
-- Name: sys_file pk_sys_file; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_file
    ADD CONSTRAINT pk_sys_file PRIMARY KEY (id);


--
-- Name: sys_group pk_sys_group; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_group
    ADD CONSTRAINT pk_sys_group PRIMARY KEY (id);


--
-- Name: sys_iam_relation pk_sys_iam_relation; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_iam_relation
    ADD CONSTRAINT pk_sys_iam_relation PRIMARY KEY (id);


--
-- Name: sys_operation_audit_log pk_sys_operation_audit_log; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_operation_audit_log
    ADD CONSTRAINT pk_sys_operation_audit_log PRIMARY KEY (id);


--
-- Name: sys_position pk_sys_position; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_position
    ADD CONSTRAINT pk_sys_position PRIMARY KEY (id);


--
-- Name: sys_resource pk_sys_resource; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource
    ADD CONSTRAINT pk_sys_resource PRIMARY KEY (id);


--
-- Name: sys_resource_module pk_sys_resource_module; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource_module
    ADD CONSTRAINT pk_sys_resource_module PRIMARY KEY (id);


--
-- Name: sys_role pk_sys_role; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT pk_sys_role PRIMARY KEY (id);


--
-- Name: msg_group_member uq_msg_group_member_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_group_member
    ADD CONSTRAINT uq_msg_group_member_account UNIQUE (group_id, account_type, account_id);


--
-- Name: msg_message_reaction uq_msg_message_reaction_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_reaction
    ADD CONSTRAINT uq_msg_message_reaction_account UNIQUE (message_id, account_type, account_id, reaction);


--
-- Name: msg_message_receipt uq_msg_message_receipt_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_message_receipt
    ADD CONSTRAINT uq_msg_message_receipt_account UNIQUE (message_id, account_type, account_id);


--
-- Name: msg_notification_read uq_msg_notification_read_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_notification_read
    ADD CONSTRAINT uq_msg_notification_read_account UNIQUE (notification_id, account_type, account_id);


--
-- Name: msg_thread_participant uq_msg_thread_participant_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_thread_participant
    ADD CONSTRAINT uq_msg_thread_participant_account UNIQUE (thread_id, account_type, account_id);


--
-- Name: msg_todo_assignee uq_msg_todo_assignee_account; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.msg_todo_assignee
    ADD CONSTRAINT uq_msg_todo_assignee_account UNIQUE (todo_id, account_type, account_id);


--
-- Name: sys_account_identity uq_sys_account_identity_type_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_account_identity
    ADD CONSTRAINT uq_sys_account_identity_type_identifier UNIQUE (identity_type, identifier);


--
-- Name: sys_dept uq_sys_dept_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT uq_sys_dept_code UNIQUE (code);


--
-- Name: sys_file uq_sys_file_object_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_file
    ADD CONSTRAINT uq_sys_file_object_name UNIQUE (object_name);


--
-- Name: sys_group uq_sys_group_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_group
    ADD CONSTRAINT uq_sys_group_name UNIQUE (name);


--
-- Name: sys_iam_relation uq_sys_iam_relation_subject_relation_target; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_iam_relation
    ADD CONSTRAINT uq_sys_iam_relation_subject_relation_target UNIQUE (subject_type, subject_id, relation_type, target_type, target_id, target_key);


--
-- Name: sys_position uq_sys_position_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_position
    ADD CONSTRAINT uq_sys_position_code UNIQUE (code);


--
-- Name: sys_resource uq_sys_resource_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource
    ADD CONSTRAINT uq_sys_resource_code UNIQUE (code);


--
-- Name: sys_resource_module uq_sys_resource_module_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_resource_module
    ADD CONSTRAINT uq_sys_resource_module_code UNIQUE (code);


--
-- Name: sys_role uq_sys_role_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT uq_sys_role_code UNIQUE (code);


--
-- Name: idx_sys_config_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_config_category ON public.sys_config USING btree (category);


--
-- Name: idx_sys_config_key; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_sys_config_key ON public.sys_config USING btree (config_key);


--
-- Name: idx_sys_dict_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_dict_category ON public.sys_dict USING btree (category);


--
-- Name: idx_sys_dict_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_sys_dict_code ON public.sys_dict USING btree (code);


--
-- Name: idx_sys_dict_parent_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_dict_parent_id ON public.sys_dict USING btree (parent_id);


--
-- Name: idx_sys_operation_audit_account_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_account_id ON public.sys_operation_audit_log USING btree (account_id);


--
-- Name: idx_sys_operation_audit_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_created_at ON public.sys_operation_audit_log USING btree (created_at);


--
-- Name: idx_sys_operation_audit_module_action; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_module_action ON public.sys_operation_audit_log USING btree (module, action);


--
-- Name: idx_sys_operation_audit_resource; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sys_operation_audit_resource ON public.sys_operation_audit_log USING btree (resource_type, resource_id);


--
-- Name: ix_msg_group_member_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_group_member_account ON public.msg_group_member USING btree (account_type, account_id);


--
-- Name: ix_msg_message_attachment_message; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_attachment_message ON public.msg_message_attachment USING btree (message_id, sort);


--
-- Name: ix_msg_message_parent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_parent ON public.msg_message USING btree (parent_id);


--
-- Name: ix_msg_message_reaction_message; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_reaction_message ON public.msg_message_reaction USING btree (message_id);


--
-- Name: ix_msg_message_receipt_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_receipt_account ON public.msg_message_receipt USING btree (account_type, account_id);


--
-- Name: ix_msg_message_thread_created; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_message_thread_created ON public.msg_message USING btree (thread_id, created_at);


--
-- Name: ix_msg_notification_read_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_notification_read_account ON public.msg_notification_read USING btree (account_type, account_id);


--
-- Name: ix_msg_notification_status_scope_publish; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_notification_status_scope_publish ON public.msg_notification USING btree (status, target_scope, publish_at);


--
-- Name: ix_msg_notification_target_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_notification_target_account ON public.msg_notification USING btree (target_account_type, target_account_id);


--
-- Name: ix_msg_thread_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_thread_group ON public.msg_thread USING btree (group_id);


--
-- Name: ix_msg_thread_participant_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_thread_participant_account ON public.msg_thread_participant USING btree (account_type, account_id);


--
-- Name: ix_msg_thread_type_status_last; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_thread_type_status_last ON public.msg_thread USING btree (thread_type, status, last_message_at);


--
-- Name: ix_msg_todo_assignee_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_todo_assignee_account ON public.msg_todo_assignee USING btree (account_type, account_id, status);


--
-- Name: ix_msg_todo_status_scope_due; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_todo_status_scope_due ON public.msg_todo USING btree (status, target_scope, due_at);


--
-- Name: ix_msg_todo_target_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_msg_todo_target_account ON public.msg_todo USING btree (target_account_type, target_account_id);


--
-- Name: ix_sys_banner_scope_position_status_sort; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_banner_scope_position_status_sort ON public.sys_banner USING btree (display_scope, "position", status, sort);


--
-- Name: ix_sys_iam_relation_subject; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_iam_relation_subject ON public.sys_iam_relation USING btree (subject_type, subject_id, relation_type);


--
-- Name: ix_sys_iam_relation_target; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX ix_sys_iam_relation_target ON public.sys_iam_relation USING btree (target_type, target_id, target_key);


--
-- PostgreSQL database dump complete
--

\unrestrict cnnHCZ6eYcN90fyHoJfKBqw2TN3AU4VvpLuuiRmzVr5bGecOcbyARcH3fsM4MSB


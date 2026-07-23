"""message module refactoring

Revision ID: 0001_message_refactor
Revises:
Create Date: 2026-07-23

Complete refactoring of message module:
- Drop all old msg_* tables
- Create 16 new tables for conversation, message, group, friend,
  notification, announcement, terminal, offline message queue
"""

from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "0001_message_refactor"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Drop old tables
    op.drop_table("msg_todo_assignee", if_exists=True)
    op.drop_table("msg_todo", if_exists=True)
    op.drop_table("msg_message_reaction", if_exists=True)
    op.drop_table("msg_message_attachment", if_exists=True)
    op.drop_table("msg_message_receipt", if_exists=True)
    op.drop_table("msg_group_join_request", if_exists=True)
    op.drop_table("msg_group_member", if_exists=True)
    op.drop_table("msg_thread_participant", if_exists=True)
    op.drop_table("msg_message", if_exists=True)
    op.drop_table("msg_thread", if_exists=True)
    op.drop_table("msg_group", if_exists=True)
    op.drop_table("msg_friend_request", if_exists=True)
    op.drop_table("msg_friend", if_exists=True)
    op.drop_table("msg_notification_read", if_exists=True)
    op.drop_table("msg_notification", if_exists=True)

    # ============================================================
    # 1. msg_conversation
    # ============================================================
    op.create_table(
        "msg_conversation",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("conversation_type", sa.String(32), nullable=False, comment="会话类型 DIRECT/GROUP"),
        sa.Column("title", sa.String(255), nullable=True, comment="会话标题"),
        sa.Column("avatar", sa.String(500), nullable=True, comment="会话头像"),
        sa.Column("group_id", sa.String(64), nullable=True, comment="关联群ID"),
        sa.Column("owner_account_type", sa.String(32), nullable=True, comment="创建者账户类型"),
        sa.Column("owner_account_id", sa.String(64), nullable=True, comment="创建者账户ID"),
        sa.Column("status", sa.String(32), nullable=False, server_default="ACTIVE", comment="状态"),
        sa.Column("last_message_id", sa.String(64), nullable=True, comment="最新消息ID"),
        sa.Column("last_message_at", sa.DateTime(timezone=True), nullable=True, comment="最新消息时间"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_index("ix_msg_conv_type_status_last", "msg_conversation", ["conversation_type", "status", "last_message_at"])
    op.create_index("ix_msg_conv_group", "msg_conversation", ["group_id"])

    # ============================================================
    # 2. msg_conversation_member
    # ============================================================
    op.create_table(
        "msg_conversation_member",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("conversation_id", sa.String(64), nullable=False, comment="会话ID"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("role", sa.String(32), nullable=False, server_default="MEMBER", comment="角色"),
        sa.Column("unread_count", sa.Integer(), nullable=False, server_default="0", comment="未读消息数"),
        sa.Column("last_read_message_id", sa.String(64), nullable=True, comment="最后已读消息ID"),
        sa.Column("last_read_at", sa.DateTime(timezone=True), nullable=True, comment="最后阅读时间"),
        sa.Column("last_delivered_at", sa.DateTime(timezone=True), nullable=True, comment="最后投递时间"),
        sa.Column("is_muted", sa.Boolean(), nullable=False, server_default="false", comment="是否免打扰"),
        sa.Column("is_pinned", sa.Boolean(), nullable=False, server_default="false", comment="是否置顶"),
        sa.Column("joined_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="加入时间"),
        sa.Column("left_at", sa.DateTime(timezone=True), nullable=True, comment="离开时间"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
    )
    op.create_unique_constraint("uq_msg_cmember_account", "msg_conversation_member", ["conversation_id", "account_type", "account_id"])
    op.create_index("ix_msg_cmember_account", "msg_conversation_member", ["account_type", "account_id"])

    # ============================================================
    # 3. msg_message (new)
    # ============================================================
    op.create_table(
        "msg_message",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("conversation_id", sa.String(64), nullable=False, comment="会话ID"),
        sa.Column("msg_type", sa.String(32), nullable=False, server_default="TEXT", comment="消息类型"),
        sa.Column("parent_id", sa.String(64), nullable=True, comment="回复消息ID"),
        sa.Column("sender_type", sa.String(32), nullable=False, server_default="USER", comment="发送方类型"),
        sa.Column("sender_account_type", sa.String(32), nullable=True, comment="发送者账户类型"),
        sa.Column("sender_account_id", sa.String(64), nullable=True, comment="发送者账户ID"),
        sa.Column("sender_name", sa.String(128), nullable=True, comment="发送者快照名称"),
        sa.Column("content", sa.Text(), nullable=False, comment="消息正文"),
        sa.Column("content_type", sa.String(32), nullable=False, server_default="TEXT", comment="内容格式"),
        sa.Column("reply_count", sa.Integer(), nullable=False, server_default="0", comment="回复数"),
        sa.Column("is_revoked", sa.Boolean(), nullable=False, server_default="false", comment="是否撤回"),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True, comment="撤回时间"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False, comment="消息时间"),
    )
    op.create_index("ix_msg_msg_conv_created", "msg_message", ["conversation_id", "created_at"])
    op.create_index("ix_msg_msg_parent", "msg_message", ["parent_id"])
    op.create_index("ix_msg_msg_sender", "msg_message", ["sender_account_type", "sender_account_id"])

    # ============================================================
    # 4. msg_message_read
    # ============================================================
    op.create_table(
        "msg_message_read",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("conversation_id", sa.String(64), nullable=False, comment="会话ID"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("last_read_message_id", sa.String(64), nullable=False, comment="最后已读消息ID"),
        sa.Column("last_read_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="最后阅读时间"),
        sa.Column("terminal_id", sa.String(64), nullable=True, comment="终端ID"),
    )
    op.create_unique_constraint("uq_msg_mread_cursor", "msg_message_read", ["conversation_id", "account_type", "account_id", "terminal_id"])
    op.create_index("ix_msg_mread_account", "msg_message_read", ["account_type", "account_id"])

    # ============================================================
    # 5. msg_message_attachment (new)
    # ============================================================
    op.create_table(
        "msg_message_attachment",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("message_id", sa.String(64), nullable=False, comment="消息ID"),
        sa.Column("file_id", sa.String(64), nullable=True, comment="关联sys_file.id"),
        sa.Column("name", sa.String(255), nullable=False, comment="文件名"),
        sa.Column("url", sa.String(1024), nullable=False, comment="文件访问URL"),
        sa.Column("content_type", sa.String(128), nullable=True, comment="MIME类型"),
        sa.Column("size", sa.BigInteger(), nullable=True, comment="文件大小"),
        sa.Column("attachment_type", sa.String(32), nullable=False, server_default="FILE", comment="附件类型"),
        sa.Column("thumbnail_url", sa.String(1024), nullable=True, comment="缩略图URL"),
        sa.Column("duration", sa.Integer(), nullable=True, comment="时长(秒)"),
        sa.Column("width", sa.Integer(), nullable=True, comment="宽度"),
        sa.Column("height", sa.Integer(), nullable=True, comment="高度"),
        sa.Column("sort", sa.Integer(), nullable=False, server_default="0", comment="排序"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
    )
    op.create_index("ix_msg_mattach_message", "msg_message_attachment", ["message_id", "sort"])

    # ============================================================
    # 6. msg_group (new)
    # ============================================================
    op.create_table(
        "msg_group",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("name", sa.String(128), nullable=False, comment="群名称"),
        sa.Column("avatar", sa.String(500), nullable=True, comment="群头像"),
        sa.Column("description", sa.Text(), nullable=True, comment="群简介"),
        sa.Column("owner_account_type", sa.String(32), nullable=False, comment="群主账户类型"),
        sa.Column("owner_account_id", sa.String(64), nullable=False, comment="群主账户ID"),
        sa.Column("status", sa.String(32), nullable=False, server_default="ENABLED", comment="状态"),
        sa.Column("join_mode", sa.String(32), nullable=False, server_default="APPROVAL", comment="入群方式"),
        sa.Column("max_members", sa.Integer(), nullable=False, server_default="200", comment="最大成员数"),
        sa.Column("member_count", sa.Integer(), nullable=False, server_default="0", comment="当前成员数"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_index("ix_msg_group_status", "msg_group", ["status"])
    op.create_index("ix_msg_group_owner", "msg_group", ["owner_account_type", "owner_account_id"])

    # ============================================================
    # 7. msg_group_member (new)
    # ============================================================
    op.create_table(
        "msg_group_member",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("group_id", sa.String(64), nullable=False, comment="群ID"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("role", sa.String(32), nullable=False, server_default="MEMBER", comment="角色"),
        sa.Column("nickname", sa.String(64), nullable=True, comment="群内昵称"),
        sa.Column("is_muted", sa.Boolean(), nullable=False, server_default="false", comment="是否免打扰"),
        sa.Column("joined_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="加入时间"),
        sa.Column("left_at", sa.DateTime(timezone=True), nullable=True, comment="离开时间"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
    )
    op.create_unique_constraint("uq_msg_gmember_account", "msg_group_member", ["group_id", "account_type", "account_id"])
    op.create_index("ix_msg_gmember_account", "msg_group_member", ["account_type", "account_id"])

    # ============================================================
    # 8. msg_group_join_request (new)
    # ============================================================
    op.create_table(
        "msg_group_join_request",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("group_id", sa.String(64), nullable=False, comment="群ID"),
        sa.Column("applicant_type", sa.String(32), nullable=False, comment="申请人账户类型"),
        sa.Column("applicant_id", sa.String(64), nullable=False, comment="申请人账户ID"),
        sa.Column("message", sa.Text(), nullable=True, comment="申请附言"),
        sa.Column("status", sa.String(32), nullable=False, server_default="PENDING", comment="状态"),
        sa.Column("handled_by_type", sa.String(32), nullable=True, comment="处理人账户类型"),
        sa.Column("handled_by_id", sa.String(64), nullable=True, comment="处理人账户ID"),
        sa.Column("handled_at", sa.DateTime(timezone=True), nullable=True, comment="处理时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_unique_constraint("uq_msg_gjoin_request", "msg_group_join_request", ["group_id", "applicant_type", "applicant_id"])
    op.create_index("ix_msg_gjoin_req_group", "msg_group_join_request", ["group_id", "status"])
    op.create_index("ix_msg_gjoin_req_applicant", "msg_group_join_request", ["applicant_type", "applicant_id"])

    # ============================================================
    # 9. msg_friend (new)
    # ============================================================
    op.create_table(
        "msg_friend",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("friend_account_type", sa.String(32), nullable=False, comment="好友账户类型"),
        sa.Column("friend_account_id", sa.String(64), nullable=False, comment="好友账户ID"),
        sa.Column("remark", sa.String(64), nullable=True, comment="备注名"),
        sa.Column("status", sa.String(32), nullable=False, server_default="ACTIVE", comment="状态"),
        sa.Column("friend_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="成为好友时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_unique_constraint("uq_msg_friend_pair", "msg_friend", ["account_type", "account_id", "friend_account_type", "friend_account_id"])
    op.create_index("ix_msg_friend_account", "msg_friend", ["account_type", "account_id"])
    op.create_index("ix_msg_friend_friend", "msg_friend", ["friend_account_type", "friend_account_id"])

    # ============================================================
    # 10. msg_friend_request (new)
    # ============================================================
    op.create_table(
        "msg_friend_request",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("applicant_type", sa.String(32), nullable=False, comment="申请人账户类型"),
        sa.Column("applicant_id", sa.String(64), nullable=False, comment="申请人账户ID"),
        sa.Column("recipient_type", sa.String(32), nullable=False, comment="接收人账户类型"),
        sa.Column("recipient_id", sa.String(64), nullable=False, comment="接收人账户ID"),
        sa.Column("message", sa.Text(), nullable=True, comment="申请附言"),
        sa.Column("status", sa.String(32), nullable=False, server_default="PENDING", comment="状态"),
        sa.Column("handled_at", sa.DateTime(timezone=True), nullable=True, comment="处理时间"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_unique_constraint("uq_msg_freq_pair", "msg_friend_request", ["applicant_type", "applicant_id", "recipient_type", "recipient_id"])
    op.create_index("ix_msg_freq_recipient", "msg_friend_request", ["recipient_type", "recipient_id", "status"])
    op.create_index("ix_msg_freq_applicant", "msg_friend_request", ["applicant_type", "applicant_id"])

    # ============================================================
    # 11. msg_notification (new)
    # ============================================================
    op.create_table(
        "msg_notification",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("title", sa.String(255), nullable=False, comment="标题"),
        sa.Column("content", sa.Text(), nullable=False, comment="内容"),
        sa.Column("content_type", sa.String(32), nullable=False, server_default="TEXT", comment="内容格式"),
        sa.Column("category", sa.String(32), nullable=False, server_default="BIZ", comment="分类"),
        sa.Column("severity", sa.String(32), nullable=False, server_default="INFO", comment="等级"),
        sa.Column("target_scope", sa.String(32), nullable=False, server_default="SPECIFIC", comment="目标范围"),
        sa.Column("target_account_type", sa.String(32), nullable=True, comment="目标账户类型"),
        sa.Column("target_account_id", sa.String(64), nullable=True, comment="目标账户ID"),
        sa.Column("sender_account_type", sa.String(32), nullable=True, comment="发送者账户类型"),
        sa.Column("sender_account_id", sa.String(64), nullable=True, comment="发送者账户ID"),
        sa.Column("source_type", sa.String(64), nullable=True, comment="来源模块"),
        sa.Column("source_id", sa.String(64), nullable=True, comment="来源业务ID"),
        sa.Column("status", sa.String(32), nullable=False, server_default="DRAFT", comment="状态"),
        sa.Column("publish_at", sa.DateTime(timezone=True), nullable=True, comment="发布时间"),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True, comment="撤回时间"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_index("ix_msg_notif_status_scope_pub", "msg_notification", ["status", "target_scope", "publish_at"])
    op.create_index("ix_msg_notif_target_account", "msg_notification", ["target_account_type", "target_account_id"])
    op.create_index("ix_msg_notif_category", "msg_notification", ["category"])

    # ============================================================
    # 12. msg_notification_read (new)
    # ============================================================
    op.create_table(
        "msg_notification_read",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("notification_id", sa.String(64), nullable=False, comment="通知ID"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("read_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="阅读时间"),
    )
    op.create_unique_constraint("uq_msg_nread_account", "msg_notification_read", ["notification_id", "account_type", "account_id"])
    op.create_index("ix_msg_nread_account", "msg_notification_read", ["account_type", "account_id"])

    # ============================================================
    # 13. msg_announcement
    # ============================================================
    op.create_table(
        "msg_announcement",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("title", sa.String(255), nullable=False, comment="标题"),
        sa.Column("content", sa.Text(), nullable=False, comment="内容"),
        sa.Column("content_type", sa.String(32), nullable=False, server_default="TEXT", comment="内容格式"),
        sa.Column("severity", sa.String(32), nullable=False, server_default="INFO", comment="等级"),
        sa.Column("target_scope", sa.String(32), nullable=False, server_default="ALL", comment="目标范围"),
        sa.Column("target_account_type", sa.String(32), nullable=True, comment="目标账户类型"),
        sa.Column("publish_locations", sa.JSON(), nullable=False, server_default='["im_page"]', comment="发布位置列表"),
        sa.Column("is_pinned", sa.Boolean(), nullable=False, server_default="false", comment="是否置顶"),
        sa.Column("pinned_until", sa.DateTime(timezone=True), nullable=True, comment="置顶截止时间"),
        sa.Column("sender_account_type", sa.String(32), nullable=True, comment="发布者账户类型"),
        sa.Column("sender_account_id", sa.String(64), nullable=True, comment="发布者账户ID"),
        sa.Column("status", sa.String(32), nullable=False, server_default="DRAFT", comment="状态"),
        sa.Column("publish_at", sa.DateTime(timezone=True), nullable=True, comment="发布时间"),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True, comment="撤回时间"),
        sa.Column("expire_at", sa.DateTime(timezone=True), nullable=True, comment="过期时间"),
        sa.Column("view_count", sa.Integer(), nullable=False, server_default="0", comment="查看次数"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("created_by", sa.String(64), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_by", sa.String(64), nullable=True),
    )
    op.create_index("ix_msg_ann_status_scope_pub", "msg_announcement", ["status", "target_scope", "publish_at"])
    op.create_index("ix_msg_ann_pinned", "msg_announcement", ["is_pinned", "pinned_until"])

    # ============================================================
    # 14. msg_announcement_read
    # ============================================================
    op.create_table(
        "msg_announcement_read",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("announcement_id", sa.String(64), nullable=False, comment="公告ID"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("read_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="阅读时间"),
    )
    op.create_unique_constraint("uq_msg_aread_account", "msg_announcement_read", ["announcement_id", "account_type", "account_id"])
    op.create_index("ix_msg_aread_account", "msg_announcement_read", ["account_type", "account_id"])

    # ============================================================
    # 15. msg_terminal
    # ============================================================
    op.create_table(
        "msg_terminal",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("account_type", sa.String(32), nullable=False, comment="账户类型"),
        sa.Column("account_id", sa.String(64), nullable=False, comment="账户ID"),
        sa.Column("device_type", sa.String(32), nullable=False, comment="设备类型"),
        sa.Column("device_name", sa.String(128), nullable=True, comment="设备名称"),
        sa.Column("device_id", sa.String(255), nullable=True, comment="设备唯一标识"),
        sa.Column("push_token", sa.String(500), nullable=True, comment="推送Token"),
        sa.Column("push_provider", sa.String(32), nullable=True, comment="推送渠道"),
        sa.Column("app_version", sa.String(32), nullable=True, comment="App版本号"),
        sa.Column("is_online", sa.Boolean(), nullable=False, server_default="false", comment="是否在线"),
        sa.Column("last_online_at", sa.DateTime(timezone=True), nullable=True, comment="最后在线时间"),
        sa.Column("last_login_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now(), comment="最后登录时间"),
        sa.Column("extra", sa.JSON(), nullable=False, server_default="{}", comment="扩展信息"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )
    op.create_unique_constraint("uq_msg_terminal_device", "msg_terminal", ["account_type", "account_id", "device_type", "device_id"])
    op.create_index("ix_msg_terminal_account", "msg_terminal", ["account_type", "account_id"])

    # ============================================================
    # 16. msg_offline_message_queue
    # ============================================================
    op.create_table(
        "msg_offline_message_queue",
        sa.Column("id", sa.String(64), primary_key=True, comment="主键"),
        sa.Column("message_id", sa.String(64), nullable=False, comment="消息ID"),
        sa.Column("conversation_id", sa.String(64), nullable=False, comment="会话ID"),
        sa.Column("target_account_type", sa.String(32), nullable=False, comment="目标用户账户类型"),
        sa.Column("target_account_id", sa.String(64), nullable=False, comment="目标用户账户ID"),
        sa.Column("event_type", sa.String(32), nullable=False, comment="事件类型"),
        sa.Column("event_payload", sa.JSON(), nullable=False, server_default="{}", comment="事件数据摘要"),
        sa.Column("status", sa.String(32), nullable=False, server_default="PENDING", comment="状态"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("delivered_at", sa.DateTime(timezone=True), nullable=True, comment="投递时间"),
    )
    op.create_index("ix_msg_offline_target_status", "msg_offline_message_queue", ["target_account_type", "target_account_id", "status", "created_at"])


def downgrade() -> None:
    op.drop_table("msg_offline_message_queue", if_exists=True)
    op.drop_table("msg_terminal", if_exists=True)
    op.drop_table("msg_announcement_read", if_exists=True)
    op.drop_table("msg_announcement", if_exists=True)
    op.drop_table("msg_notification_read", if_exists=True)
    op.drop_table("msg_notification", if_exists=True)
    op.drop_table("msg_friend_request", if_exists=True)
    op.drop_table("msg_friend", if_exists=True)
    op.drop_table("msg_group_join_request", if_exists=True)
    op.drop_table("msg_group_member", if_exists=True)
    op.drop_table("msg_group", if_exists=True)
    op.drop_table("msg_message_attachment", if_exists=True)
    op.drop_table("msg_message_read", if_exists=True)
    op.drop_table("msg_message", if_exists=True)
    op.drop_table("msg_conversation_member", if_exists=True)
    op.drop_table("msg_conversation", if_exists=True)

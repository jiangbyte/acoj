"""Course, announcement, task models."""

from datetime import datetime
from typing import Any

from sqlalchemy import DateTime, Float, Index, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjCourse(Base, TimestampMixin):
    __tablename__ = "oj_course"
    __table_args__ = (
        Index("ix_oj_course_status", "status"),
        Index("ix_oj_course_access_scope", "access_scope"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    name: Mapped[str] = mapped_column(String(200), nullable=False, comment="课程名称")
    summary: Mapped[str | None] = mapped_column(Text, comment="简介")
    cover_url: Mapped[str | None] = mapped_column(String(512), comment="封面")
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="DRAFT", comment="DRAFT|PUBLISHED|ARCHIVED")
    visibility: Mapped[str] = mapped_column(String(16), nullable=False, default="PRIVATE", comment="PUBLIC|PRIVATE")
    access_scope: Mapped[str] = mapped_column(
        String(16), nullable=False, default="CLASS", comment="OPEN公开课|CLASS私有课"
    )
    binding_mode: Mapped[str] = mapped_column(
        String(16), nullable=False, default="PER_CLASS", comment="SHARED|PER_CLASS"
    )
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="排序")
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict)


class OjCourseClass(Base, TimestampMixin):
    """课程 ↔ 班级多对多。合班一门课多行；分班每课一行。"""

    __tablename__ = "oj_course_class"
    __table_args__ = (
        UniqueConstraint("course_id", "class_id", name="uq_oj_course_class_course_class"),
        Index("ix_oj_course_class_course_id", "course_id"),
        Index("ix_oj_course_class_class_id", "class_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    course_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="课程ID")
    class_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="班级ID")


class OjCourseAnnouncement(Base, TimestampMixin):
    __tablename__ = "oj_course_announcement"
    __table_args__ = (Index("ix_oj_course_announcement_course_id", "course_id"),)

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    course_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="课程ID")
    title: Mapped[str] = mapped_column(String(200), nullable=False, comment="标题")
    content: Mapped[str | None] = mapped_column(Text, comment="内容(Markdown)")
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="DRAFT", comment="DRAFT|PUBLISHED|REVOKED")
    published_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="发布时间")


class OjCourseTask(Base, TimestampMixin):
    __tablename__ = "oj_course_task"
    __table_args__ = (
        Index("ix_oj_course_task_course_id", "course_id"),
        Index("ix_oj_course_task_course_status", "course_id", "status"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    course_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="课程ID")
    title: Mapped[str] = mapped_column(String(200), nullable=False, comment="任务标题")
    description: Mapped[str | None] = mapped_column(Text, comment="描述")
    mode: Mapped[str] = mapped_column(String(16), nullable=False, comment="REALTIME|ASYNC")
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="DRAFT", comment="DRAFT|PUBLISHED|CLOSED")
    open_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="实时任务开放")
    close_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="实时任务关闭")
    due_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="异步任务截止")
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="排序")
    extra: Mapped[dict[str, Any]] = mapped_column(JSON, nullable=False, default=dict)


class OjCourseTaskProblem(Base, TimestampMixin):
    __tablename__ = "oj_course_task_problem"
    __table_args__ = (
        UniqueConstraint("task_id", "problem_id", name="uq_oj_course_task_problem_task_problem"),
        Index("ix_oj_course_task_problem_task_id", "task_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    task_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="任务ID")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="排序")
    score: Mapped[float | None] = mapped_column(Float, comment="分值(可选)")


class OjCourseTaskProgress(Base, TimestampMixin):
    __tablename__ = "oj_course_task_progress"
    __table_args__ = (
        UniqueConstraint("task_id", "account_id", name="uq_oj_course_task_progress_task_account"),
        Index("ix_oj_course_task_progress_task_id", "task_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    task_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="任务ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    solved_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="已 AC 题数")
    total_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0, comment="总题数")
    status: Mapped[str] = mapped_column(
        String(16), nullable=False, default="NOT_STARTED", comment="NOT_STARTED|IN_PROGRESS|DONE"
    )
    finished_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), comment="完成时间")


class OjCourseTaskSubmission(Base, TimestampMixin):
    __tablename__ = "oj_course_task_submission"
    __table_args__ = (
        UniqueConstraint("task_id", "account_id", "problem_id", name="uq_oj_course_task_submission_task_account_problem"),
        Index("ix_oj_course_task_submission_task_id", "task_id"),
        Index("ix_oj_course_task_submission_submission_id", "submission_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=generate_snowflake_id)
    task_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="任务ID")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户ID")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交ID")

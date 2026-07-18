"""OJ problem problem model."""

from sqlalchemy import JSON, BigInteger, Boolean, Float, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.config.enums import StatusEnum
from app.modules.oj.enums import (
    OjJudgeMode,
    OjProblemType,
    OjProblemVisibility,
)
from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblem(Base, TimestampMixin):
    """题目主表，承载题面、限制、统计和判题模式入口。"""

    __tablename__ = "oj_problem"
    __table_args__ = (
        Index("ix_oj_problem_code", "code", unique=True),
        Index("ix_oj_problem_status_visibility", "status", "visibility"),
        Index("ix_oj_problem_difficulty", "difficulty"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    code: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目编码")
    title: Mapped[str] = mapped_column(String(255), nullable=False, comment="题目标题")
    summary: Mapped[str | None] = mapped_column(String(500), comment="摘要")
    description: Mapped[str | None] = mapped_column(Text, comment="题面")
    input_description: Mapped[str | None] = mapped_column(Text, comment="输入描述")
    output_description: Mapped[str | None] = mapped_column(Text, comment="输出描述")
    source: Mapped[str | None] = mapped_column(String(255), comment="来源")
    difficulty: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="难度")
    problem_type: Mapped[str] = mapped_column(
        String(32),
        default=OjProblemType.PROGRAM.value,
        nullable=False,
        comment=f"题目类型：{OjProblemType.__doc__}",
    )
    judge_mode: Mapped[str] = mapped_column(
        String(32),
        default=OjJudgeMode.STANDARD.value,
        nullable=False,
        comment=f"判题方式：{OjJudgeMode.__doc__}",
    )
    visibility: Mapped[str] = mapped_column(
        String(32),
        default=OjProblemVisibility.PUBLIC.value,
        nullable=False,
        comment=f"可见性：{OjProblemVisibility.__doc__}",
    )
    time_limit_ms: Mapped[int] = mapped_column(
        Integer, default=1000, nullable=False, comment="时间限制毫秒"
    )
    memory_limit_kb: Mapped[int] = mapped_column(
        Integer, default=262144, nullable=False, comment="内存限制KB"
    )
    stack_limit_kb: Mapped[int | None] = mapped_column(Integer, comment="栈限制KB")
    output_limit_kb: Mapped[int | None] = mapped_column(Integer, comment="输出限制KB")
    points: Mapped[float] = mapped_column(Float, default=100.0, nullable=False, comment="分值")
    partial: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否允许部分分"
    )
    allow_languages: Mapped[list[str]] = mapped_column(
        JSON,
        default=list,
        nullable=False,
        comment="允许语言ID列表",
    )
    spj_language_id: Mapped[str | None] = mapped_column(String(64), comment="Special Judge 语言ID")
    spj_source: Mapped[str | None] = mapped_column(Text, comment="Special Judge 源码")
    interactor_language_id: Mapped[str | None] = mapped_column(String(64), comment="交互器语言ID")
    interactor_source: Mapped[str | None] = mapped_column(Text, comment="交互器源码")
    remote_provider: Mapped[str | None] = mapped_column(String(64), comment="远程判题提供方")
    remote_problem_id: Mapped[str | None] = mapped_column(String(128), comment="远程题目ID")
    accepted_count: Mapped[int] = mapped_column(
        BigInteger, default=0, nullable=False, comment="通过次数"
    )
    submit_count: Mapped[int] = mapped_column(
        BigInteger, default=0, nullable=False, comment="提交次数"
    )
    ac_rate: Mapped[float] = mapped_column(Float, default=0.0, nullable=False, comment="通过率")
    sort: Mapped[int] = mapped_column(Integer, default=0, nullable=False, comment="排序")
    status: Mapped[str] = mapped_column(
        String(32),
        default=StatusEnum.ENABLED.value,
        nullable=False,
        comment="状态",
    )
    extra: Mapped[dict] = mapped_column(JSON, default=dict, nullable=False, comment="扩展信息")

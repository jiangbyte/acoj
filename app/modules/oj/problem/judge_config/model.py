"""OJ problem judge_config model — SPJ/interactor/remote 判题配置。"""

from sqlalchemy import Index, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemJudgeConfig(Base, TimestampMixin):
    """题目判题配置，从 OjProblem 拆分的 SPJ / interactor / 远程判题字段。"""

    __tablename__ = "oj_problem_judge_config"
    __table_args__ = (
        Index("uq_oj_problem_judge_config_problem", "problem_id", unique=True),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    spj_language_id: Mapped[str | None] = mapped_column(String(64), comment="Special Judge 语言ID")
    spj_source: Mapped[str | None] = mapped_column(Text, comment="Special Judge 源码")
    interactor_language_id: Mapped[str | None] = mapped_column(String(64), comment="交互器语言ID")
    interactor_source: Mapped[str | None] = mapped_column(Text, comment="交互器源码")
    remote_provider: Mapped[str | None] = mapped_column(String(64), comment="远程判题提供方")
    remote_problem_id: Mapped[str | None] = mapped_column(String(128), comment="远程题目ID")

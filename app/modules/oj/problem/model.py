from datetime import datetime

from sqlalchemy import Boolean, DateTime, Float, Index, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjProblemCategory(Base, TimestampMixin):
    """题目分类表。"""

    __tablename__ = "oj_problem_category"
    __table_args__ = (UniqueConstraint("name", name="uq_oj_problem_category_name"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    name: Mapped[str] = mapped_column(String(20), nullable=False, comment="题目分类标识")
    full_name: Mapped[str] = mapped_column(
        String(100), nullable=False, comment="题目分类名称"
    )


class OjProblemGroup(Base, TimestampMixin):
    """题目分组表。"""

    __tablename__ = "oj_problem_group"
    __table_args__ = (UniqueConstraint("name", name="uq_oj_problem_group_name"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    name: Mapped[str] = mapped_column(String(20), nullable=False, comment="题目分组标识")
    full_name: Mapped[str] = mapped_column(
        String(100), nullable=False, comment="题目分组名称"
    )



class OjProblem(Base, TimestampMixin):
    """题目表。"""

    __tablename__ = "oj_problem"
    __table_args__ = (
        UniqueConstraint("code", name="uq_oj_problem_code"),
        Index("ix_oj_problem_name", "name"),
        Index("ix_oj_problem_public_publish_at", "is_public", "publish_at"),
        Index("ix_oj_problem_manually_managed", "is_manually_managed"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    code: Mapped[str] = mapped_column(String(20), nullable=False, comment="题目标识")
    name: Mapped[str] = mapped_column(String(100), nullable=False, comment="题目名称")
    description: Mapped[str] = mapped_column(Text, nullable=False, comment="题面内容")
    group_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目分组")
    short_circuit: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否短路评测"
    )
    points: Mapped[float] = mapped_column(Float, nullable=False, comment="分数")
    partial: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否允许部分分"
    )
    is_public: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否公开可见"
    )
    is_manually_managed: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否手动管理"
    )
    publish_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="发布时间"
    )
    summary: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="题目摘要"
    )
    account_count: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="账户数量"
    )
    accepted_rate: Mapped[float] = mapped_column(Float, default=0, nullable=False, comment="通过率")
    is_full_markup: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否允许完整 Markdown"
    )
    submission_source_visibility_mode: Mapped[str] = mapped_column(
        String(1), default="F", nullable=False, comment="提交源码可见性"
    )
    is_organization_private: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否仅组织可见"
    )


class OjProblemData(Base, TimestampMixin):
    """题目数据表。"""

    __tablename__ = "oj_problem_data"
    __table_args__ = (UniqueConstraint("problem_id", name="uq_oj_problem_data_problem_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    zip_file: Mapped[str | None] = mapped_column(String(512), comment="数据压缩包")
    generator: Mapped[str | None] = mapped_column(String(512), comment="生成器文件")
    output_prefix: Mapped[int | None] = mapped_column(Integer, comment="输出前缀长度")
    output_limit: Mapped[int | None] = mapped_column(Integer, comment="输出长度限制")
    feedback: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="init.yml 生成反馈"
    )
    checker: Mapped[str] = mapped_column(String(10), default="", nullable=False, comment="校验器")
    unicode: Mapped[bool | None] = mapped_column(Boolean, comment="是否启用 Unicode")
    disable_big_math: Mapped[bool | None] = mapped_column(
        Boolean, comment="是否禁用大整数和高精度小数"
    )
    checker_arguments: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="校验器参数"
    )


class OjProblemTestCase(Base, TimestampMixin):
    """题目测试用例表。"""

    __tablename__ = "oj_problem_test_case"
    __table_args__ = (
        UniqueConstraint("dataset_id", "order", name="uq_oj_problem_test_case_dataset_order"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    dataset_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目数据集")
    order: Mapped[int] = mapped_column(Integer, nullable=False, comment="用例顺序")
    type: Mapped[str] = mapped_column(String(1), default="C", nullable=False, comment="用例类型")
    input_file: Mapped[str] = mapped_column(
        String(100), default="", nullable=False, comment="输入文件名"
    )
    output_file: Mapped[str] = mapped_column(
        String(100), default="", nullable=False, comment="输出文件名"
    )
    generator_arguments: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="生成器参数"
    )
    points: Mapped[int | None] = mapped_column(Integer, comment="分值")
    is_pretest: Mapped[bool] = mapped_column(Boolean, nullable=False, comment="是否预评测用例")
    output_prefix: Mapped[int | None] = mapped_column(Integer, comment="输出前缀长度")
    output_limit: Mapped[int | None] = mapped_column(Integer, comment="输出长度限制")
    checker: Mapped[str] = mapped_column(String(10), default="", nullable=False, comment="校验器")
    checker_arguments: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="校验器参数"
    )
    batch_dependencies: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="批量依赖"
    )


class OjProblemClarification(Base, TimestampMixin):
    """题目答疑表。"""

    __tablename__ = "oj_problem_clarification"
    __table_args__ = (Index("ix_oj_problem_clarification_problem", "problem_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="答疑题目")
    description: Mapped[str] = mapped_column(Text, nullable=False, comment="答疑内容")
    clarification_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="答疑时间"
    )


class OjLanguageLimit(Base, TimestampMixin):
    """题目语言限制表。"""

    __tablename__ = "oj_language_limit"
    __table_args__ = (
        UniqueConstraint("problem_id", "language_id", name="uq_oj_language_limit_problem_language"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    language_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="语言")
    time_limit: Mapped[float] = mapped_column(Float, nullable=False, comment="时间限制")
    memory_limit: Mapped[int] = mapped_column(Integer, nullable=False, comment="内存限制")


class OjSolution(Base, TimestampMixin):
    """题解表。"""

    __tablename__ = "oj_solution"
    __table_args__ = (UniqueConstraint("problem_id", name="uq_oj_solution_problem_id"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="关联题目"
    )
    is_public: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否公开可见"
    )
    publish_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="发布时间"
    )
    content: Mapped[str] = mapped_column(Text, nullable=False, comment="题解内容")


class OjProblemPointsVote(Base, TimestampMixin):
    """题目分值投票表。"""

    __tablename__ = "oj_problem_points_vote"
    __table_args__ = (
        Index("ix_oj_problem_points_vote_problem", "problem_id"),
        Index("ix_oj_problem_points_vote_voter", "voter_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    points: Mapped[int] = mapped_column(Integer, nullable=False, comment="建议分值")
    voter_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="投票人")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    vote_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="投票时间"
    )
    note: Mapped[str] = mapped_column(Text, default="", nullable=False, comment="备注")


class OjProblemCategoryRel(Base):
    """题目分类关系表。"""

    __tablename__ = "oj_problem_category_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id",
            "category_id",
            name="uq_oj_problem_category_rel_problem_category",
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    category_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目分类")


class OjProblemAuthorRel(Base):
    """题目作者关系表。"""

    __tablename__ = "oj_problem_author_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id", "account_id", name="uq_oj_problem_author_rel_problem_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjProblemCuratorRel(Base):
    """题目维护人关系表。"""

    __tablename__ = "oj_problem_curator_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id", "account_id", name="uq_oj_problem_curator_rel_problem_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjProblemTesterRel(Base):
    """题目测试员关系表。"""

    __tablename__ = "oj_problem_tester_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id", "account_id", name="uq_oj_problem_tester_rel_problem_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjProblemLanguageRel(Base):
    """题目允许语言关系表。"""

    __tablename__ = "oj_problem_language_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id", "language_id", name="uq_oj_problem_language_rel_problem_language"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    language_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="语言")


class OjProblemBannedAccountRel(Base):
    """题目封禁账户关系表。"""

    __tablename__ = "oj_problem_banned_account_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id", "account_id", name="uq_oj_problem_banned_account_rel_problem_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjProblemOrganizationRel(Base):
    """题目组织关系表。"""

    __tablename__ = "oj_problem_organization_rel"
    __table_args__ = (
        UniqueConstraint(
            "problem_id", "organization_id", name="uq_oj_problem_organization_rel_problem_org"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")


class OjSolutionAuthorRel(Base):
    """题解作者关系表。"""

    __tablename__ = "oj_solution_author_rel"
    __table_args__ = (
        UniqueConstraint(
            "solution_id", "account_id", name="uq_oj_solution_author_rel_solution_account"
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    solution_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题解")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")

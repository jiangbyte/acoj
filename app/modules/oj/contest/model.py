from datetime import datetime, timedelta

from sqlalchemy import (
    JSON,
    Boolean,
    DateTime,
    Float,
    Index,
    Integer,
    Interval,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column

from app.platform.db.base import Base
from app.platform.db.mixins import TimestampMixin
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestTag(Base, TimestampMixin):
    """比赛标签表。"""

    __tablename__ = "oj_contest_tag"
    __table_args__ = (UniqueConstraint("name", name="uq_oj_contest_tag_name"),)

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    name: Mapped[str] = mapped_column(String(20), nullable=False, comment="标签名称")
    color: Mapped[str] = mapped_column(String(7), nullable=False, comment="标签颜色")
    description: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="标签描述"
    )


class OjContest(Base, TimestampMixin):
    """比赛表。"""

    __tablename__ = "oj_contest"
    __table_args__ = (
        UniqueConstraint("key", name="uq_oj_contest_key"),
        Index("ix_oj_contest_name", "name"),
        Index("ix_oj_contest_start_at", "start_at"),
        Index("ix_oj_contest_end_at", "end_at"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    key: Mapped[str] = mapped_column(String(20), nullable=False, comment="比赛标识")
    name: Mapped[str] = mapped_column(String(100), nullable=False, comment="比赛名称")
    tester_see_scoreboard: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="测试员是否可看榜单"
    )
    tester_see_submissions: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="测试员是否可看提交"
    )
    description: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="描述"
    )
    start_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="开始时间"
    )
    end_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="结束时间"
    )
    time_limit: Mapped[timedelta | None] = mapped_column(Interval, comment="时间限制")
    is_visible: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否公开可见"
    )
    is_rated: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否计入评级"
    )
    scoreboard_visibility: Mapped[str] = mapped_column(
        String(1), default="V", nullable=False, comment="榜单可见性"
    )
    use_clarifications: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False, comment="是否启用答疑"
    )
    rating_floor: Mapped[int | None] = mapped_column(Integer, comment="评级下限")
    rating_ceiling: Mapped[int | None] = mapped_column(Integer, comment="评级上限")
    performance_ceiling_override: Mapped[int | None] = mapped_column(
        Integer, comment="表现分上限覆盖值"
    )
    rate_all: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否全部计入评级"
    )
    is_private: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否仅指定账户可见"
    )
    hide_problem_tags: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否隐藏题目标签"
    )
    hide_problem_authors: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否隐藏题目作者"
    )
    run_pretests_only: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否仅运行预评测"
    )
    show_short_display: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否显示短名称"
    )
    is_organization_private: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否仅组织可见"
    )
    limit_join_organizations: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否限制组织"
    )
    open_graph_image: Mapped[str] = mapped_column(
        String(150), default="", nullable=False, comment="OpenGraph 图片"
    )
    logo_override_image: Mapped[str] = mapped_column(
        String(150), default="", nullable=False, comment="Logo 覆盖图"
    )
    account_count: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="实时参赛人数"
    )
    summary: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="比赛摘要"
    )
    access_code: Mapped[str] = mapped_column(
        String(255), default="", nullable=False, comment="访问码"
    )
    format_name: Mapped[str] = mapped_column(
        String(32), default="default", nullable=False, comment="比赛赛制"
    )
    format_configuration: Mapped[dict | None] = mapped_column(JSON, comment="比赛赛制配置")
    problem_label_script: Mapped[str] = mapped_column(
        Text, default="", nullable=False, comment="题目标号脚本"
    )
    locked_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), comment="比赛锁定时间"
    )
    points_precision: Mapped[int] = mapped_column(
        Integer, default=3, nullable=False, comment="分数精度"
    )


class OjContestParticipation(Base, TimestampMixin):
    """比赛参赛记录表。"""

    __tablename__ = "oj_contest_participation"
    __table_args__ = (
        UniqueConstraint(
            "contest_id",
            "account_id",
            "virtual",
            name="uq_oj_contest_participation_contest_account_virtual",
        ),
        Index("ix_oj_contest_participation_score", "score"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="关联比赛"
    )
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
    started_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="开始时间"
    )
    score: Mapped[float] = mapped_column(Float, default=0, nullable=False, comment="得分")
    cumulative_time: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="累计用时"
    )
    is_disqualified: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否取消资格"
    )
    tie_breaker: Mapped[float] = mapped_column(
        Float, default=0, nullable=False, comment="排名打破平局值"
    )
    virtual: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="虚拟参赛ID"
    )
    format_data: Mapped[dict | None] = mapped_column(JSON, comment="比赛赛制数据")


class OjContestProblem(Base, TimestampMixin):
    """比赛题目关系表。"""

    __tablename__ = "oj_contest_problem"
    __table_args__ = (
        UniqueConstraint("problem_id", "contest_id", name="uq_oj_contest_problem_problem_contest"),
        Index("ix_oj_contest_problem_order", "order"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    points: Mapped[int] = mapped_column(Integer, nullable=False, comment="分数")
    partial: Mapped[bool] = mapped_column(
        Boolean, default=True, nullable=False, comment="是否部分分"
    )
    is_pretested: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否预评测"
    )
    order: Mapped[int] = mapped_column(Integer, nullable=False, comment="排序")
    output_prefix_override: Mapped[int | None] = mapped_column(
        Integer, comment="输出前缀长度覆盖值"
    )
    maximum_submissions: Mapped[int | None] = mapped_column(Integer, comment="最大提交次数")


class OjContestSubmission(Base, TimestampMixin):
    """比赛提交表。"""

    __tablename__ = "oj_contest_submission"
    __table_args__ = (
        UniqueConstraint("submission_id", name="uq_oj_contest_submission_submission_id"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    submission_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="提交")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    participation_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="参赛记录"
    )
    points: Mapped[float] = mapped_column(Float, default=0, nullable=False, comment="分数")
    is_pretest: Mapped[bool] = mapped_column(
        Boolean, default=False, nullable=False, comment="是否预评测"
    )


class OjRating(Base, TimestampMixin):
    """比赛评级表。"""

    __tablename__ = "oj_rating"
    __table_args__ = (
        UniqueConstraint("account_id", "contest_id", name="uq_oj_rating_account_contest"),
        UniqueConstraint("participation_id", name="uq_oj_rating_participation_id"),
        Index("ix_oj_rating_last_rated_at", "last_rated_at"),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    participation_id: Mapped[str] = mapped_column(
        String(64), nullable=False, comment="参赛记录"
    )
    rank: Mapped[int] = mapped_column(Integer, nullable=False, comment="排名")
    rating: Mapped[int] = mapped_column(Integer, nullable=False, comment="评级")
    rating_mean: Mapped[float] = mapped_column(Float, nullable=False, comment="原始评级")
    performance: Mapped[float] = mapped_column(Float, nullable=False, comment="比赛表现分")
    last_rated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, comment="最近评级时间"
    )


class OjContestMoss(Base, TimestampMixin):
    """比赛代码查重表。"""

    __tablename__ = "oj_contest_moss"
    __table_args__ = (
        UniqueConstraint(
            "contest_id",
            "problem_id",
            "language",
            name="uq_oj_contest_moss_contest_problem_language",
        ),
    )

    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目")
    language: Mapped[str] = mapped_column(String(10), nullable=False, comment="语言")
    submission_count: Mapped[int] = mapped_column(
        Integer, default=0, nullable=False, comment="提交数量"
    )
    result_url: Mapped[str | None] = mapped_column(String(512), comment="地址")


class OjContestAuthorRel(Base):
    """比赛作者关系表。"""

    __tablename__ = "oj_contest_author_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_author_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestCuratorRel(Base):
    """比赛维护人关系表。"""

    __tablename__ = "oj_contest_curator_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_curator_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestTesterRel(Base):
    """比赛测试员关系表。"""

    __tablename__ = "oj_contest_tester_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_tester_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestSpectatorRel(Base):
    """比赛观察者关系表。"""

    __tablename__ = "oj_contest_spectator_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_spectator_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestScoreboardViewerRel(Base):
    """比赛榜单查看人关系表。"""

    __tablename__ = "oj_contest_scoreboard_viewer_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_scoreboard_viewer_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestSubmissionViewerRel(Base):
    """比赛提交查看人关系表。"""

    __tablename__ = "oj_contest_submission_viewer_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_submission_viewer_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestRateExcludeRel(Base):
    """比赛评级排除账户关系表。"""

    __tablename__ = "oj_contest_rate_exclude_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_rate_exclude_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestPrivateContestantRel(Base):
    """私有比赛参赛账户关系表。"""

    __tablename__ = "oj_contest_private_contestant_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_private_contestant_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestBannedAccountRel(Base):
    """比赛封禁账户关系表。"""

    __tablename__ = "oj_contest_banned_account_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "account_id", name="uq_oj_contest_banned_account_rel_contest_account"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    account_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="账户")


class OjContestOrganizationRel(Base):
    """比赛组织关系表。"""

    __tablename__ = "oj_contest_organization_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "organization_id", name="uq_oj_contest_organization_rel_contest_org"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")


class OjContestJoinOrganizationRel(Base):
    """比赛可加入组织关系表。"""

    __tablename__ = "oj_contest_join_organization_rel"
    __table_args__ = (
        UniqueConstraint(
            "contest_id", "organization_id", name="uq_oj_contest_join_organization_rel_contest_org"
        ),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    organization_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="组织")


class OjContestClassRel(Base):
    """比赛班级关系表。"""

    __tablename__ = "oj_contest_class_rel"
    __table_args__ = (
        UniqueConstraint("contest_id", "class_id", name="uq_oj_contest_class_rel_contest_class"),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    class_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="班级")


class OjContestTagRel(Base):
    """比赛标签关系表。"""

    __tablename__ = "oj_contest_tag_rel"
    __table_args__ = (
        UniqueConstraint("contest_id", "tag_id", name="uq_oj_contest_tag_rel_contest_tag"),
    )
    id: Mapped[str] = mapped_column(
        String(64), primary_key=True, default=generate_snowflake_id, comment="主键"
    )
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛")
    tag_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="比赛标签")

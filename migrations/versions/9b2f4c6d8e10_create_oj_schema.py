"""create oj schema"""

from collections.abc import Sequence

from alembic import op

import app.modules.oj.community.announcement.model  # noqa: F401
import app.modules.oj.community.clarification.model  # noqa: F401
import app.modules.oj.community.comment.model  # noqa: F401
import app.modules.oj.community.favorite.model  # noqa: F401
import app.modules.oj.community.solution.model  # noqa: F401
import app.modules.oj.community.vote.model  # noqa: F401
import app.modules.oj.contest.contest.model  # noqa: F401
import app.modules.oj.contest.member.model  # noqa: F401
import app.modules.oj.contest.participation.model  # noqa: F401
import app.modules.oj.contest.problem.model  # noqa: F401
import app.modules.oj.contest.problem_result.model  # noqa: F401
import app.modules.oj.contest.rating.model  # noqa: F401
import app.modules.oj.judge.language.model  # noqa: F401
import app.modules.oj.judge.node.model  # noqa: F401
import app.modules.oj.judge.runtime_version.model  # noqa: F401
import app.modules.oj.judge.task.model  # noqa: F401
import app.modules.oj.problem.asset.model  # noqa: F401
import app.modules.oj.problem.dataset.model  # noqa: F401
import app.modules.oj.problem.member.model  # noqa: F401
import app.modules.oj.problem.objective_answer.model  # noqa: F401
import app.modules.oj.problem.problem.model  # noqa: F401
import app.modules.oj.problem.sample.model  # noqa: F401
import app.modules.oj.problem.tag.model  # noqa: F401
import app.modules.oj.problem.tag_relation.model  # noqa: F401
import app.modules.oj.problem.test_case.model  # noqa: F401
import app.modules.oj.submission.case.model  # noqa: F401
import app.modules.oj.submission.rejudge_record.model  # noqa: F401
import app.modules.oj.submission.source.model  # noqa: F401
import app.modules.oj.submission.submission.model  # noqa: F401
from app.platform.db.base import Base

revision: str = "9b2f4c6d8e10"
down_revision: str | Sequence[str] | None = "1d4e8f2a9b70"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


OJ_TABLES: tuple[str, ...] = (
    "oj_problem",
    "oj_problem_sample",
    "oj_problem_tag",
    "oj_problem_tag_relation",
    "oj_problem_member",
    "oj_problem_asset",
    "oj_dataset",
    "oj_test_case",
    "oj_objective_answer",
    "oj_language",
    "oj_judge_node",
    "oj_runtime_version",
    "oj_judge_task",
    "oj_submission",
    "oj_submission_source",
    "oj_submission_case",
    "oj_rejudge_record",
    "oj_contest",
    "oj_contest_member",
    "oj_contest_problem",
    "oj_contest_participation",
    "oj_contest_problem_result",
    "oj_contest_rating",
    "oj_solution",
    "oj_comment",
    "oj_vote",
    "oj_favorite",
    "oj_clarification",
    "oj_announcement",
)


def upgrade() -> None:
    for table_name in OJ_TABLES:
        _create_table(table_name)


def downgrade() -> None:
    for table_name in reversed(OJ_TABLES):
        op.drop_table(table_name)


def _create_table(table_name: str) -> None:
    table = Base.metadata.tables[table_name]
    op.create_table(
        table_name,
        *(column.copy() for column in table.columns),
        *(constraint.copy() for constraint in table.constraints),
    )
    for index in sorted(table.indexes, key=lambda item: item.name or ""):
        op.create_index(
            index.name,
            table_name,
            [column.name for column in index.columns],
            unique=index.unique,
        )

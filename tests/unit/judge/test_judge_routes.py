import pytest
from sqlalchemy import select

from app.factory import create_app
from app.modules.judge.enums import JudgeTaskStatus
from app.modules.judge.model import OjJudgeTask, OjSubmission, OjSubmissionCase
from app.modules.judge.schema import JudgeTaskFinishRequest
from app.modules.judge.service import JudgeService


def test_internal_judge_routes_are_registered() -> None:
    app = create_app()
    paths = {route.path for route in app.routes}

    assert "/api/v1/internal/judge/nodes/register" in paths
    assert "/api/v1/internal/judge/nodes/heartbeat" in paths
    assert "/api/v1/internal/judge/tasks/poll" in paths


@pytest.mark.asyncio
async def test_finish_task_persists_submission_result(db_session) -> None:
    submission = OjSubmission(
        id="sub-1",
        problem_id="p1000",
        user_id="u1",
        language="CPP17",
        source="int main(){}",
    )
    task = OjJudgeTask(
        id="task-1",
        submission_id=submission.id,
        problem_id=submission.problem_id,
        node_id="node-1",
        payload={},
    )
    db_session.add_all([submission, task])
    await db_session.commit()

    await JudgeService(db_session).finish_task(
        task.id,
        JudgeTaskFinishRequest(
            status="FINISHED",
            result={
                "status": "AC",
                "score": 100,
                "time_ms": 12,
                "memory_kb": 2048,
                "compile_message": None,
                "cases": [
                    {
                        "case_no": 1,
                        "batch_no": None,
                        "status": "AC",
                        "score": 100,
                        "total_score": 100,
                        "time_ms": 12,
                        "memory_kb": 2048,
                        "feedback": None,
                        "output_preview": "3\n",
                    }
                ],
            },
        ),
    )

    saved_submission = await db_session.get(OjSubmission, submission.id)
    saved_task = await db_session.get(OjJudgeTask, task.id)
    cases = (
        await db_session.execute(
            select(OjSubmissionCase).where(OjSubmissionCase.submission_id == submission.id)
        )
    ).scalars().all()

    assert saved_task.status == JudgeTaskStatus.FINISHED.value
    assert saved_submission.status == "AC"
    assert saved_submission.score == 100
    assert saved_submission.judger_id == "node-1"
    assert len(cases) == 1
    assert cases[0].status == "AC"

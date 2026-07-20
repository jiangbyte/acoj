"""门户提交聚合服务 — 加载元数据、持久化、构建 JudgeRequest 发 MQ。"""

from datetime import datetime, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import BusinessError, NotFoundError
from app.modules.oj.enums import OjJudgeMode, OjSubmitStatus
from app.modules.oj.judge.language.model import OjLanguage
from app.modules.oj.problem.dataset.model import OjDataset
from app.modules.oj.problem.problem.model import OjProblem
from app.modules.oj.problem.test_case.model import OjTestCase
from app.modules.oj.submission.source.model import OjSubmissionSource
from app.modules.oj.submission.submission.model import OjSubmission
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id
from app.platform.mq.producer import event_producer


class OjPortalSubmitService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def submit(
        self,
        problem_id: str,
        language_id: str,
        source: str,
        account_type: str,
        account_id: str,
    ) -> str:
        # 1. 校验题目
        problem = await self.db.get(OjProblem, problem_id)
        if not problem or problem.status != StatusEnum.ENABLED.value:
            raise NotFoundError("题目未找到或已禁用")

        judge_mode = problem.judge_mode
        if judge_mode not in (OjJudgeMode.STANDARD.value, OjJudgeMode.SPECIAL_JUDGE.value, OjJudgeMode.INTERACTIVE.value):
            raise BusinessError(f"不支持的判题模式: {judge_mode}")

        # SPJ 模式必须提供 spj_source
        if judge_mode == OjJudgeMode.SPECIAL_JUDGE.value and not problem.spj_source:
            raise BusinessError("SPJ 题目必须提供 spj_source")
        # 交互模式必须提供 interactor_source
        if judge_mode == OjJudgeMode.INTERACTIVE.value and not problem.interactor_source:
            raise BusinessError("交互式题目必须提供 interactor_source")

        # 2. 校验语言
        language = await self.db.get(OjLanguage, language_id)
        if not language or language.status != StatusEnum.ENABLED.value:
            raise NotFoundError("语言未找到或已禁用")

        if problem.allow_languages and language_id not in problem.allow_languages:
            raise BusinessError("该题目不支持此语言")

        # 3. 加载测试点元数据
        test_cases = await self._load_test_cases(problem_id)
        if not test_cases:
            raise BusinessError("该题目没有测试数据")

        # 4. 持久化 submission + source
        submission_id = generate_snowflake_id()
        async with transactional(self.db):
            submission = OjSubmission(
                id=submission_id,
                problem_id=problem_id,
                problem_code=problem.code,
                account_type=account_type,
                account_id=account_id,
                language_id=language_id,
                judge_mode=judge_mode,
                status=OjSubmitStatus.QUEUED.value,
                submitted_at=datetime.now(timezone.utc),
            )
            self.db.add(submission)

            source_record = OjSubmissionSource(
                submission_id=submission_id,
                source=source,
                size=len(source.encode("utf-8")),
            )
            self.db.add(source_record)
            await self.db.flush()

        # 5. 打包 JudgeRequest 发送 MQ
        judge_request = await self._build_judge_request(
            submission_id, problem, language, source, test_cases
        )

        await event_producer.publish(
            topic="request",
            payload=judge_request,
            exchange="oj.judge",
            exchange_type="direct",
        )

        return submission_id

    async def _build_judge_request(
        self,
        submission_id: str,
        problem: OjProblem,
        language: OjLanguage | None,
        source: str,
        test_cases: list[OjTestCase],
    ) -> dict:
        default_points = problem.points / max(len(test_cases), 1)

        request = {
            "submission_id": submission_id,
            "judge_mode": problem.judge_mode,
            "problem": {
                "code": problem.code,
                "time_limit_ms": problem.time_limit_ms,
                "memory_limit_kb": problem.memory_limit_kb,
                "points": problem.points,
                "partial": problem.partial,
            },
            "source": source,
            "test_cases": [
                {
                    "case_no": tc.case_no,
                    "points": tc.points or default_points,
                    "time_limit_ms": tc.time_limit_ms,
                    "memory_limit_kb": tc.memory_limit_kb,
                    "input_file": tc.input_file or None,
                    "output_file": tc.output_file or None,
                    "input_inline": tc.input_inline or None,
                    "output_inline": tc.output_inline or None,
                    "batch_no": tc.batch_no,
                    "batch_depends": tc.batch_dependencies,
                }
                for tc in test_cases
            ],
        }

        # 用户程序语言配置（所有判题模式都需要）
        if language:
            request["language"] = {
                "key": language.key,
                "name": language.name,
                "extension": language.extension,
                "compile_command": language.compile_command,
                "run_command": language.run_command,
            }

        # SPJ 模式：加载 SPJ checker
        if problem.judge_mode == OjJudgeMode.SPECIAL_JUDGE.value:
            if not problem.spj_language_id:
                raise BusinessError("SPJ 题目必须设置 spj_language_id")
            spj_lang = await self.db.get(OjLanguage, problem.spj_language_id)
            if not spj_lang:
                raise BusinessError(f"SPJ checker 语言未找到: {problem.spj_language_id}")
            request["spj"] = {
                "language": {
                    "key": spj_lang.key,
                    "name": spj_lang.name,
                    "extension": spj_lang.extension,
                    "compile_command": spj_lang.compile_command,
                    "run_command": spj_lang.run_command,
                },
                "source": problem.spj_source,
            }

        # 交互模式：加载交互器
        if problem.judge_mode == OjJudgeMode.INTERACTIVE.value:
            if not problem.interactor_language_id:
                raise BusinessError("交互式题目必须设置 interactor_language_id")
            interactor_lang = await self.db.get(OjLanguage, problem.interactor_language_id)
            if not interactor_lang:
                raise BusinessError(f"交互器语言未找到: {problem.interactor_language_id}")
            request["interactor"] = {
                "language": {
                    "key": interactor_lang.key,
                    "name": interactor_lang.name,
                    "extension": interactor_lang.extension,
                    "compile_command": interactor_lang.compile_command,
                    "run_command": interactor_lang.run_command,
                },
                "source": problem.interactor_source,
                "time_limit_ms": problem.time_limit_ms * 2,
                "memory_limit_kb": problem.memory_limit_kb,
            }

        return request

    async def _load_test_cases(self, problem_id: str) -> list[OjTestCase]:
        dataset_result = await self.db.execute(
            select(OjDataset.id).where(
                OjDataset.problem_id == problem_id,
                OjDataset.is_active == True,  # noqa: E712
            )
        )
        dataset_ids = dataset_result.scalars().all()
        if not dataset_ids:
            return []

        tc_result = await self.db.execute(
            select(OjTestCase)
            .where(
                OjTestCase.dataset_id.in_(dataset_ids),
                OjTestCase.case_type == "NORMAL",
            )
            .order_by(OjTestCase.sort, OjTestCase.case_no)
        )
        return list(tc_result.scalars().all())

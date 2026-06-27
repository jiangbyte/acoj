from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import GrantSubjectType
from app.core.schema.base import to_schema
from app.modules.iam.grant.repository import GrantRepository
from app.modules.iam.grant.schema import (
    SubjectPermissionGrantRequest,
    SubjectResourceGrantRequest,
    SysSubjectPermissionGrantRelSchema,
    SysSubjectResourceGrantRelSchema,
)
from app.modules.iam.permission.service import ensure_registered_permission
from app.platform.db.transaction import transactional


class GrantService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = GrantRepository(db)

    async def grant_subject_resource(
        self,
        payload: SubjectResourceGrantRequest,
    ) -> SysSubjectResourceGrantRelSchema:
        async with transactional(self.db):
            return to_schema(
                SysSubjectResourceGrantRelSchema,
                await self.repo.grant_subject_resource(payload),
            )

    async def grant_subject_permission(
        self,
        payload: SubjectPermissionGrantRequest,
    ) -> SysSubjectPermissionGrantRelSchema:
        if payload.subject_type == GrantSubjectType.ROLE:
            raise ValueError("Role should grant resources instead of direct permission exceptions")
        await ensure_registered_permission(payload.permission_key)
        async with transactional(self.db):
            return to_schema(
                SysSubjectPermissionGrantRelSchema,
                await self.repo.grant_subject_permission(payload),
            )

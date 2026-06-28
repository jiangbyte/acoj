from collections import defaultdict
from typing import Literal, TypedDict

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import DataScope, StatusEnum
from app.core.exceptions.business import NotFoundError
from app.modules.iam.enums import GrantEffect, GrantMode, GrantSubjectType
from app.modules.iam.account.model import SysAccount, SysAccountGroupRel
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.grant.model import SysSubjectPermissionGrantRel, SysSubjectResourceGrantRel
from app.modules.iam.grant.schema import SubjectPermissionGrantRequest, SubjectResourceGrantRequest
from app.modules.iam.group.model import SysGroup
from app.modules.iam.resource.model import SysResource, SysResourcePermissionRel
from app.modules.iam.role.model import SysRole


class AccountResourceGrantRecord(TypedDict):
    subject_type: GrantSubjectType | str
    subject_id: str
    resource_id: str
    grant_mode: GrantMode | str
    effect: GrantEffect | str


class PermissionGrantRecord(TypedDict):
    permission_key: str
    data_scope: DataScope | str
    custom_scope_dept_ids: list[str]
    effect: GrantEffect | str
    source_type: GrantSubjectType | Literal["RESOURCE"] | str
    source_id: str


class GrantRepository:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.account_repo = AccountRepository(db)

    async def grant_subject_resource(
        self,
        payload: SubjectResourceGrantRequest,
    ) -> SysSubjectResourceGrantRel:
        await self._ensure_subject_exists(payload.subject_type.value, payload.subject_id)
        if not await self.db.get(SysResource, payload.resource_id):
            raise NotFoundError("Resource not found")
        relation = SysSubjectResourceGrantRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def grant_subject_permission(
        self,
        payload: SubjectPermissionGrantRequest,
    ) -> SysSubjectPermissionGrantRel:
        await self._ensure_subject_exists(payload.subject_type.value, payload.subject_id)
        relation = SysSubjectPermissionGrantRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def get_account_resource_grants(
        self,
        account_id: str,
    ) -> list[AccountResourceGrantRecord]:
        role_ids = await self.account_repo.get_account_role_ids(account_id)
        group_ids = await self.account_repo.get_account_group_ids(account_id)
        role_filter = SysSubjectResourceGrantRel.subject_id.in_(role_ids) if role_ids else False
        group_filter = SysSubjectResourceGrantRel.subject_id.in_(group_ids) if group_ids else False
        stmt = select(SysSubjectResourceGrantRel).where(
            SysSubjectResourceGrantRel.status == StatusEnum.ENABLED.value,
            or_(
                (SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ACCOUNT.value)
                & (SysSubjectResourceGrantRel.subject_id == account_id),
                (SysSubjectResourceGrantRel.subject_type == GrantSubjectType.GROUP.value)
                & group_filter,
                (SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ROLE.value)
                & role_filter,
            ),
        )
        grants = (await self.db.execute(stmt)).scalars().all()
        return [
            {
                "subject_type": grant.subject_type,
                "subject_id": grant.subject_id,
                "resource_id": grant.resource_id,
                "grant_mode": grant.grant_mode,
                "effect": grant.effect,
            }
            for grant in grants
        ]

    async def get_account_permission_exception_grants(
        self,
        account_id: str,
    ) -> list[PermissionGrantRecord]:
        role_ids = await self.account_repo.get_account_role_ids(account_id)
        group_ids = await self.account_repo.get_account_group_ids(account_id)
        role_filter = (
            SysSubjectPermissionGrantRel.subject_id.in_(role_ids) if role_ids else False
        )
        group_filter = (
            SysSubjectPermissionGrantRel.subject_id.in_(group_ids) if group_ids else False
        )
        stmt = select(SysSubjectPermissionGrantRel).where(
            SysSubjectPermissionGrantRel.status == StatusEnum.ENABLED.value,
            or_(
                (SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ACCOUNT.value)
                & (SysSubjectPermissionGrantRel.subject_id == account_id),
                (SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ROLE.value)
                & role_filter,
                (SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.GROUP.value)
                & group_filter,
            ),
        )
        grants = (await self.db.execute(stmt)).scalars().all()
        return [
            {
                "permission_key": grant.permission_key,
                "data_scope": grant.data_scope,
                "custom_scope_dept_ids": list(grant.custom_scope_dept_ids),
                "effect": grant.effect,
                "source_type": grant.subject_type,
                "source_id": grant.subject_id,
            }
            for grant in grants
        ]

    async def get_account_effective_permissions(
        self,
        account_id: str,
    ) -> list[PermissionGrantRecord]:
        resource_grants = await self.get_account_resource_grants(account_id)
        if resource_grants:
            resource_ids = sorted({grant["resource_id"] for grant in resource_grants})
            permission_stmt = select(SysResourcePermissionRel).where(
                SysResourcePermissionRel.status == StatusEnum.ENABLED.value,
                SysResourcePermissionRel.resource_id.in_(resource_ids),
            )
            permission_rows = (await self.db.execute(permission_stmt)).scalars().all()
        else:
            permission_rows = []

        grant_source_map: dict[str, list[AccountResourceGrantRecord]] = defaultdict(list)
        for grant in resource_grants:
            grant_source_map[grant["resource_id"]].append(grant)

        permission_map: dict[str, PermissionGrantRecord] = {}
        for row in permission_rows:
            sources = grant_source_map.get(row.resource_id, [])
            source_type: GrantSubjectType | Literal["RESOURCE"] | str
            source_id: str
            if sources:
                source_type = sources[0]["subject_type"]
                source_id = sources[0]["subject_id"]
            else:
                source_type = "RESOURCE"
                source_id = row.resource_id
            permission_map[row.permission_key] = {
                "permission_key": row.permission_key,
                "data_scope": row.data_scope,
                "custom_scope_dept_ids": list(row.custom_scope_dept_ids),
                "effect": GrantEffect.ALLOW.value,
                "source_type": source_type,
                "source_id": source_id,
            }

        exception_grants = await self.get_account_permission_exception_grants(account_id)
        for exception_grant in exception_grants:
            permission_map[exception_grant["permission_key"]] = exception_grant

        return list(sorted(permission_map.values(), key=lambda item: item["permission_key"]))

    async def _ensure_subject_exists(self, subject_type: str, subject_id: str) -> None:
        entity: object | None
        if subject_type == GrantSubjectType.ROLE.value:
            entity = await self.db.get(SysRole, subject_id)
        elif subject_type == GrantSubjectType.ACCOUNT.value:
            entity = await self.db.get(SysAccount, subject_id)
        elif subject_type == GrantSubjectType.GROUP.value:
            entity = await self.db.get(SysGroup, subject_id)
        else:
            entity = None
        if not entity:
            raise NotFoundError("Subject not found")

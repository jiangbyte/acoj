from collections import defaultdict
from datetime import UTC, datetime
from typing import Literal, TypedDict

from sqlalchemy import delete, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import DataScope, StatusEnum
from app.core.exceptions.business import NotFoundError
from app.modules.iam.enums import GrantEffect, GrantMode, GrantSubjectType
from app.modules.iam.account.model import (
    SysAccount,
    SysAccountDeptRel,
    SysAccountGroupRel,
    SysAccountRoleRel,
)
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.grant.model import SysSubjectPermissionGrantRel, SysSubjectResourceGrantRel
from app.modules.iam.group.model import SysGroup, SysGroupRoleRel
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


class AccountAuthorizationRecord(TypedDict):
    role_ids: list[str]
    role_codes: list[str]
    group_ids: list[str]
    dept_ids: list[str]
    resource_ids: list[str]
    button_codes: list[str]
    permission_keys: list[str]
    permission_grants: list[PermissionGrantRecord]


_PERMISSION_PRIORITY_RESOURCE = 0
_PERMISSION_PRIORITY_ROLE = 10
_PERMISSION_PRIORITY_GROUP = 20
_PERMISSION_PRIORITY_ACCOUNT = 30


class GrantRepository:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.account_repo = AccountRepository(db)

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
                SysSubjectResourceGrantRel.expired_at.is_(None),
                SysSubjectResourceGrantRel.expired_at > datetime.now(UTC),
            ),
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
                SysSubjectPermissionGrantRel.expired_at.is_(None),
                SysSubjectPermissionGrantRel.expired_at > datetime.now(UTC),
            ),
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
        return (await self.get_account_authorization(account_id))["permission_grants"]

    async def get_account_resource_ids(self, account_id: str) -> list[str]:
        resource_grants = await self.get_account_resource_grants(account_id)
        return sorted({grant["resource_id"] for grant in resource_grants if grant["effect"] != GrantEffect.DENY.value})

    async def get_account_authorization(self, account_id: str) -> AccountAuthorizationRecord:
        authorizations = await self.get_accounts_authorization([account_id])
        return authorizations[account_id]

    async def get_accounts_authorization(
        self,
        account_ids: list[str],
    ) -> dict[str, AccountAuthorizationRecord]:
        unique_account_ids = list(dict.fromkeys(account_ids))
        authorizations = {
            account_id: AccountAuthorizationRecord(
                role_ids=[],
                role_codes=[],
                group_ids=[],
                dept_ids=[],
                resource_ids=[],
                button_codes=[],
                permission_keys=[],
                permission_grants=[],
            )
            for account_id in unique_account_ids
        }
        if not unique_account_ids:
            return authorizations

        group_rows = (
            await self.db.execute(
                select(SysAccountGroupRel.account_id, SysAccountGroupRel.group_id).where(
                    SysAccountGroupRel.account_id.in_(unique_account_ids)
                )
            )
        ).all()
        dept_rows = (
            await self.db.execute(
                select(SysAccountDeptRel.account_id, SysAccountDeptRel.dept_id).where(
                    SysAccountDeptRel.account_id.in_(unique_account_ids)
                )
            )
        ).all()
        direct_role_rows = (
            await self.db.execute(
                select(SysAccountRoleRel.account_id, SysAccountRoleRel.role_id).where(
                    SysAccountRoleRel.account_id.in_(unique_account_ids)
                )
            )
        ).all()
        group_role_rows = (
            await self.db.execute(
                select(SysAccountGroupRel.account_id, SysGroupRoleRel.role_id)
                .join(SysGroupRoleRel, SysGroupRoleRel.group_id == SysAccountGroupRel.group_id)
                .where(SysAccountGroupRel.account_id.in_(unique_account_ids))
            )
        ).all()

        account_ids_by_group: dict[str, set[str]] = defaultdict(set)
        account_ids_by_role: dict[str, set[str]] = defaultdict(set)
        role_ids_by_account: dict[str, set[str]] = defaultdict(set)

        for account_id, group_id in group_rows:
            account_id = str(account_id)
            group_id = str(group_id)
            authorizations[account_id]["group_ids"].append(group_id)
            account_ids_by_group[group_id].add(account_id)

        for account_id, dept_id in dept_rows:
            authorizations[str(account_id)]["dept_ids"].append(str(dept_id))

        for account_id, role_id in list(direct_role_rows) + list(group_role_rows):
            account_id = str(account_id)
            role_id = str(role_id)
            role_ids_by_account[account_id].add(role_id)
            account_ids_by_role[role_id].add(account_id)

        role_ids = sorted({role_id for values in role_ids_by_account.values() for role_id in values})
        role_code_map: dict[str, str] = {}
        if role_ids:
            role_rows = (
                await self.db.execute(select(SysRole.id, SysRole.code).where(SysRole.id.in_(role_ids)))
            ).all()
            role_code_map = {str(role_id): str(code) for role_id, code in role_rows}

        for account_id, account_role_ids in role_ids_by_account.items():
            sorted_role_ids = sorted(account_role_ids)
            authorizations[account_id]["role_ids"] = sorted_role_ids
            authorizations[account_id]["role_codes"] = sorted(
                role_code_map[role_id] for role_id in sorted_role_ids if role_id in role_code_map
            )

        resource_grants_by_account = await self._list_resource_grants_by_account(
            unique_account_ids,
            account_ids_by_group,
            account_ids_by_role,
        )
        permission_grants_by_account = await self._list_permission_grants_by_account(
            unique_account_ids,
            account_ids_by_group,
            account_ids_by_role,
            resource_grants_by_account,
        )

        for account_id in unique_account_ids:
            resource_grants = resource_grants_by_account.get(account_id, [])
            permission_grants = permission_grants_by_account.get(account_id, [])
            permission_keys = sorted({grant["permission_key"] for grant in permission_grants})
            authorizations[account_id]["group_ids"] = sorted(set(authorizations[account_id]["group_ids"]))
            authorizations[account_id]["dept_ids"] = sorted(set(authorizations[account_id]["dept_ids"]))
            authorizations[account_id]["resource_ids"] = sorted(
                {
                    grant["resource_id"]
                    for grant in resource_grants
                    if grant["effect"] != GrantEffect.DENY.value
                }
            )
            authorizations[account_id]["permission_grants"] = permission_grants
            authorizations[account_id]["permission_keys"] = permission_keys
            authorizations[account_id]["button_codes"] = permission_keys.copy()

        return authorizations

    async def _list_resource_grants_by_account(
        self,
        account_ids: list[str],
        account_ids_by_group: dict[str, set[str]],
        account_ids_by_role: dict[str, set[str]],
    ) -> dict[str, list[AccountResourceGrantRecord]]:
        subject_conditions = [
            (SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ACCOUNT.value)
            & (SysSubjectResourceGrantRel.subject_id.in_(account_ids))
        ]
        if account_ids_by_group:
            subject_conditions.append(
                (SysSubjectResourceGrantRel.subject_type == GrantSubjectType.GROUP.value)
                & (SysSubjectResourceGrantRel.subject_id.in_(account_ids_by_group.keys()))
            )
        if account_ids_by_role:
            subject_conditions.append(
                (SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ROLE.value)
                & (SysSubjectResourceGrantRel.subject_id.in_(account_ids_by_role.keys()))
            )
        stmt = select(SysSubjectResourceGrantRel).where(
            SysSubjectResourceGrantRel.status == StatusEnum.ENABLED.value,
            or_(
                SysSubjectResourceGrantRel.expired_at.is_(None),
                SysSubjectResourceGrantRel.expired_at > datetime.now(UTC),
            ),
            or_(*subject_conditions),
        )
        grants = list((await self.db.execute(stmt)).scalars().all())
        grants_by_account: dict[str, list[AccountResourceGrantRecord]] = defaultdict(list)
        for grant in grants:
            target_account_ids: set[str]
            if grant.subject_type == GrantSubjectType.ACCOUNT.value:
                target_account_ids = {grant.subject_id}
            elif grant.subject_type == GrantSubjectType.GROUP.value:
                target_account_ids = account_ids_by_group.get(grant.subject_id, set())
            elif grant.subject_type == GrantSubjectType.ROLE.value:
                target_account_ids = account_ids_by_role.get(grant.subject_id, set())
            else:
                target_account_ids = set()
            record: AccountResourceGrantRecord = {
                "subject_type": grant.subject_type,
                "subject_id": grant.subject_id,
                "resource_id": grant.resource_id,
                "grant_mode": grant.grant_mode,
                "effect": grant.effect,
            }
            for account_id in target_account_ids:
                grants_by_account[account_id].append(record)
        return grants_by_account

    async def _list_permission_grants_by_account(
        self,
        account_ids: list[str],
        account_ids_by_group: dict[str, set[str]],
        account_ids_by_role: dict[str, set[str]],
        resource_grants_by_account: dict[str, list[AccountResourceGrantRecord]],
    ) -> dict[str, list[PermissionGrantRecord]]:
        cascade_resource_ids = sorted(
            {
                grant["resource_id"]
                for grants in resource_grants_by_account.values()
                for grant in grants
                if grant["grant_mode"] == GrantMode.CASCADE.value
                and grant["effect"] != GrantEffect.DENY.value
            }
        )
        permission_rows = []
        if cascade_resource_ids:
            permission_stmt = select(SysResourcePermissionRel).where(
                SysResourcePermissionRel.status == StatusEnum.ENABLED.value,
                SysResourcePermissionRel.resource_id.in_(cascade_resource_ids),
            )
            permission_rows = list((await self.db.execute(permission_stmt)).scalars().all())

        permission_rows_by_resource: dict[str, list[SysResourcePermissionRel]] = defaultdict(list)
        for row in permission_rows:
            permission_rows_by_resource[row.resource_id].append(row)

        permission_map_by_account: dict[
            str,
            dict[str, tuple[int, PermissionGrantRecord]],
        ] = defaultdict(dict)
        for account_id, resource_grants in resource_grants_by_account.items():
            for grant in resource_grants:
                if (
                    grant["grant_mode"] != GrantMode.CASCADE.value
                    or grant["effect"] == GrantEffect.DENY.value
                ):
                    continue
                for row in permission_rows_by_resource.get(grant["resource_id"], []):
                    self._apply_permission_record(
                        permission_map_by_account[account_id],
                        _PERMISSION_PRIORITY_RESOURCE,
                        {
                            "permission_key": row.permission_key,
                            "data_scope": row.data_scope,
                            "custom_scope_dept_ids": list(row.custom_scope_dept_ids),
                            "effect": GrantEffect.ALLOW.value,
                            "source_type": grant["subject_type"],
                            "source_id": grant["subject_id"],
                        },
                    )

        subject_conditions = [
            (SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ACCOUNT.value)
            & (SysSubjectPermissionGrantRel.subject_id.in_(account_ids))
        ]
        if account_ids_by_group:
            subject_conditions.append(
                (SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.GROUP.value)
                & (SysSubjectPermissionGrantRel.subject_id.in_(account_ids_by_group.keys()))
            )
        if account_ids_by_role:
            subject_conditions.append(
                (SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ROLE.value)
                & (SysSubjectPermissionGrantRel.subject_id.in_(account_ids_by_role.keys()))
            )
        exception_stmt = select(SysSubjectPermissionGrantRel).where(
            SysSubjectPermissionGrantRel.status == StatusEnum.ENABLED.value,
            or_(
                SysSubjectPermissionGrantRel.expired_at.is_(None),
                SysSubjectPermissionGrantRel.expired_at > datetime.now(UTC),
            ),
            or_(*subject_conditions),
        )
        exception_grants = list((await self.db.execute(exception_stmt)).scalars().all())
        for grant in exception_grants:
            if grant.subject_type == GrantSubjectType.ACCOUNT.value:
                target_account_ids = {grant.subject_id}
            elif grant.subject_type == GrantSubjectType.GROUP.value:
                target_account_ids = account_ids_by_group.get(grant.subject_id, set())
            elif grant.subject_type == GrantSubjectType.ROLE.value:
                target_account_ids = account_ids_by_role.get(grant.subject_id, set())
            else:
                target_account_ids = set()
            for account_id in target_account_ids:
                self._apply_permission_record(
                    permission_map_by_account[account_id],
                    self._permission_priority(str(grant.subject_type)),
                    {
                        "permission_key": grant.permission_key,
                        "data_scope": grant.data_scope,
                        "custom_scope_dept_ids": list(grant.custom_scope_dept_ids),
                        "effect": grant.effect,
                        "source_type": grant.subject_type,
                        "source_id": grant.subject_id,
                    },
                )

        return {
            account_id: [
                record
                for _, record in sorted(
                    permission_map.values(),
                    key=lambda item: item[1]["permission_key"],
                )
                if record["effect"] != GrantEffect.DENY.value
            ]
            for account_id, permission_map in permission_map_by_account.items()
        }

    def _apply_permission_record(
        self,
        permission_map: dict[str, tuple[int, PermissionGrantRecord]],
        priority: int,
        record: PermissionGrantRecord,
    ) -> None:
        current = permission_map.get(record["permission_key"])
        if current is None or priority >= current[0]:
            permission_map[record["permission_key"]] = (priority, record)

    def _permission_priority(self, subject_type: str) -> int:
        if subject_type == GrantSubjectType.ACCOUNT.value:
            return _PERMISSION_PRIORITY_ACCOUNT
        if subject_type == GrantSubjectType.GROUP.value:
            return _PERMISSION_PRIORITY_GROUP
        if subject_type == GrantSubjectType.ROLE.value:
            return _PERMISSION_PRIORITY_ROLE
        return _PERMISSION_PRIORITY_RESOURCE

    async def replace_subject_resource_grants(
        self,
        subject_type: GrantSubjectType,
        subject_id: str,
        resource_ids: list[str],
    ) -> None:
        await self._ensure_subject_exists(subject_type.value, subject_id)
        unique_resource_ids = list(dict.fromkeys(resource_ids))
        if unique_resource_ids:
            stmt = select(SysResource.id).where(SysResource.id.in_(unique_resource_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(unique_resource_ids):
                raise NotFoundError("Resource not found")
        await self.db.execute(
            delete(SysSubjectResourceGrantRel).where(
                SysSubjectResourceGrantRel.subject_type == subject_type.value,
                SysSubjectResourceGrantRel.subject_id == subject_id,
            )
        )
        for resource_id in unique_resource_ids:
            self.db.add(
                SysSubjectResourceGrantRel(
                    subject_type=subject_type.value,
                    subject_id=subject_id,
                    resource_id=resource_id,
                    grant_mode=GrantMode.CASCADE.value,
                    effect=GrantEffect.ALLOW.value,
                )
            )
        await self.db.flush()

    async def list_subject_resource_grants(
        self,
        subject_type: GrantSubjectType,
        subject_id: str,
    ) -> list[dict[str, object]]:
        await self._ensure_subject_exists(subject_type.value, subject_id)
        stmt = (
            select(SysSubjectResourceGrantRel)
            .where(
                SysSubjectResourceGrantRel.subject_type == subject_type.value,
                SysSubjectResourceGrantRel.subject_id == subject_id,
            )
            .order_by(SysSubjectResourceGrantRel.id.asc())
        )
        grants = list((await self.db.execute(stmt)).scalars().all())
        resource_ids = [grant.resource_id for grant in grants]
        if not resource_ids:
            return []

        resource_stmt = select(SysResource).where(SysResource.id.in_(resource_ids))
        resources = list((await self.db.execute(resource_stmt)).scalars().all())
        resource_map = {resource.id: resource for resource in resources}

        permission_stmt = select(SysResourcePermissionRel).where(
            SysResourcePermissionRel.resource_id.in_(resource_ids)
        )
        permissions = list((await self.db.execute(permission_stmt)).scalars().all())
        permission_map: dict[str, list[str]] = defaultdict(list)
        for permission in permissions:
            permission_map[permission.resource_id].append(permission.permission_key)

        menu_resource_ids = set()
        for resource_id in resource_ids:
            resource = resource_map.get(resource_id)
            if not resource:
                continue
            if resource.resource_type in {"BUTTON", "ACTION"}:
                menu_resource_ids.add(resource.parent_id or resource.id)
            else:
                menu_resource_ids.add(resource.id)

        grant_map: dict[str, set[str]] = defaultdict(set)
        for resource_id in menu_resource_ids:
            grant_map[resource_id]
        for resource_id in resource_ids:
            resource = resource_map.get(resource_id)
            if not resource:
                continue
            permission_keys = permission_map.get(resource.id) or [resource.code]
            if resource.resource_type in {"BUTTON", "ACTION"}:
                parent_id = resource.parent_id or resource.id
                grant_map[parent_id].update(permission_keys)

        return [
            {
                "resource_id": resource_id,
                "permission_keys": sorted(permission_keys),
            }
            for resource_id, permission_keys in sorted(grant_map.items())
        ]

    async def replace_subject_resource_grant_infos(
        self,
        subject_type: GrantSubjectType,
        subject_id: str,
        grant_info_list,
    ) -> None:
        await self._ensure_subject_exists(subject_type.value, subject_id)
        resource_ids = list(dict.fromkeys(item.resource_id for item in grant_info_list))
        original_resource_ids = set(resource_ids)
        permission_keys = list(
            dict.fromkeys(
                permission_key
                for item in grant_info_list
                for permission_key in item.permission_keys
            )
        )
        if resource_ids:
            stmt = select(SysResource.id).where(SysResource.id.in_(resource_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(resource_ids):
                raise NotFoundError("Resource not found")
        if permission_keys:
            permission_resource_stmt = select(
                SysResourcePermissionRel.permission_key,
                SysResourcePermissionRel.resource_id,
            ).where(SysResourcePermissionRel.permission_key.in_(permission_keys))
            permission_resource_rows = list((await self.db.execute(permission_resource_stmt)).all())
            code_resource_stmt = select(SysResource.code, SysResource.id).where(
                SysResource.code.in_(permission_keys),
                SysResource.resource_type.in_(["BUTTON", "ACTION"]),
            )
            code_resource_rows = list((await self.db.execute(code_resource_stmt)).all())
            permission_resource_map: dict[str, set[str]] = defaultdict(set)
            for permission_key, resource_id in permission_resource_rows:
                permission_resource_map[str(permission_key)].add(str(resource_id))
            for permission_key, resource_id in code_resource_rows:
                permission_resource_map[str(permission_key)].add(str(resource_id))
            missing_permission_keys = [
                permission_key
                for permission_key in permission_keys
                if permission_key not in permission_resource_map
            ]
            if missing_permission_keys:
                raise NotFoundError("Permission resource not found")
            for permission_key in permission_keys:
                resource_ids.extend(permission_resource_map[permission_key])
        resource_ids = list(dict.fromkeys(resource_ids))
        await self.db.execute(
            delete(SysSubjectResourceGrantRel).where(
                SysSubjectResourceGrantRel.subject_type == subject_type.value,
                SysSubjectResourceGrantRel.subject_id == subject_id,
            )
        )
        for resource_id in resource_ids:
            self.db.add(
                SysSubjectResourceGrantRel(
                    subject_type=subject_type.value,
                    subject_id=subject_id,
                    resource_id=resource_id,
                    grant_mode=(
                        GrantMode.DIRECT.value
                        if resource_id in original_resource_ids
                        else GrantMode.CASCADE.value
                    ),
                    effect=GrantEffect.ALLOW.value,
                )
            )
        await self.db.flush()

    async def list_subject_permission_grants(
        self,
        subject_type: GrantSubjectType,
        subject_id: str,
    ) -> list[PermissionGrantRecord]:
        await self._ensure_subject_exists(subject_type.value, subject_id)
        stmt = (
            select(SysSubjectPermissionGrantRel)
            .where(
                SysSubjectPermissionGrantRel.subject_type == subject_type.value,
                SysSubjectPermissionGrantRel.subject_id == subject_id,
            )
            .order_by(SysSubjectPermissionGrantRel.id.asc())
        )
        grants = list((await self.db.execute(stmt)).scalars().all())
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

    async def replace_subject_permission_grants(
        self,
        subject_type: GrantSubjectType,
        subject_id: str,
        grant_info_list,
    ) -> None:
        await self._ensure_subject_exists(subject_type.value, subject_id)
        await self.db.execute(
            delete(SysSubjectPermissionGrantRel).where(
                SysSubjectPermissionGrantRel.subject_type == subject_type.value,
                SysSubjectPermissionGrantRel.subject_id == subject_id,
            )
        )
        for grant in grant_info_list:
            self.db.add(
                SysSubjectPermissionGrantRel(
                    subject_type=subject_type.value,
                    subject_id=subject_id,
                    permission_key=grant.permission_key,
                    data_scope=grant.data_scope.value,
                    custom_scope_dept_ids=list(grant.custom_scope_dept_ids),
                    effect=GrantEffect.ALLOW.value,
                )
            )
        await self.db.flush()

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

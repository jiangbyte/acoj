from collections import defaultdict
from typing import Literal, TypedDict

from sqlalchemy import Select, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import (
    AccountStatusEnum,
    DataScope,
    GrantEffect,
    GrantMode,
    GrantSubjectType,
    ResourceType,
    StatusEnum,
)
from app.core.exceptions.business import ConflictError, NotFoundError
from app.modules.iam.model import (
    SysAccount,
    SysAccountDeptRel,
    SysAccountGroupRel,
    SysAccountRoleRel,
    SysDept,
    SysGroup,
    SysGroupRoleRel,
    SysResource,
    SysResourcePermissionRel,
    SysRole,
    SysSubjectPermissionGrantRel,
    SysSubjectResourceGrantRel,
)
from app.modules.iam.schema import (
    AccountCreateRequest,
    AccountDeptAssignRequest,
    AccountGroupAssignRequest,
    AccountRoleAssignRequest,
    DeptCreateRequest,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    ResourceCreateRequest,
    ResourcePermissionBindRequest,
    RoleCreateRequest,
    SubjectPermissionGrantRequest,
    SubjectResourceGrantRequest,
)


class AccountResourceGrantRecord(TypedDict):
    """账户资源授权聚合记录。"""

    subject_type: GrantSubjectType | str
    subject_id: str
    resource_id: str
    grant_mode: GrantMode | str
    effect: GrantEffect | str


class PermissionGrantRecord(TypedDict):
    """账户有效权限聚合记录。"""

    permission_key: str
    data_scope: DataScope | str
    custom_scope_dept_ids: list[str]
    effect: GrantEffect | str
    source_type: GrantSubjectType | Literal["RESOURCE"] | str
    source_id: str


class DeptTreeRecord(TypedDict):
    """部门树节点结构。"""

    id: str
    name: str
    code: str
    category: str
    children: list["DeptTreeRecord"]


class ResourceTreeRecord(TypedDict):
    """资源树节点结构。"""

    id: str
    code: str
    name: str
    resource_type: ResourceType | str
    children: list["ResourceTreeRecord"]


class AccountRepository:
    """账户基础仓储，负责系统账户主表的直接查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_account_by_id(self, account_id: str) -> SysAccount | None:
        """按账户 ID 查询账户主表记录。"""
        return await self.db.get(SysAccount, account_id)


class IAMRepository:
    """IAM 仓储，负责账户、角色、资源、授权关系的显式读写和聚合。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_account_by_account(self, account: str) -> SysAccount | None:
        """按账号查询账户记录。"""
        stmt = select(SysAccount).where(SysAccount.account == account)
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def create_account(self, payload: AccountCreateRequest, password_hash: str) -> SysAccount:
        """创建账户主表记录，并处理账号唯一性校验。"""
        existing = await self.get_account_by_account(payload.account)
        if existing:
            raise ConflictError("Account already exists")
        data = payload.model_dump(exclude={"password"})
        account = SysAccount(
            account_status=AccountStatusEnum.ENABLED.value,
            password_hash=password_hash,
            **data,
        )
        self.db.add(account)
        await self.db.flush()
        return account

    async def list_accounts(self, offset: int, limit: int) -> tuple[list[SysAccount], int]:
        """分页查询账户列表。"""
        stmt: Select[tuple[SysAccount]] = (
            select(SysAccount)
            .order_by(SysAccount.id.desc())
            .offset(offset)
            .limit(limit)
        )
        count_stmt = select(func.count(SysAccount.id))
        accounts = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return accounts, total

    async def create_dept(self, payload: DeptCreateRequest) -> SysDept:
        """创建部门记录。"""
        dept = SysDept(**payload.model_dump())
        self.db.add(dept)
        await self.db.flush()
        return dept

    async def list_depts(self) -> list[SysDept]:
        """查询全部部门，供构造组织树。"""
        stmt = select(SysDept).order_by(SysDept.sort.asc(), SysDept.id.asc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def create_group(self, payload: GroupCreateRequest) -> SysGroup:
        """创建账户组。"""
        group = SysGroup(**payload.model_dump())
        self.db.add(group)
        await self.db.flush()
        return group

    async def list_groups(self) -> list[SysGroup]:
        """查询全部账户组。"""
        stmt = select(SysGroup).order_by(SysGroup.id.desc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def create_role(self, payload: RoleCreateRequest) -> SysRole:
        """创建角色。"""
        role = SysRole(**payload.model_dump())
        self.db.add(role)
        await self.db.flush()
        return role

    async def list_roles(self) -> list[SysRole]:
        """查询全部角色。"""
        stmt = select(SysRole).order_by(SysRole.sort.asc(), SysRole.id.asc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def create_resource(self, payload: ResourceCreateRequest) -> SysResource:
        """创建资源节点。"""
        resource = SysResource(**payload.model_dump())
        self.db.add(resource)
        await self.db.flush()
        return resource

    async def list_resources(self) -> list[SysResource]:
        """查询全部资源节点。"""
        stmt = select(SysResource).where(SysResource.status == StatusEnum.ENABLED.value).order_by(
            SysResource.sort.asc(),
            SysResource.id.asc(),
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def bind_resource_permission(
        self,
        payload: ResourcePermissionBindRequest,
    ) -> SysResourcePermissionRel:
        """为资源节点绑定权限项及其数据范围。"""
        if not await self.db.get(SysResource, payload.resource_id):
            raise NotFoundError("Resource not found")
        relation = SysResourcePermissionRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def list_resource_permissions(self) -> list[SysResourcePermissionRel]:
        """查询全部资源权限绑定关系。"""
        stmt = select(SysResourcePermissionRel).where(
            SysResourcePermissionRel.status == StatusEnum.ENABLED.value
        ).order_by(SysResourcePermissionRel.sort.asc(), SysResourcePermissionRel.id.asc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def grant_subject_resource(
        self,
        payload: SubjectResourceGrantRequest,
    ) -> SysSubjectResourceGrantRel:
        """为主体授予资源，资源会在后续聚合时展开为权限项。"""
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
        """为主体创建例外权限授权，主要用于账户或账户组补权与限权。"""
        await self._ensure_subject_exists(payload.subject_type.value, payload.subject_id)
        relation = SysSubjectPermissionGrantRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def assign_account_to_role(self, payload: AccountRoleAssignRequest) -> SysAccountRoleRel:
        """为账户分配角色。"""
        if not await self.db.get(SysAccount, payload.account_id):
            raise NotFoundError("Account not found")
        if not await self.db.get(SysRole, payload.role_id):
            raise NotFoundError("Role not found")
        relation = SysAccountRoleRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def assign_account_to_group(
        self,
        payload: AccountGroupAssignRequest,
    ) -> SysAccountGroupRel:
        """为账户分配账户组。"""
        if not await self.db.get(SysAccount, payload.account_id):
            raise NotFoundError("Account not found")
        if not await self.db.get(SysGroup, payload.group_id):
            raise NotFoundError("Group not found")
        relation = SysAccountGroupRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def assign_account_to_dept(self, payload: AccountDeptAssignRequest) -> SysAccountDeptRel:
        """为账户分配部门。"""
        if not await self.db.get(SysAccount, payload.account_id):
            raise NotFoundError("Account not found")
        if not await self.db.get(SysDept, payload.dept_id):
            raise NotFoundError("Dept not found")
        relation = SysAccountDeptRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def assign_group_to_role(self, payload: GroupRoleAssignRequest) -> SysGroupRoleRel:
        """为账户组分配角色。"""
        if not await self.db.get(SysGroup, payload.group_id):
            raise NotFoundError("Group not found")
        if not await self.db.get(SysRole, payload.role_id):
            raise NotFoundError("Role not found")
        relation = SysGroupRoleRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def get_account_role_ids(self, account_id: str) -> list[str]:
        """查询账户直接角色和账户组继承角色的去重合集。"""
        direct_stmt = select(SysAccountRoleRel.role_id).where(
            SysAccountRoleRel.account_id == account_id
        )
        group_stmt = (
            select(SysGroupRoleRel.role_id)
            .join(SysAccountGroupRel, SysAccountGroupRel.group_id == SysGroupRoleRel.group_id)
            .where(SysAccountGroupRel.account_id == account_id)
        )
        direct = [str(value) for value in (await self.db.execute(direct_stmt)).scalars().all()]
        group = [str(value) for value in (await self.db.execute(group_stmt)).scalars().all()]
        return sorted(set(direct + group))

    async def get_account_group_ids(self, account_id: str) -> list[str]:
        """查询账户所属账户组 ID 列表。"""
        stmt = select(SysAccountGroupRel.group_id).where(
            SysAccountGroupRel.account_id == account_id
        )
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def get_account_dept_ids(self, account_id: str) -> list[str]:
        """查询账户所属部门 ID 列表。"""
        stmt = select(SysAccountDeptRel.dept_id).where(SysAccountDeptRel.account_id == account_id)
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def get_account_resource_grants(
        self,
        account_id: str,
    ) -> list[AccountResourceGrantRecord]:
        """查询账户通过直授、组直授、角色直授、组角色授资源的所有资源授权。"""
        role_ids = await self.get_account_role_ids(account_id)
        group_ids = await self.get_account_group_ids(account_id)
        role_filter = SysSubjectResourceGrantRel.subject_id.in_(role_ids) if role_ids else False
        group_filter = SysSubjectResourceGrantRel.subject_id.in_(group_ids) if group_ids else False
        stmt = select(SysSubjectResourceGrantRel).where(
            SysSubjectResourceGrantRel.status == StatusEnum.ENABLED.value,
            or_(
                (
                    SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ACCOUNT.value
                )
                & (SysSubjectResourceGrantRel.subject_id == account_id),
                (
                    SysSubjectResourceGrantRel.subject_type == GrantSubjectType.GROUP.value
                )
                & group_filter,
                (
                    SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ROLE.value
                )
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
        """查询账户直授权限和账户组例外权限授权。"""
        group_ids = await self.get_account_group_ids(account_id)
        group_filter = (
            SysSubjectPermissionGrantRel.subject_id.in_(group_ids)
            if group_ids
            else False
        )
        stmt = select(SysSubjectPermissionGrantRel).where(
            SysSubjectPermissionGrantRel.status == StatusEnum.ENABLED.value,
            or_(
                (
                    SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ACCOUNT.value
                )
                & (SysSubjectPermissionGrantRel.subject_id == account_id),
                (
                    SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.GROUP.value
                )
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
        """汇总账户的最终权限项，资源授权为主，例外权限授权为辅。"""
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

    async def get_dept_tree(self) -> list[DeptTreeRecord]:
        """显式组装部门树，不依赖 ORM 关系。"""
        depts = await self.list_depts()
        node_map: dict[str, DeptTreeRecord] = {
            dept.id: {
                "id": dept.id,
                "name": dept.name,
                "code": dept.code,
                "category": dept.category,
                "children": [],
            }
            for dept in depts
        }
        roots: list[DeptTreeRecord] = []
        for dept in depts:
            if dept.parent_id and dept.parent_id in node_map:
                node_map[dept.parent_id]["children"].append(node_map[dept.id])
            else:
                roots.append(node_map[dept.id])
        return roots

    async def get_resource_tree(self) -> list[ResourceTreeRecord]:
        """显式组装资源树，支持后续资源授权界面和菜单树使用。"""
        resources = await self.list_resources()
        node_map: dict[str, ResourceTreeRecord] = {
            resource.id: {
                "id": resource.id,
                "code": resource.code,
                "name": resource.name,
                "resource_type": resource.resource_type,
                "children": [],
            }
            for resource in resources
        }
        roots: list[ResourceTreeRecord] = []
        for resource in resources:
            if resource.parent_id and resource.parent_id in node_map:
                node_map[resource.parent_id]["children"].append(node_map[resource.id])
            else:
                roots.append(node_map[resource.id])
        return roots

    async def _ensure_subject_exists(self, subject_type: str, subject_id: str) -> None:
        """显式校验授权主体存在性，替代数据库外键。"""
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

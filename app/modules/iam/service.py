import logging
from collections.abc import Mapping, Sequence
from typing import TypedDict

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import GrantSubjectType, ResourceType, UserType
from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import to_schema, to_schema_list
from app.core.security.password import hash_password
from app.core.security.permission_registry import (
    get_permission_definition,
    list_permission_definitions,
)
from app.modules.iam.repository import DeptTreeRecord, IAMRepository, ResourceTreeRecord
from app.modules.iam.schema import (
    AccountCreateRequest,
    AccountDeptAssignRequest,
    AccountGroupAssignRequest,
    AccountRoleAssignRequest,
    DeptCreateRequest,
    DeptTreeNode,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    ResourceCreateRequest,
    ResourcePermissionBindRequest,
    ResourceTreeNode,
    RoleCreateRequest,
    SubjectPermissionGrantRequest,
    SubjectResourceGrantRequest,
    SysAccountDeptRelSchema,
    SysAccountGroupRelSchema,
    SysAccountRoleRelSchema,
    SysAccountSchema,
    SysDeptSchema,
    SysGroupRoleRelSchema,
    SysGroupSchema,
    SysResourcePermissionRelSchema,
    SysResourceSchema,
    SysRoleSchema,
    SysSubjectPermissionGrantRelSchema,
    SysSubjectResourceGrantRelSchema,
)
from app.modules.user.admin.service import AdminUserProfileService
from app.modules.user.portal.service import PortalUserProfileService
from app.platform.db.transaction import transactional

logger = logging.getLogger(__name__)


class PermissionDefinition(TypedDict):
    """权限定义结构，来自 Redis 中的代码权限注册表。"""

    permission_key: str
    module: str
    source: str
    methods: list[str]
    login_scopes: list[str]
    routes: list[dict[str, object]]


class IAMService:
    """IAM 业务服务，围绕账户、资源和授权关系进行显式业务编排。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = IAMRepository(db)

    async def create_account(self, payload: AccountCreateRequest) -> SysAccountSchema:
        """创建系统账户并按账户体系自动补齐扩展资料。"""
        async with transactional(self.db):
            account = await self.repo.create_account(
                payload,
                password_hash=hash_password(payload.password),
            )
            if payload.account_type == UserType.ADMIN:
                await AdminUserProfileService(self.db).create_default_profile(account.id)
            elif payload.account_type == UserType.PORTAL:
                await PortalUserProfileService(self.db).create_default_profile(account.id)
            await self.db.refresh(account)
            return to_schema(SysAccountSchema, account)

    async def list_accounts(self, pagination: PageQuery) -> PageData[SysAccountSchema]:
        """分页查询账户列表。"""
        accounts, total = await self.repo.list_accounts(pagination.offset, pagination.size)
        return build_page(pagination, total, to_schema_list(SysAccountSchema, accounts))

    async def create_dept(self, payload: DeptCreateRequest) -> SysDeptSchema:
        """创建部门。"""
        async with transactional(self.db):
            return to_schema(SysDeptSchema, await self.repo.create_dept(payload))

    async def create_group(self, payload: GroupCreateRequest) -> SysGroupSchema:
        """创建账户组。"""
        async with transactional(self.db):
            return to_schema(SysGroupSchema, await self.repo.create_group(payload))

    async def create_role(self, payload: RoleCreateRequest) -> SysRoleSchema:
        """创建角色。"""
        async with transactional(self.db):
            return to_schema(SysRoleSchema, await self.repo.create_role(payload))

    async def create_resource(self, payload: ResourceCreateRequest) -> SysResourceSchema:
        """创建资源节点。"""
        async with transactional(self.db):
            return to_schema(SysResourceSchema, await self.repo.create_resource(payload))

    async def bind_resource_permission(
        self,
        payload: ResourcePermissionBindRequest,
    ) -> SysResourcePermissionRelSchema:
        """为资源节点绑定权限项和数据范围。"""
        await self._ensure_registered_permission(payload.permission_key)
        async with transactional(self.db):
            return to_schema(
                SysResourcePermissionRelSchema,
                await self.repo.bind_resource_permission(payload),
            )

    async def grant_subject_resource(
        self,
        payload: SubjectResourceGrantRequest,
    ) -> SysSubjectResourceGrantRelSchema:
        """为主体授予资源，是资源优先授权模型的主路径。"""
        async with transactional(self.db):
            return to_schema(
                SysSubjectResourceGrantRelSchema,
                await self.repo.grant_subject_resource(payload),
            )

    async def grant_subject_permission(
        self,
        payload: SubjectPermissionGrantRequest,
    ) -> SysSubjectPermissionGrantRelSchema:
        """为账户或账户组授予例外权限项，角色不允许走此例外路径。"""
        if payload.subject_type == GrantSubjectType.ROLE:
            raise ValueError("Role should grant resources instead of direct permission exceptions")
        await self._ensure_registered_permission(payload.permission_key)
        async with transactional(self.db):
            return to_schema(
                SysSubjectPermissionGrantRelSchema,
                await self.repo.grant_subject_permission(payload),
            )

    async def assign_account_role(
        self,
        payload: AccountRoleAssignRequest,
    ) -> SysAccountRoleRelSchema:
        """为账户分配角色。"""
        async with transactional(self.db):
            return to_schema(
                SysAccountRoleRelSchema,
                await self.repo.assign_account_to_role(payload),
            )

    async def assign_account_group(
        self,
        payload: AccountGroupAssignRequest,
    ) -> SysAccountGroupRelSchema:
        """为账户分配账户组。"""
        async with transactional(self.db):
            return to_schema(
                SysAccountGroupRelSchema,
                await self.repo.assign_account_to_group(payload),
            )

    async def assign_account_dept(
        self,
        payload: AccountDeptAssignRequest,
    ) -> SysAccountDeptRelSchema:
        """为账户分配部门。"""
        async with transactional(self.db):
            return to_schema(
                SysAccountDeptRelSchema,
                await self.repo.assign_account_to_dept(payload),
            )

    async def assign_group_role(self, payload: GroupRoleAssignRequest) -> SysGroupRoleRelSchema:
        """为账户组分配角色。"""
        async with transactional(self.db):
            return to_schema(SysGroupRoleRelSchema, await self.repo.assign_group_to_role(payload))

    async def list_groups(self):
        """查询账户组列表。"""
        return await self.repo.list_groups()

    async def list_roles(self):
        """查询角色列表。"""
        return await self.repo.list_roles()

    async def list_resource_tree(self) -> list[ResourceTreeNode]:
        """查询资源树。"""
        return _build_resource_tree_nodes(await self.repo.get_resource_tree())

    async def list_dept_tree(self) -> list[DeptTreeNode]:
        """查询部门树。"""
        return _build_dept_tree_nodes(await self.repo.get_dept_tree())

    async def resolve_permission_definitions(
        self,
        permission_keys: list[str],
    ) -> dict[str, PermissionDefinition]:
        """从 Redis 权限注册表解析权限定义。"""
        definitions: dict[str, PermissionDefinition] = {}
        for permission_key in permission_keys:
            item = await get_permission_definition(permission_key)
            if not item:
                logger.info(
                    "Permission definition missing from registry",
                    extra={"permission_key": permission_key},
                )
                continue
            definitions[permission_key] = {
                "permission_key": item.permission_key,
                "module": item.module,
                "source": item.source,
                "methods": list(item.methods),
                "login_scopes": list(item.login_scopes),
                "routes": [
                    {
                        "path": route_ref.path,
                        "methods": list(route_ref.methods),
                        "login_scopes": list(route_ref.login_scopes),
                    }
                    for route_ref in item.routes
                ],
            }
        return definitions

    async def list_permission_registry(self) -> list[PermissionDefinition]:
        items = await list_permission_definitions()
        return [
            {
                "permission_key": item.permission_key,
                "module": item.module,
                "source": item.source,
                "methods": list(item.methods),
                "login_scopes": list(item.login_scopes),
                "routes": [
                    {
                        "path": route_ref.path,
                        "methods": list(route_ref.methods),
                        "login_scopes": list(route_ref.login_scopes),
                    }
                    for route_ref in item.routes
                ],
            }
            for item in items
        ]

    async def _ensure_registered_permission(self, permission_key: str) -> None:
        item = await get_permission_definition(permission_key)
        if item is None:
            raise BusinessError(f"Permission is not registered in Redis: {permission_key}")


def _build_dept_tree_nodes(
    items: Sequence[DeptTreeRecord | DeptTreeNode | Mapping[str, object]],
) -> list[DeptTreeNode]:
    nodes: list[DeptTreeNode] = []
    for item in items:
        raw_item: Mapping[str, object] = (
            item.model_dump() if isinstance(item, DeptTreeNode) else item
        )
        nodes.append(
            DeptTreeNode(
                id=str(raw_item["id"]),
                name=str(raw_item["name"]),
                code=str(raw_item["code"]),
                category=str(raw_item["category"]),
                children=_build_dept_tree_nodes(raw_item.get("children", [])),  # type: ignore[arg-type]
            )
        )
    return nodes


def _build_resource_tree_nodes(
    items: Sequence[ResourceTreeRecord | ResourceTreeNode | Mapping[str, object]],
) -> list[ResourceTreeNode]:
    nodes: list[ResourceTreeNode] = []
    for item in items:
        raw_item: Mapping[str, object] = (
            item.model_dump() if isinstance(item, ResourceTreeNode) else item
        )
        nodes.append(
            ResourceTreeNode(
                id=str(raw_item["id"]),
                code=str(raw_item["code"]),
                name=str(raw_item["name"]),
                resource_type=ResourceType(str(raw_item["resource_type"])),
                children=_build_resource_tree_nodes(raw_item.get("children", [])),  # type: ignore[arg-type]
            )
        )
    return nodes

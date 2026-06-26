from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_permission, require_scope
from app.deps.db import get_db_session
from app.modules.iam.schema import (
    AccountCreateRequest,
    AccountDeptAssignRequest,
    AccountGroupAssignRequest,
    AccountRoleAssignRequest,
    DeptCreateRequest,
    DeptTreeNode,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    PermissionRegistryResponse,
    PermissionRegistryRouteResponse,
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
from app.modules.iam.service import IAMService

router = APIRouter()


@router.post(
    "/accounts",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:account:create")),
    ],
    response_model=ApiResponse[SysAccountSchema],
)
async def create_account(
    payload: AccountCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountSchema]:
    return success(await IAMService(db).create_account(payload))


@router.get(
    "/accounts",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:account:list")),
    ],
    response_model=ApiResponse[PageData[SysAccountSchema]],
)
async def list_accounts(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
) -> ApiResponse[PageData[SysAccountSchema]]:
    pagination = PageQuery(current=current, size=size)
    return success(await IAMService(db).list_accounts(pagination))


@router.post(
    "/depts",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:create")),
    ],
    response_model=ApiResponse[SysDeptSchema],
)
async def create_dept(
    payload: DeptCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysDeptSchema]:
    return success(await IAMService(db).create_dept(payload))


@router.get(
    "/depts/tree",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:list")),
    ],
    response_model=ApiResponse[list[DeptTreeNode]],
)
async def list_dept_tree(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[DeptTreeNode]]:
    return success(await IAMService(db).list_dept_tree())


@router.post(
    "/groups",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:group:create")),
    ],
    response_model=ApiResponse[SysGroupSchema],
)
async def create_group(
    payload: GroupCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysGroupSchema]:
    return success(await IAMService(db).create_group(payload))


@router.post(
    "/roles",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:role:create")),
    ],
    response_model=ApiResponse[SysRoleSchema],
)
async def create_role(
    payload: RoleCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysRoleSchema]:
    return success(await IAMService(db).create_role(payload))


@router.post(
    "/resources",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:resource:create")),
    ],
    response_model=ApiResponse[SysResourceSchema],
)
async def create_resource(
    payload: ResourceCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysResourceSchema]:
    return success(await IAMService(db).create_resource(payload))


@router.get(
    "/resources/tree",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:resource:list")),
    ],
    response_model=ApiResponse[list[ResourceTreeNode]],
)
async def list_resource_tree(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[ResourceTreeNode]]:
    return success(await IAMService(db).list_resource_tree())


@router.post(
    "/resource-permissions",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:resource:grant")),
    ],
    response_model=ApiResponse[SysResourcePermissionRelSchema],
)
async def bind_resource_permission(
    payload: ResourcePermissionBindRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysResourcePermissionRelSchema]:
    return success(await IAMService(db).bind_resource_permission(payload))


@router.post(
    "/resource-grants",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:grant:resource")),
    ],
    response_model=ApiResponse[SysSubjectResourceGrantRelSchema],
)
async def grant_subject_resource(
    payload: SubjectResourceGrantRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysSubjectResourceGrantRelSchema]:
    return success(await IAMService(db).grant_subject_resource(payload))


@router.post(
    "/permission-grants",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:grant:permission")),
    ],
    response_model=ApiResponse[SysSubjectPermissionGrantRelSchema],
)
async def grant_subject_permission(
    payload: SubjectPermissionGrantRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysSubjectPermissionGrantRelSchema]:
    return success(await IAMService(db).grant_subject_permission(payload))


@router.post(
    "/account-roles",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:account:grant-role")),
    ],
    response_model=ApiResponse[SysAccountRoleRelSchema],
)
async def assign_account_role(
    payload: AccountRoleAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountRoleRelSchema]:
    return success(await IAMService(db).assign_account_role(payload))


@router.post(
    "/account-groups",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:account:grant-group")),
    ],
    response_model=ApiResponse[SysAccountGroupRelSchema],
)
async def assign_account_group(
    payload: AccountGroupAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountGroupRelSchema]:
    return success(await IAMService(db).assign_account_group(payload))


@router.post(
    "/account-depts",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:account:grant-dept")),
    ],
    response_model=ApiResponse[SysAccountDeptRelSchema],
)
async def assign_account_dept(
    payload: AccountDeptAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountDeptRelSchema]:
    return success(await IAMService(db).assign_account_dept(payload))


@router.post(
    "/group-roles",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:group:grant-role")),
    ],
    response_model=ApiResponse[SysGroupRoleRelSchema],
)
async def assign_group_role(
    payload: GroupRoleAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysGroupRoleRelSchema]:
    return success(await IAMService(db).assign_group_role(payload))


@router.get(
    "/permissions/registry",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:permission:list")),
    ],
    response_model=ApiResponse[list[PermissionRegistryResponse]],
)
async def list_permission_registry(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[PermissionRegistryResponse]]:
    items = await IAMService(db).list_permission_registry()
    return success(
        [
            PermissionRegistryResponse(
                permission_key=item["permission_key"],
                module=item["module"],
                source=item["source"],
                methods=list(item["methods"]),
                login_scopes=list(item["login_scopes"]),
                routes=[
                    PermissionRegistryRouteResponse(
                        path=str(route_ref["path"]),
                        methods=list(route_ref["methods"]),
                        login_scopes=list(route_ref["login_scopes"]),
                    )
                    for route_ref in item["routes"]
                ],
            )
            for item in items
        ]
    )

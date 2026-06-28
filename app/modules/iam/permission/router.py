from fastapi import APIRouter, Depends

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_permission, require_account_type
from app.modules.iam.permission.schema import (
    PermissionRegistryResponse,
    PermissionRegistryRouteResponse,
)
from app.modules.iam.permission.service import PermissionService

router = APIRouter()


@router.get(
    "/permissions/registry",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:permission:list")),
    ],
    response_model=ApiResponse[list[PermissionRegistryResponse]],
)
async def list_permission_registry() -> ApiResponse[list[PermissionRegistryResponse]]:
    items = await PermissionService().list_permission_registry()
    return success(
        [
            PermissionRegistryResponse(
                permission_key=item["permission_key"],
                module=item["module"],
                source=item["source"],
                methods=list(item["methods"]),
                account_types=list(item["account_types"]),
                routes=[
                    PermissionRegistryRouteResponse(
                        path=str(route_ref["path"]),
                        methods=list(route_ref["methods"]),
                        account_types=list(route_ref["account_types"]),
                    )
                    for route_ref in item["routes"]
                ],
            )
            for item in items
        ]
    )

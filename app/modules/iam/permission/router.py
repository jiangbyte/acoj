from fastapi import APIRouter, Depends

from app.core.config.enums import LoginScope
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_permission, require_scope
from app.modules.iam.permission.schema import PermissionRegistryResponse, PermissionRegistryRouteResponse
from app.modules.iam.permission.service import PermissionService

router = APIRouter()


@router.get(
    "/permissions/registry",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("iam:permission:list")),
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

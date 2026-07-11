from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.schema import ApiResponse, success
from app.deps.db import get_db_session
from app.modules.iam.resource.schema import CurrentResourceModuleSchema
from app.modules.iam.resource.service import ResourceService

router = APIRouter()


@router.get(
    "/sys/resources/current",
    response_model=ApiResponse[list[CurrentResourceModuleSchema]],
)
async def current_resources(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[CurrentResourceModuleSchema]]:
    return success(await ResourceService(db).list_public_portal_resource_modules())

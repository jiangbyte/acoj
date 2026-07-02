from typing import Annotated

from fastapi import APIRouter, Depends
from fastapi.responses import Response
from sqlalchemy.ext.asyncio import AsyncSession

from app.deps.db import get_db_session
from app.modules.sys.file.service import FileService

router = APIRouter()


@router.get("/files/{object_name:path}", response_class=Response)
async def get_file(
    object_name: str,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> Response:
    return await FileService(db).response(object_name)

from typing import Annotated

from fastapi import APIRouter, Depends, File, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_permission, require_scope
from app.deps.db import get_db_session
from app.modules.file.schema import (
    FileDeleteResponse,
    FileUploadRequest,
    FileUrlRequest,
    FileUrlResponse,
    SysFileSchema,
)
from app.modules.file.service import FileService

router = APIRouter()


@router.post(
    "/upload",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("file:upload")),
    ],
    response_model=ApiResponse[SysFileSchema],
)
async def upload_file(
    file: Annotated[UploadFile, File(...)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysFileSchema]:
    content = await file.read()
    return success(
        await FileService(db).upload(
            FileUploadRequest(
                filename=file.filename or "file.bin",
                content=content,
                content_type=file.content_type or "application/octet-stream",
            )
        )
    )


@router.post(
    "/delete",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("file:delete")),
    ],
    response_model=ApiResponse[FileDeleteResponse],
)
async def delete_file(
    payload: FileUrlRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[FileDeleteResponse]:
    await FileService(db).delete(payload.object_name)
    return success(FileDeleteResponse(object_name=payload.object_name, deleted=True))


@router.post(
    "/url",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("file:url")),
    ],
    response_model=ApiResponse[FileUrlResponse],
)
async def get_file_url(
    payload: FileUrlRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[FileUrlResponse]:
    return success(
        FileUrlResponse(
            object_name=payload.object_name,
            url=await FileService(db).get_url(payload.object_name),
        )
    )


@router.post(
    "/presigned-url",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("file:url")),
    ],
    response_model=ApiResponse[FileUrlResponse],
)
async def get_file_presigned_url(payload: FileUrlRequest) -> ApiResponse[FileUrlResponse]:
    return success(
        FileUrlResponse(
            object_name=payload.object_name,
            url=FileService.__new__(FileService).storage.get_presigned_url(payload.object_name),
        )
    )


@router.get(
    "/list",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("file:list")),
    ],
    response_model=ApiResponse[PageData[SysFileSchema]],
)
async def list_files(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
) -> ApiResponse[PageData[SysFileSchema]]:
    pagination = PageQuery(current=current, size=size)
    return success(await FileService(db).list_files(pagination))

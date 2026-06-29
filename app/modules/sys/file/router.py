from typing import Annotated

from fastapi import APIRouter, Depends, File, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.sys.file.schema import (
    FileDeleteResponse,
    FileUploadRequest,
    FileUrlRequest,
    FileUrlResponse,
    SysFileSchema,
)
from app.modules.sys.file.service import FileService

router = APIRouter()


@router.post(
    "/sys/file/upload",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:upload")),
    ],
    response_model=ApiResponse[SysFileSchema],
)
async def upload(
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
    "/sys/file/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:delete")),
    ],
    response_model=ApiResponse[FileDeleteResponse],
)
async def delete(
    payload: FileUrlRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[FileDeleteResponse]:
    await FileService(db).delete(payload.object_name)
    return success(FileDeleteResponse(object_name=payload.object_name, deleted=True))


@router.post(
    "/sys/file/url",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:url")),
    ],
    response_model=ApiResponse[FileUrlResponse],
)
async def url(
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
    "/sys/file/presigned_url",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:presignedurl")),
    ],
    response_model=ApiResponse[FileUrlResponse],
)
async def presigned_url(payload: FileUrlRequest) -> ApiResponse[FileUrlResponse]:
    return success(
        FileUrlResponse(
            object_name=payload.object_name,
            url=FileService.__new__(FileService).storage.get_presigned_url(payload.object_name),
        )
    )


@router.get(
    "/sys/file/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:page")),
    ],
    response_model=ApiResponse[PageData[SysFileSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    current: Current = 1,
    size: Size = 20,
) -> ApiResponse[PageData[SysFileSchema]]:
    pagination = PageQuery(current=current, size=size)
    return success(await FileService(db).page(pagination, session))

from typing import Annotated

from fastapi import APIRouter, Depends, File, Query, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType, StorageProvider
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.sys.file.schema import (
    FileAdminPageQuery,
    FileUpdateRequest,
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
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await FileService(db).delete(payload)
    return success()


@router.post(
    "/sys/file/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: FileUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await FileService(db).update(payload)
    return success()


@router.get(
    "/sys/file/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:file:detail")),
    ],
    response_model=ApiResponse[SysFileSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysFileSchema]:
    return success(await FileService(db).detail(IdQuery(id=id)))


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
async def presigned_url(
    payload: FileUrlRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[FileUrlResponse]:
    return success(
        FileUrlResponse(
            object_name=payload.object_name,
            url=await FileService(db).get_presigned_url(payload.object_name),
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
    original_name: str | None = Query(default=None, max_length=255),
    object_name: str | None = Query(default=None, max_length=255),
    storage_provider: StorageProvider | None = Query(default=None),
    content_type: str | None = Query(default=None, max_length=128),
) -> ApiResponse[PageData[SysFileSchema]]:
    query = FileAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        original_name=original_name,
        object_name=object_name,
        storage_provider=storage_provider,
        content_type=content_type,
    )
    return success(await FileService(db).page(query, session))

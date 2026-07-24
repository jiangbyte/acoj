from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.core.schema.base import ApiSchema
from app.deps.auth import require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.sys.config.schema import (
    ConfigAdminPageQuery,
    ConfigBatchSaveRequest,
    ConfigCreateRequest,
    ConfigUpdateRequest,
    SysConfigSchema,
)
from app.modules.sys.config.service import ConfigService
from pydantic import Field


class TestWebhookRequest(ApiSchema):
    webhook_url: str = Field(default="", max_length=1024)
    webhook_secret: str = Field(default="", max_length=256)

router = APIRouter()


@router.post(
    "/sys/config/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: ConfigCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ConfigService(db).create(payload)
    return success()


@router.post(
    "/sys/config/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: ConfigUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ConfigService(db).update(payload)
    return success()


@router.post(
    "/sys/config/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ConfigService(db).delete(payload)
    return success()


@router.get(
    "/sys/config/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:detail")),
    ],
    response_model=ApiResponse[SysConfigSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysConfigSchema]:
    return success(await ConfigService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/config/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:page")),
    ],
    response_model=ApiResponse[PageData[SysConfigSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    config_key: str | None = Query(default=None, max_length=255),
    category: str | None = Query(default=None, max_length=255),
) -> ApiResponse[PageData[SysConfigSchema]]:
    query = ConfigAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        config_key=config_key,
        category=category,
    )
    return success(await ConfigService(db).page_admin(query))


@router.get(
    "/sys/config/list",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:page")),
    ],
    response_model=ApiResponse[list[SysConfigSchema]],
)
async def list_config(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    category: str | None = Query(default=None, max_length=255),
) -> ApiResponse[list[SysConfigSchema]]:
    return success(await ConfigService(db).list_by_category(category))


@router.post(
    "/sys/config/batch-save",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:config:update")),
    ],
    response_model=ApiResponse[None],
)
async def batch_save(
    payload: ConfigBatchSaveRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ConfigService(db).batch_save(payload)
    return success()


@router.post(
    "/sys/config/audit-alert/test-webhook",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def test_audit_alert_webhook(
    payload: TestWebhookRequest,
) -> ApiResponse[dict]:
    from app.modules.sys.audit.alert import send_test_webhook

    err = await send_test_webhook(payload.webhook_url, payload.webhook_secret)
    if err:
        from app.core.exceptions.business import BusinessError

        raise BusinessError(err)
    return success({"message": "测试消息已发送"})

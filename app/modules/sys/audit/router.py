from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery
from app.deps.auth import require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.sys.audit.schema import OperationAuditPageQuery, OperationAuditRecord
from app.modules.sys.audit.service import OperationAuditService

router = APIRouter()


@router.get(
    "/sys/audit/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:audit:page")),
    ],
    response_model=ApiResponse[PageData[OperationAuditRecord]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    module: str | None = Query(default=None, max_length=64),
    action: str | None = Query(default=None, max_length=64),
    account_id: str | None = Query(default=None, max_length=64),
    success_filter: bool | None = Query(default=None, alias="success"),
) -> ApiResponse[PageData[OperationAuditRecord]]:
    return success(
        await OperationAuditService(db).page_admin(
            OperationAuditPageQuery(
                pagination=PageQuery(current=current, size=size),
                module=module,
                action=action,
                account_id=account_id,
                success=success_filter,
            )
        )
    )


@router.get(
    "/sys/audit/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("sys:audit:detail")),
    ],
    response_model=ApiResponse[OperationAuditRecord],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OperationAuditRecord]:
    return success(await OperationAuditService(db).detail(IdQuery(id=id)))

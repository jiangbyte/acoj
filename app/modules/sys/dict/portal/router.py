from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_account_type
from app.deps.db import get_db_session
from app.modules.sys.dict.schema import DictTreeQuery, SysDictTreeNode
from app.modules.sys.dict.service import DictService

router = APIRouter()


@router.get(
    "/sys/dicts/tree",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[SysDictTreeNode]],
)
async def tree(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    category: str | None = Query(default=None, max_length=64),
) -> ApiResponse[list[SysDictTreeNode]]:
    return success(await DictService(db).list_tree(DictTreeQuery(category=category)))

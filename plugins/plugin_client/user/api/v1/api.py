"""
Client user API — uses Perm() and standalone service functions.
Mirrors hei-gin plugins/plugin-client/user/api/v1/api.go
"""

from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from sdk.web.result import Result, PageData, success
from sdk.shared.types import IdParam, IdsParam
from sdk.infra.db import get_db
from sdk.kernel.plugin import Perm
from sdk.auth.decorator import HeiClientCheckLogin, NoRepeat
from sdk.log import SysLog
from ...params import (
    ClientUserVO, ClientUserPageParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from ...service import (
    client_user_page, client_user_detail, client_user_create,
    client_user_modify, client_user_remove,
    client_user_get_current, client_user_update_profile,
    client_user_update_avatar, client_user_update_password,
)

router = APIRouter()


# ── Admin routes (管理端) ──

@router.get("/api/v1/client-user/page", summary="获取C端用户分页",
            response_model=Result[PageData[ClientUserVO]])
@Perm("client:user:page", "C端用户分页")
async def page(request: Request, param: ClientUserPageParam = Depends(),
               db: Session = Depends(get_db)):
    return success(client_user_page(db, param))


@router.post("/api/v1/client-user/create", summary="添加C端用户", response_model=Result)
@Perm("client:user:create", "添加C端用户")
async def create(request: Request, vo: ClientUserVO, db: Session = Depends(get_db)):
    await client_user_create(db, vo, request)
    return success()


@router.post("/api/v1/client-user/modify", summary="编辑C端用户", response_model=Result)
@Perm("client:user:modify", "编辑C端用户")
async def modify(request: Request, vo: ClientUserVO, db: Session = Depends(get_db)):
    await client_user_modify(db, vo, request)
    return success()


@router.post("/api/v1/client-user/remove", summary="删除C端用户", response_model=Result)
@Perm("client:user:remove", "删除C端用户")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    client_user_remove(db, param)
    return success()


@router.get("/api/v1/client-user/detail", summary="获取C端用户详情",
            response_model=Result[ClientUserVO])
@Perm("client:user:detail", "C端用户详情")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    data = client_user_detail(db, id)
    return success(data)


# ── Self-service routes (C端) ──

@router.get("/api/v1/c/client-user/current", summary="获取当前C端用户信息")
@HeiClientCheckLogin
async def get_current_user(request: Request, db: Session = Depends(get_db)):
    data = await client_user_get_current(db, request)
    return success(data)


@router.post("/api/v1/c/client-user/update-profile", summary="更新当前C端用户个人信息",
             response_model=Result)
@SysLog("C端用户更新个人信息")
@HeiClientCheckLogin
@NoRepeat(interval=3000)
async def update_profile(request: Request, param: UpdateProfileParam,
                          db: Session = Depends(get_db)):
    await client_user_update_profile(db, param, request)
    return success()


@router.post("/api/v1/c/client-user/update-avatar", summary="更新当前C端用户头像（base64）",
             response_model=Result)
@SysLog("C端用户更新头像")
@HeiClientCheckLogin
async def update_avatar(request: Request, param: UpdateAvatarParam,
                         db: Session = Depends(get_db)):
    await client_user_update_avatar(db, param, request)
    return success()


@router.post("/api/v1/c/client-user/update-password", summary="修改当前C端用户密码",
             response_model=Result)
@SysLog("C端用户修改密码")
@HeiClientCheckLogin
@NoRepeat(interval=3000)
async def update_password(request: Request, param: UpdatePasswordParam,
                           db: Session = Depends(get_db)):
    await client_user_update_password(db, param, request)
    return success()

"""Client user service — class-based service with DI-friendly provider."""

import asyncio
from datetime import datetime
from typing import Optional

import bcrypt
from fastapi import Depends, Request
from sqlalchemy import update as sa_update
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.shared.contracts import LoginUserInfo
from sdk.shared.di import ActorContext
from sdk.utils import compress_base64_image, decrypt, generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import ClientUser
from .params import (
    ClientUserPageParam,
    ClientUserVO,
    LoginUserInfoVO,
    UpdateAvatarParam,
    UpdatePasswordParam,
    UpdateProfileParam,
)
from .repository import ClientUserRepository


def _actor_user_id(actor: Optional[ActorContext]) -> str:
    return actor.user_id if actor else ""


def _build_client_user(vo: ClientUserVO, now: datetime, actor_user_id: str) -> ClientUser:
    return ClientUser(
        id=generate_id(),
        username=vo.username,
        nickname=vo.nickname,
        avatar=vo.avatar,
        motto=vo.motto,
        gender=vo.gender,
        email=vo.email,
        github=vo.github,
        phone=vo.phone,
        status="ACTIVE",
        login_count=0,
        password=vo.password,
        created_at=now,
        updated_at=now,
        created_by=actor_user_id or None,
        updated_by=actor_user_id or None,
    )


class ClientUserService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, ClientUserRepository):
            self.repository = repository_or_db
        else:
            self.repository = ClientUserRepository(repository_or_db)
        self.db = self.repository.db

    async def page(self, param: ClientUserPageParam) -> dict:
        return map_page_data(
            await self.repository.find_page_by_filters(param),
            ClientUserVO.model_validate,
            param.current,
            param.size,
        )

    async def detail(self, id: str) -> Optional[ClientUserVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return ClientUserVO.model_validate(entity)

    async def create(self, vo: ClientUserVO, actor: Optional[ActorContext] = None) -> None:
        if vo.username and await self.repository.find_by_username(vo.username):
            raise BusinessException("帐号已存在")
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = _build_client_user(vo, now, actor_user_id)
        if vo.password:
            entity.password = await asyncio.to_thread(
                lambda: bcrypt.hashpw(vo.password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
            )
        await self.repository.insert(entity)

    async def modify(self, vo: ClientUserVO, actor: Optional[ActorContext] = None) -> None:
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        actor_user_id = _actor_user_id(actor)
        updates: dict = {"updated_at": datetime.now()}
        if vo.nickname is not None:
            updates["nickname"] = vo.nickname
        if vo.avatar is not None:
            updates["avatar"] = vo.avatar
        if vo.email is not None:
            updates["email"] = vo.email
        if vo.phone is not None:
            updates["phone"] = vo.phone
        if vo.status:
            updates["status"] = vo.status
        if actor_user_id:
            updates["updated_by"] = actor_user_id
        await self.db.execute(sa_update(ClientUser).where(ClientUser.id == vo.id).values(**updates))
        await self.db.commit()

    async def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        await self.repository.delete_by_ids(ids)

    async def record_login(self, user_id: str, request: Request) -> None:
        entity = await self.repository.find_by_id(user_id)
        if not entity:
            return
        entity.last_login_at = datetime.now()
        entity.last_login_ip = request.client.host if request.client else None
        entity.login_count = (entity.login_count or 0) + 1
        await self.repository.update(entity)

    async def get_current_user(self, actor: Optional[ActorContext] = None) -> Optional[ClientUserVO]:
        user_id = _actor_user_id(actor)
        if not user_id:
            return None
        entity = await self.repository.find_by_id(user_id)
        if not entity:
            return None
        return ClientUserVO.model_validate(entity)

    async def update_profile(self, param: UpdateProfileParam, actor: Optional[ActorContext] = None) -> None:
        user_id = _actor_user_id(actor)
        if not user_id:
            raise BusinessException("用户未登录")
        entity = await self.repository.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        if param.username:
            existing = await self.repository.find_by_username(param.username)
            if existing and existing.id != user_id:
                raise BusinessException("用户名已存在")
        updates: dict = {"updated_at": datetime.now(), "updated_by": user_id}
        if param.nickname is not None:
            updates["nickname"] = param.nickname
        if param.username is not None:
            updates["username"] = param.username
        if param.avatar is not None:
            updates["avatar"] = param.avatar
        if param.email is not None:
            updates["email"] = param.email
        if param.phone is not None:
            updates["phone"] = param.phone
        await self.db.execute(sa_update(ClientUser).where(ClientUser.id == user_id).values(**updates))
        await self.db.commit()

    async def update_avatar(self, param: UpdateAvatarParam, actor: Optional[ActorContext] = None) -> None:
        user_id = _actor_user_id(actor)
        if not user_id:
            raise BusinessException("用户未登录")
        if not param.avatar:
            raise BusinessException("头像不能为空")
        entity = await self.repository.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        entity.avatar = await asyncio.to_thread(compress_base64_image, param.avatar, 512, 512, 80)
        await self.repository.update(entity, user_id=user_id)

    async def update_password(self, param: UpdatePasswordParam, actor: Optional[ActorContext] = None) -> None:
        user_id = _actor_user_id(actor)
        if not user_id:
            raise BusinessException("用户未登录")
        entity = await self.repository.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        if not entity.password:
            raise BusinessException("未设置密码，无法修改")
        current_password = decrypt(param.current_password)
        if not await asyncio.to_thread(
            bcrypt.checkpw,
            current_password.encode("utf-8"),
            entity.password.encode("utf-8"),
        ):
            raise BusinessException("当前密码不正确")
        new_password = decrypt(param.new_password)
        entity.password = await asyncio.to_thread(
            lambda: bcrypt.hashpw(new_password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
        )
        await self.repository.update(entity, user_id=user_id)


def get_client_user_service(db: AsyncSession = Depends(get_db)) -> ClientUserService:
    return ClientUserService(ClientUserRepository(db))


class LoginUserService:
    def __init__(self, session_factory):
        self._session_factory = session_factory

    async def get_current_user(self, request) -> Optional[LoginUserInfo]:
        user_id = str(getattr(request.state, "micos_login_id", "") or "")
        if not user_id:
            return None
        return await self.get_user_by_id(user_id)

    async def get_user_by_id(self, user_id: str) -> Optional[LoginUserInfo]:
        async with self._session_factory() as db:
            entity = await ClientUserRepository(db).find_by_id(user_id)
            return LoginUserInfoVO.from_entity(entity)

    async def get_user_by_username(self, username: str) -> Optional[LoginUserInfo]:
        async with self._session_factory() as db:
            entity = await ClientUserRepository(db).find_by_username(username)
            return LoginUserInfoVO.from_entity(entity)

    async def get_user_by_email(self, email: str) -> Optional[LoginUserInfo]:
        async with self._session_factory() as db:
            entity = await ClientUserRepository(db).find_by_email(email)
            return LoginUserInfoVO.from_entity(entity)

    async def record_login(self, user_id: str, request: Request) -> None:
        async with self._session_factory() as db:
            await ClientUserService(ClientUserRepository(db)).record_login(user_id, request)

    async def get_username(self, user_id: str) -> Optional[str]:
        async with self._session_factory() as db:
            entity = await ClientUserRepository(db).find_by_id(user_id)
            if not entity:
                return None
            return entity.username

    async def create_user(self, username: str, hashed_password: str) -> str:
        async with self._session_factory() as db:
            try:
                existing = await ClientUserRepository(db).find_by_username(username)
                if existing:
                    raise BusinessException("用户名已存在")

                user_id = str(generate_id())
                now = datetime.now()
                user = ClientUser(
                    id=user_id,
                    username=username,
                    password=hashed_password,
                    nickname=username,
                    status="ACTIVE",
                    created_at=now,
                    updated_at=now,
                    created_by=user_id,
                    updated_by=user_id,
                )
                db.add(user)
                await db.commit()
                return user_id
            except Exception:
                await db.rollback()
                raise

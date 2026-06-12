"""Client user service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

import bcrypt
from fastapi import Depends, Request
from sqlalchemy import update as sa_update
from sqlalchemy.orm import Session

from sdk.auth import HeiClientAuthTool
from sdk.infra.db import get_db
from sdk.shared.contracts import LoginUserInfo
from sdk.shared.di import ActorContext, get_current_client_actor
from sdk.utils import compress_base64_image, decrypt, generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data

from .models import ClientUser
from .params import (
    ClientUserPageParam,
    ClientUserToClientUserVO,
    ClientUserToLoginUserInfo,
    ClientUserVO,
    ClientUserVOToClientUser,
    UpdateAvatarParam,
    UpdatePasswordParam,
    UpdateProfileParam,
)
from .repository import ClientUserRepository


class ClientUserService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, ClientUserRepository):
            self.repository = repository_or_db
        else:
            self.repository = ClientUserRepository(repository_or_db)
        self.db = self.repository.db

    @classmethod
    def from_db(cls, db: Session) -> "ClientUserService":
        return cls(ClientUserRepository(db))

    def find_by_id(self, user_id: str) -> Optional[ClientUser]:
        return self.repository.find_by_id(user_id)

    def find_by_username(self, username: str) -> Optional[ClientUser]:
        return self.repository.find_by_username(username)

    def find_by_email(self, email: str) -> Optional[ClientUser]:
        return self.repository.find_by_email(email)

    def page(self, param: ClientUserPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = [ClientUserToClientUserVO(row) for row in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return ClientUserToClientUserVO(entity)

    def create(self, vo: ClientUserVO, actor: Optional[ActorContext] = None) -> None:
        if vo.username and self.find_by_username(vo.username):
            raise BusinessException("帐号已存在")
        now = datetime.now()
        entity = ClientUserVOToClientUser(vo)
        entity.id = generate_id()
        entity.status = "ACTIVE"
        entity.login_count = 0
        entity.created_at = now
        entity.updated_at = now
        if vo.username:
            entity.username = vo.username
        if vo.password:
            entity.password = bcrypt.hashpw(vo.password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: ClientUserVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
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
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id
        self.db.execute(sa_update(ClientUser).where(ClientUser.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.delete_by_ids(ids)

    def record_login(self, user_id: str, request: Request) -> None:
        entity = self.repository.find_by_id(user_id)
        if not entity:
            return
        entity.last_login_at = datetime.now()
        entity.last_login_ip = request.client.host if request.client else None
        entity.login_count = (entity.login_count or 0) + 1
        self.repository.update(entity)

    def to_login_user_info(self, entity: Optional[ClientUser]) -> Optional[LoginUserInfo]:
        return ClientUserToLoginUserInfo(entity)

    def get_current_user(self, actor: Optional[ActorContext] = None) -> Optional[dict]:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return None
        entity = self.find_by_id(user_id)
        if not entity:
            return None
        return ClientUserToClientUserVO(entity)

    def update_profile(self, param: UpdateProfileParam, actor: Optional[ActorContext] = None) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            raise BusinessException("用户未登录")
        entity = self.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        if param.username:
            existing = self.find_by_username(param.username)
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
        self.db.execute(sa_update(ClientUser).where(ClientUser.id == user_id).values(**updates))
        self.db.commit()

    def update_avatar(self, param: UpdateAvatarParam, actor: Optional[ActorContext] = None) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            raise BusinessException("用户未登录")
        if not param.avatar:
            raise BusinessException("头像不能为空")
        entity = self.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        entity.avatar = compress_base64_image(param.avatar, 512, 512, 80)
        self.repository.update(entity, user_id=user_id)

    def update_password(self, param: UpdatePasswordParam, actor: Optional[ActorContext] = None) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            raise BusinessException("用户未登录")
        entity = self.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        if not entity.password:
            raise BusinessException("未设置密码，无法修改")
        current_password = decrypt(param.current_password)
        if not bcrypt.checkpw(current_password.encode("utf-8"), entity.password.encode("utf-8")):
            raise BusinessException("当前密码不正确")
        new_password = decrypt(param.new_password)
        entity.password = bcrypt.hashpw(new_password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
        self.repository.update(entity, user_id=user_id)


def get_client_user_service(db: Session = Depends(get_db)) -> ClientUserService:
    return ClientUserService.from_db(db)


class LoginUserApiProvider:
    def __init__(self, session_factory):
        self._session_factory = session_factory

    async def get_current_login_user_info(self, request) -> Optional[LoginUserInfo]:
        user_id = await HeiClientAuthTool.getLoginIdAsString(request)
        if not user_id:
            return None
        return self.get_login_user_info_by_id(user_id)

    def get_login_user_info_by_id(self, user_id: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = ClientUserRepository(db).find_by_id(user_id)
            return ClientUserToLoginUserInfo(entity)
        finally:
            db.close()

    def get_login_user_info_by_username(self, username: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = ClientUserRepository(db).find_by_username(username)
            return ClientUserToLoginUserInfo(entity)
        finally:
            db.close()

    def get_login_user_info_by_email(self, email: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = ClientUserRepository(db).find_by_email(email)
            return ClientUserToLoginUserInfo(entity)
        finally:
            db.close()

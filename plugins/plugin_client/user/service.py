"""Client user service — aligned with hei-gin."""

from typing import Optional, Dict
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update
from fastapi import Request
from .params import (
    ClientUserVO, ClientUserPageParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from .dao import ClientUserDao
from .models import ClientUser
from core.utils import decrypt, compress_base64_image
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.auth import HeiClientAuthTool, LoginUserInfo
from core.utils import generate_id
import bcrypt


def _to_vo(entity: ClientUser) -> dict:
    """Mirrors hei-gin ClientUserVO + toVO()."""
    vo = {
        "id": entity.id,
        "username": entity.username,
        "nickname": entity.nickname,
        "avatar": entity.avatar,
        "motto": entity.motto,
        "gender": entity.gender,
        "email": entity.email,
        "github": entity.github,
        "phone": entity.phone,
        "status": entity.status,
        "last_login_ip": entity.last_login_ip,
        "login_count": entity.login_count or 0,
    }
    if entity.last_login_at is not None:
        vo["last_login_at"] = entity.last_login_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_at is not None:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_at is not None:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    return vo


def find_by_id(db: Session, user_id: str) -> Optional[ClientUser]:
    return ClientUserDao(db).find_by_id(user_id)


def find_by_username(db: Session, username: str) -> Optional[ClientUser]:
    return ClientUserDao(db).find_by_username(username)


def find_by_email(db: Session, email: str) -> Optional[ClientUser]:
    return ClientUserDao(db).find_by_email(email)


def to_login_user_info(entity: Optional[ClientUser]) -> Optional[LoginUserInfo]:
    if not entity:
        return None
    return LoginUserInfo(
        id=entity.id, username=entity.username, password=entity.password,
        nickname=entity.nickname, avatar=entity.avatar, motto=entity.motto,
        gender=entity.gender, birthday=entity.birthday, email=entity.email,
        status=entity.status, last_login_at=entity.last_login_at,
        last_login_ip=entity.last_login_ip, login_count=entity.login_count,
    )


def record_login(db: Session, user_id: str, request: Request) -> None:
    dao = ClientUserDao(db)
    entity = dao.find_by_id(user_id)
    if not entity:
        return
    entity.last_login_at = datetime.now()
    entity.last_login_ip = request.client.host if request.client else None
    entity.login_count = (entity.login_count or 0) + 1
    dao.update(entity)


def page(db: Session, param: ClientUserPageParam) -> dict:
    dao = ClientUserDao(db)
    result = dao.find_page_by_filters(param)
    records = [_to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL],
                     page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = ClientUserDao(db).find_by_id(id)
    if not entity:
        return None
    return _to_vo(entity)


def create(db: Session, vo: ClientUserVO, user_id: Optional[str] = None) -> None:
    """Mirrors hei-gin ClientUserCreate — bcrypt, duplicate check."""
    now = datetime.now()

    # duplicate username check
    if vo.username:
        existing = find_by_username(db, vo.username)
        if existing:
            raise BusinessException("帐号已存在")

    entity = ClientUser(
        id=generate_id(),
        status="ACTIVE",
        login_count=0,
        created_at=now,
        updated_at=now,
    )
    if vo.username:
        entity.username = vo.username
    if vo.password:
        hashed = bcrypt.hashpw(vo.password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
        entity.password = hashed
    if vo.nickname:
        entity.nickname = vo.nickname
    if vo.avatar:
        entity.avatar = vo.avatar
    if vo.email:
        entity.email = vo.email
    if vo.phone:
        entity.phone = vo.phone
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id

    ClientUserDao(db).insert(entity)


def modify(db: Session, vo: ClientUserVO, user_id: Optional[str] = None) -> None:
    """Mirrors hei-gin ClientUserModify — updates map: nickname, avatar, email, phone, status."""
    dao = ClientUserDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")

    now = datetime.now()
    up: dict = {"updated_at": now}
    if vo.nickname is not None:
        up["nickname"] = vo.nickname
    if vo.avatar is not None:
        up["avatar"] = vo.avatar
    if vo.email is not None:
        up["email"] = vo.email
    if vo.phone is not None:
        up["phone"] = vo.phone
    if vo.status:
        up["status"] = vo.status
    if user_id:
        up["updated_by"] = user_id

    dao.db.execute(sa_update(ClientUser).where(ClientUser.id == vo.id).values(**up))
    dao.db.commit()


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    ClientUserDao(db).delete_by_ids(ids)


# ── Backward-compatible class ──

class ClientUserService:
    def __init__(self, db: Session):
        self.db = db
        self.dao = ClientUserDao(db)

    @property
    def _auth_tool(self):
        return HeiClientAuthTool

    def find_by_id(self, user_id: str) -> Optional[ClientUser]:
        return find_by_id(self.db, user_id)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await self._auth_tool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def detail(self, id: str):
        return detail(self.db, id)

    def page(self, param: ClientUserPageParam) -> dict:
        return page(self.db, param)

    def find_by_email(self, email: str) -> Optional[ClientUser]:
        return find_by_email(self.db, email)

    def find_by_username(self, username: str) -> Optional[ClientUser]:
        return find_by_username(self.db, username)

    async def create(self, vo: ClientUserVO, request: Optional[Request] = None) -> None:
        if vo.username and self.find_by_username(vo.username):
            raise BusinessException("帐号已存在")
        return create(self.db, vo, await self._get_current_user_id(request))

    async def modify(self, vo: ClientUserVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_current_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def to_login_user_info(self, entity: Optional[ClientUser]) -> Optional[LoginUserInfo]:
        return to_login_user_info(entity)

    def record_login(self, user_id: str, request: Request) -> None:
        return record_login(self.db, user_id, request)

    async def get_current_user(self, request: Request) -> Optional[Dict]:
        user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            return None
        entity = self.find_by_id(user_id)
        if not entity:
            return None
        return _to_vo(entity)

    async def update_profile(self, param: UpdateProfileParam, request: Request) -> None:
        """Mirrors hei-gin UpdateProfile — nickname, avatar, email, phone, username."""
        user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            raise BusinessException("用户未登录")
        entity = self.dao.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")

        # duplicate username check (exclude self)
        if param.username:
            existing = self.find_by_username(param.username)
            if existing and existing.id != user_id:
                raise BusinessException("用户名已存在")

        now = datetime.now()
        up: dict = {"updated_at": now}
        if param.nickname is not None:
            up["nickname"] = param.nickname
        if param.username is not None:
            up["username"] = param.username
        if param.avatar is not None:
            up["avatar"] = param.avatar
        if param.email is not None:
            up["email"] = param.email
        if param.phone is not None:
            up["phone"] = param.phone
        up["updated_by"] = user_id
        self.db.execute(sa_update(ClientUser).where(ClientUser.id == user_id).values(**up))
        self.db.commit()

    async def update_avatar(self, param: UpdateAvatarParam, request: Request) -> None:
        """Mirrors hei-gin UpdateAvatar — compress base64 512×512 80%."""
        user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            raise BusinessException("用户未登录")
        if not param.avatar:
            raise BusinessException("头像不能为空")
        entity = self.dao.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")

        compressed = compress_base64_image(param.avatar, 512, 512, 80)
        entity.avatar = compressed
        self.dao.update(entity, user_id=user_id)

    async def update_password(self, param: UpdatePasswordParam, request: Request) -> None:
        """Mirrors hei-gin UpdatePassword — verify current via decrypt, hash new."""
        user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            raise BusinessException("用户未登录")
        entity = self.dao.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在")
        if not entity.password:
            raise BusinessException("未设置密码，无法修改")
        current_password = decrypt(param.current_password)
        if not bcrypt.checkpw(current_password.encode('utf-8'), entity.password.encode('utf-8')):
            raise BusinessException("当前密码不正确")
        new_password = decrypt(param.new_password)
        hashed = bcrypt.hashpw(new_password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
        entity.password = hashed
        self.dao.update(entity, user_id=user_id)


# ── Standalone function aliases for API compatibility ──

def client_user_page(db: Session, param: ClientUserPageParam) -> dict:
    return page(db, param)


def client_user_detail(db: Session, id: str) -> Optional[dict]:
    return detail(db, id)


async def client_user_create(db: Session, vo: ClientUserVO, request: Request) -> None:
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    return create(db, vo, user_id)


async def client_user_modify(db: Session, vo: ClientUserVO, request: Request) -> None:
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    return modify(db, vo, user_id)


def client_user_remove(db: Session, param: IdsParam) -> None:
    return remove(db, param.ids)


async def client_user_get_current(db: Session, request: Request) -> Optional[Dict]:
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return None
    entity = find_by_id(db, user_id)
    if not entity:
        return None
    return _to_vo(entity)


async def client_user_update_profile(db: Session, param: UpdateProfileParam,
                                     request: Request) -> None:
    """Mirrors hei-gin UpdateProfile."""
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        raise BusinessException("用户未登录")
    entity = find_by_id(db, user_id)
    if not entity:
        raise BusinessException("用户不存在")

    # duplicate username check (exclude self)
    if param.username:
        existing = find_by_username(db, param.username)
        if existing and existing.id != user_id:
            raise BusinessException("用户名已存在")

    now = datetime.now()
    up: dict = {"updated_at": now}
    if param.nickname is not None:
        up["nickname"] = param.nickname
    if param.username is not None:
        up["username"] = param.username
    if param.avatar is not None:
        up["avatar"] = param.avatar
    if param.email is not None:
        up["email"] = param.email
    if param.phone is not None:
        up["phone"] = param.phone
    up["updated_by"] = user_id
    db.execute(sa_update(ClientUser).where(ClientUser.id == user_id).values(**up))
    db.commit()


async def client_user_update_avatar(db: Session, param: UpdateAvatarParam,
                                    request: Request) -> None:
    """Mirrors hei-gin UpdateAvatar — compress base64 512×512 80%."""
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        raise BusinessException("用户未登录")
    if not param.avatar:
        raise BusinessException("头像不能为空")
    entity = find_by_id(db, user_id)
    if not entity:
        raise BusinessException("用户不存在")

    compressed = compress_base64_image(param.avatar, 512, 512, 80)
    entity.avatar = compressed
    ClientUserDao(db).update(entity, user_id=user_id)


async def client_user_update_password(db: Session, param: UpdatePasswordParam,
                                      request: Request) -> None:
    """Mirrors hei-gin UpdatePassword."""
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        raise BusinessException("用户未登录")
    entity = find_by_id(db, user_id)
    if not entity:
        raise BusinessException("用户不存在")
    if not entity.password:
        raise BusinessException("未设置密码，无法修改")
    current_password = decrypt(param.current_password)
    if not bcrypt.checkpw(current_password.encode('utf-8'), entity.password.encode('utf-8')):
        raise BusinessException("当前密码不正确")
    new_password = decrypt(param.new_password)
    hashed = bcrypt.hashpw(new_password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
    entity.password = hashed
    ClientUserDao(db).update(entity, user_id=user_id)


class LoginUserApiProvider:
    def __init__(self, session_factory):
        self._session_factory = session_factory

    async def get_current_login_user_info(self, request) -> Optional[LoginUserInfo]:
        from core.auth import HeiClientAuthTool
        user_id = await HeiClientAuthTool.getLoginIdAsString(request)
        if not user_id:
            return None
        return self.get_login_user_info_by_id(user_id)

    def get_login_user_info_by_id(self, user_id: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = find_by_id(db, user_id)
            return to_login_user_info(entity)
        finally:
            db.close()

    def get_login_user_info_by_username(self, username: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = find_by_username(db, username)
            return to_login_user_info(entity)
        finally:
            db.close()

    def get_login_user_info_by_email(self, email: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = find_by_email(db, email)
            return to_login_user_info(entity)
        finally:
            db.close()

from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin
from sdk.shared.contracts import LoginUserInfo
from .models import ClientUser


class ClientUserVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    username: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    motto: Optional[str] = None
    gender: Optional[str] = None
    email: Optional[str] = None
    github: Optional[str] = None
    phone: Optional[str] = None
    status: Optional[str] = None
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None
    login_count: Optional[int] = 0
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class ClientUserPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    status: Optional[str] = None


class UpdateProfileParam(BaseModel):
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    username: Optional[str] = None


class UpdateAvatarParam(BaseModel):
    avatar: str


class UpdatePasswordParam(BaseModel):
    current_password: str
    new_password: str


def ClientUserToClientUserVO(src: Optional[ClientUser]) -> Optional[ClientUserVO]:
    if src is None:
        return None
    return ClientUserVO(
        id=src.id,
        username=src.username,
        nickname=src.nickname,
        avatar=src.avatar,
        motto=src.motto,
        gender=src.gender,
        email=src.email,
        github=src.github,
        phone=src.phone,
        status=src.status,
        last_login_at=src.last_login_at,
        last_login_ip=src.last_login_ip,
        login_count=src.login_count or 0,
        created_at=src.created_at,
        updated_at=src.updated_at,
    )


def ClientUserVOToClientUser(src: Optional[ClientUserVO]) -> Optional[ClientUser]:
    if src is None:
        return None
    dst = ClientUser(id=src.id or "")
    dst.username = src.username
    dst.nickname = src.nickname
    dst.avatar = src.avatar
    dst.motto = src.motto
    dst.gender = src.gender
    dst.email = src.email
    dst.github = src.github
    dst.phone = src.phone
    dst.status = src.status
    dst.last_login_at = src.last_login_at
    dst.last_login_ip = src.last_login_ip
    dst.login_count = src.login_count or 0
    return dst


def ClientUserToLoginUserInfo(src: Optional[ClientUser]) -> Optional[LoginUserInfo]:
    if src is None:
        return None
    return LoginUserInfo(
        id=src.id,
        username=src.username,
        password=src.password,
        nickname=src.nickname,
        avatar=src.avatar,
        motto=src.motto,
        gender=src.gender,
        birthday=src.birthday,
        email=src.email,
        status=src.status,
        last_login_at=src.last_login_at,
        last_login_ip=src.last_login_ip,
        login_count=src.login_count,
    )

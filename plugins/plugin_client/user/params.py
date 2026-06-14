from typing import Optional
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
    password: Optional[str] = None
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


class LoginUserInfoVO(LoginUserInfo):
    @classmethod
    def from_entity(cls, src: Optional["ClientUser"]) -> Optional["LoginUserInfoVO"]:
        if src is None:
            return None
        return cls(
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

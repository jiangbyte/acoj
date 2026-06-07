"""Client auth username params — mirrors hei-gin plugin-client/auth/username/params.go."""

from typing import Optional
from pydantic import BaseModel


class UsernameLoginParam(BaseModel):
    username: str
    password: str
    captcha_code: str
    captcha_id: str
    device_id: Optional[str] = None


class UsernameLoginResult(BaseModel):
    token: str = ""


class UsernameRegisterParam(BaseModel):
    username: str
    password: str
    captcha_code: str
    captcha_id: str


class UsernameRegisterResult(BaseModel):
    message: str = ""


class UsernameLogoutResult(BaseModel):
    message: str = ""

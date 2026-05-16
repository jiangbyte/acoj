from typing import Optional
from pydantic import BaseModel


class UsernameLoginParam(BaseModel):
    username: str
    password: str
    captcha_code: str
    captcha_id: str
    device_id: Optional[str]


class UsernameLoginResult(BaseModel):
    token: Optional[str] = None


class UsernameRegisterParam(BaseModel):
    username: str
    password: str
    captcha_code: str
    captcha_id: str


class UsernameRegisterResult(BaseModel):
    message: Optional[str] = None


class UsernameLogoutResult(BaseModel):
    message: Optional[str] = None

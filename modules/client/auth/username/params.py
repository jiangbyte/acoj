from typing import Optional
from pydantic import BaseModel


class UsernameLoginParam(BaseModel):
    username: str
    password: str
    captcha_code: Optional[str] = None
    captcha_id: Optional[str] = None
    device_type: Optional[str] = None
    device_id: Optional[str] = None


class UsernameLoginResult(BaseModel):
    token: Optional[str] = None


class UsernameRegisterParam(BaseModel):
    username: str
    password: str
    captcha_code: Optional[str] = None
    captcha_id: Optional[str] = None


class UsernameRegisterResult(BaseModel):
    message: Optional[str] = None


class UsernameLogoutResult(BaseModel):
    message: Optional[str] = None

from typing import Optional
from datetime import datetime
from decimal import Decimal
from pydantic import BaseModel, Field


class LoginUserInfo(BaseModel):
    id: Optional[str] = None
    account: Optional[str] = None
    password: Optional[str] = Field(default=None, exclude=True)
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    signature: Optional[str] = None
    motto: Optional[str] = None
    gender: Optional[str] = None
    birthday: Optional[datetime] = None
    constellation: Optional[str] = None
    real_name: Optional[str] = None
    id_card: Optional[str] = None
    native_place: Optional[str] = None
    province: Optional[str] = None
    city: Optional[str] = None
    district: Optional[str] = None
    address: Optional[str] = None
    zip_code: Optional[str] = None
    phone: Optional[str] = None
    mobile: Optional[str] = None
    email: Optional[str] = None
    wechat: Optional[str] = None
    qq: Optional[str] = None
    education: Optional[str] = None
    school: Optional[str] = None
    major: Optional[str] = None
    website: Optional[str] = None
    blog: Optional[str] = None
    github: Optional[str] = None
    interests: Optional[str] = None
    bio: Optional[str] = None
    marital_status: Optional[str] = None
    blood_type: Optional[str] = None
    height: Optional[Decimal] = None
    weight: Optional[Decimal] = None
    health_status: Optional[str] = None
    political_status: Optional[str] = None
    nationality: Optional[str] = None
    ethnicity: Optional[str] = None
    religion: Optional[str] = None
    language: Optional[str] = None
    status: Optional[str] = None
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None
    login_count: Optional[int] = None
    remark: Optional[str] = None

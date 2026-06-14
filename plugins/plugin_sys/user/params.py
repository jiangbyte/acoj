from typing import Optional, List
from plugins.plugin_sys.role.params import PermissionItem
from datetime import datetime, date
from pydantic import BaseModel, ConfigDict, Field
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class UpdateStatusParam(BaseModel):
    """Batch update user status — mirrors hei-gin's UpdateStatusParam."""
    ids: List[str]
    status: str


class BatchImportUser(BaseModel):
    """A single user entry in a batch import — mirrors hei-gin's BatchImportUser."""
    username: Optional[str] = None
    nickname: Optional[str] = None
    phone: Optional[str] = None
    email: Optional[str] = None
    gender: Optional[str] = None
    password: Optional[str] = None


class BatchImportParam(BaseModel):
    """Batch import users — mirrors hei-gin's BatchImportParam."""
    users: List[BatchImportUser]


class UserVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    username: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    motto: Optional[str] = None
    gender: Optional[str] = None
    birthday: Optional[date] = None
    email: Optional[str] = None
    github: Optional[str] = None
    phone: Optional[str] = None
    org_id: Optional[str] = None
    position_id: Optional[str] = None
    group_id: Optional[str] = None
    org_names: Optional[List[str]] = None
    group_names: Optional[List[str]] = None
    position_name: Optional[str] = None
    status: Optional[str] = None
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None
    login_count: Optional[int] = 0
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None
    password: Optional[str] = None
    role_ids: Optional[List[str]] = None


class UserMenuVO(BaseModel):
    id: str
    code: Optional[str] = None
    name: Optional[str] = None
    category: Optional[str] = None
    type: Optional[str] = None
    parent_id: Optional[str] = None
    route_path: Optional[str] = None
    component_path: Optional[str] = None
    redirect_path: Optional[str] = None
    icon: Optional[str] = None
    color: Optional[str] = None
    is_visible: Optional[bool] = None
    is_cache: Optional[bool] = None
    is_affix: Optional[bool] = None
    is_breadcrumb: Optional[bool] = None
    external_url: Optional[str] = None
    description: Optional[str] = None
    sort_code: Optional[int] = None
    status: Optional[str] = None
    children: List["UserMenuVO"] = Field(default_factory=list)


class UserExportVO(UserVO):
    pass


class UserPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    status: Optional[str] = None


class GrantRoleParam(BaseModel):
    user_id: str
    role_ids: List[str]


class GrantUserPermissionParam(BaseModel):
    user_id: str
    permissions: Optional[List[PermissionItem]] = None


class RefreshSessionACLParam(BaseModel):
    user_id: str


class BatchRefreshSessionACLParam(BaseModel):
    user_ids: List[str]


class UpdateProfileParam(BaseModel):
    username: Optional[str] = None
    nickname: Optional[str] = None
    motto: Optional[str] = None
    gender: Optional[str] = None
    birthday: Optional[date] = None
    email: Optional[str] = None
    github: Optional[str] = None
    phone: Optional[str] = None


class UpdateAvatarParam(BaseModel):
    avatar: str


class UpdatePasswordParam(BaseModel):
    current_password: str
    new_password: str

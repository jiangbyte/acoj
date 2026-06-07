"""
User service — standalone functions matching hei-gin's service.go pattern.

| hei-gin (Go)              | Python                          |
|----------------------------|---------------------------------|
| ``user.UserPage(c, p)``    | ``user_page(db, param)``        |
| ``user.UserCreate(c, v,u)``| ``user_create(db, vo, request)``|
| ``db.DB.WithContext(ctx)`` | ``db: Session`` (FastAPI DI)    |

The ``UserService`` class is retained for backward compatibility;
it delegates to the standalone functions internally.
"""

from __future__ import annotations

import asyncio
import logging
from typing import Optional, List, Dict
from datetime import datetime

import bcrypt
from fastapi import Request
from sqlalchemy.orm import Session

from .models import SysUser
from .params import (
    UpdateStatusParam, BatchImportParam, BatchImportUser,
    UserVO, UserPageParam, GrantRoleParam,
    GrantUserPermissionParam, UpdateProfileParam,
    UpdateAvatarParam, UpdatePasswordParam,
)
from .dao import UserDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import decrypt, apply_update
from core.auth import HeiAuthTool, LoginUserInfo
from core.utils.resolve_utils import resolve_name_path, resolve_path_from_map
from ..resource import SysResource
from core.auth.permission.hei_permission_interface import SUPER_ADMIN_CODE

logger = logging.getLogger(__name__)


# ═════════════════════════════════════════════════════════════════════
# Standalone service functions  —  mirror hei-gin service.go
# ═════════════════════════════════════════════════════════════════════

def user_find_by_id(db: Session, user_id: str) -> Optional[SysUser]:
    dao = UserDao(db)
    return dao.find_by_id(user_id)


def user_find_by_username(db: Session, username: str) -> Optional[SysUser]:
    dao = UserDao(db)
    return dao.find_by_username(username)


def user_find_by_email(db: Session, email: str) -> Optional[SysUser]:
    dao = UserDao(db)
    return dao.find_by_email(email)


def user_to_login_info(entity: Optional[SysUser]) -> Optional[LoginUserInfo]:
    if not entity:
        return None
    return LoginUserInfo(
        id=entity.id, username=entity.username, password=entity.password,
        nickname=entity.nickname, avatar=entity.avatar, motto=entity.motto,
        gender=entity.gender, birthday=entity.birthday, email=entity.email,
        github=entity.github, status=entity.status,
        last_login_at=entity.last_login_at, last_login_ip=entity.last_login_ip,
        login_count=entity.login_count,
    )


def user_record_login(db: Session, user_id: str, request: Request) -> None:
    dao = UserDao(db)
    entity = dao.find_by_id(user_id)
    if not entity:
        return
    entity.last_login_at = datetime.now()
    entity.last_login_ip = request.client.host if request.client else None
    entity.login_count = (entity.login_count or 0) + 1
    dao.update(entity)


def user_page(db: Session, param: UserPageParam) -> dict:
    dao = UserDao(db)
    result = dao.find_page_by_filters(param)
    records = result[PageDataField.RECORDS]
    user_ids = [r.id for r in records]
    role_map = dao.get_role_ids_map_by_user_ids(user_ids)
    group_map = dao.get_group_id_map_by_user_ids(user_ids)
    vo_list = []
    for r in records:
        vo = UserVO.model_validate(r).model_dump()
        vo["role_ids"] = role_map.get(r.id, [])
        vo["group_id"] = group_map.get(r.id)
        vo_list.append(vo)
    _batch_enrich(db, vo_list)
    return page_data(records=vo_list, total=result[PageDataField.TOTAL],
                     page=param.current, size=param.size)


def _batch_enrich(db: Session, vo_list: List[dict]) -> None:
    if not vo_list:
        return
    from sqlalchemy import select
    from plugins.plugin_sys.org.models import SysOrg
    from plugins.plugin_sys.group.models import SysGroup
    from plugins.plugin_sys.position.models import SysPosition

    position_ids = {vo["position_id"] for vo in vo_list if vo.get("position_id")}
    pos_name_map = {}
    if position_ids:
        rows = db.execute(select(SysPosition.id, SysPosition.name).where(
            SysPosition.id.in_(position_ids))).all()
        pos_name_map = {r.id: r.name for r in rows}

    org_rows = db.execute(
        select(SysOrg.id, SysOrg.name, SysOrg.parent_id)).all()
    org_node_map = {r.id: {"name": r.name, "parent_id": r.parent_id} for r in org_rows}
    group_rows = db.execute(
        select(SysGroup.id, SysGroup.name, SysGroup.parent_id)).all()
    group_node_map = {r.id: {"name": r.name, "parent_id": r.parent_id} for r in group_rows}

    org_paths = {}
    group_paths = {}
    for vo in vo_list:
        oid = vo.get("org_id")
        if oid:
            if oid not in org_paths:
                org_paths[oid] = resolve_path_from_map(oid, org_node_map)
            vo["org_names"] = org_paths[oid]
        gid = vo.get("group_id")
        if gid:
            if gid not in group_paths:
                group_paths[gid] = resolve_path_from_map(gid, group_node_map)
            vo["group_names"] = group_paths[gid]
        pid = vo.get("position_id")
        if pid:
            vo["position_name"] = pos_name_map.get(pid)


async def user_create(db: Session, vo: UserVO, request: Request) -> None:
    """Create a new user — mirrors hei-gin's UserCreate()."""
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    now = datetime.now()

    entity = SysUser(
        id="",  # will be generated by dao.insert
        status="ACTIVE",
        created_at=now,
        updated_at=now,
    )

    if vo.username:
        existing = dao.find_by_username(vo.username)
        if existing:
            raise BusinessException("账号已存在", 400)
        entity.username = vo.username

    if vo.password:
        entity.password = await _hash_password(decrypt(vo.password))

    if vo.nickname:
        entity.nickname = vo.nickname
    if vo.avatar:
        entity.avatar = vo.avatar
    if vo.motto:
        entity.motto = vo.motto
    if vo.gender:
        entity.gender = vo.gender
    if vo.birthday:
        entity.birthday = vo.birthday
    if vo.email:
        entity.email = vo.email
    if vo.github:
        entity.github = vo.github
    if vo.phone:
        entity.phone = vo.phone
    if vo.org_id:
        entity.org_id = vo.org_id
    if vo.position_id:
        entity.position_id = vo.position_id
    if vo.group_id:
        entity.group_id = vo.group_id
    if vo.status:
        entity.status = vo.status

    dao.insert(entity, user_id=user_id)


async def user_modify(db: Session, vo: UserVO, request: Request) -> None:
    """Modify an existing user — mirrors hei-gin's UserModify()."""
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)

    existing = dao.find_by_id(vo.id)
    if not existing:
        raise BusinessException("数据不存在", 400)

    update_data = {}

    if vo.username is not None:
        dup = dao.find_by_username(vo.username)
        if dup and dup.id != vo.id:
            raise BusinessException("账号已存在", 400)
        update_data["username"] = vo.username

    if vo.nickname is not None:
        update_data["nickname"] = vo.nickname
    if vo.avatar is not None:
        update_data["avatar"] = vo.avatar
    if vo.motto is not None:
        update_data["motto"] = vo.motto
    if vo.gender is not None:
        update_data["gender"] = vo.gender
    if vo.birthday is not None:
        update_data["birthday"] = vo.birthday
    if vo.email is not None:
        update_data["email"] = vo.email
    if vo.github is not None:
        update_data["github"] = vo.github
    if vo.phone is not None:
        update_data["phone"] = vo.phone

    # org_id/position_id/group_id: explicitly set to None when empty/nil
    if vo.org_id is not None:
        update_data["org_id"] = vo.org_id
    elif existing.org_id is not None:
        update_data["org_id"] = None

    if vo.position_id is not None:
        update_data["position_id"] = vo.position_id
    elif existing.position_id is not None:
        update_data["position_id"] = None

    if vo.group_id is not None:
        update_data["group_id"] = vo.group_id
    elif existing.group_id is not None:
        update_data["group_id"] = None

    if vo.password:
        update_data["password"] = await _hash_password(decrypt(vo.password))
    if vo.status:
        update_data["status"] = vo.status

    update_data["updated_at"] = datetime.now()

    apply_update(existing, update_data)
    dao.update(existing, user_id=user_id)
def user_remove(db: Session, param: IdsParam) -> None:
    dao = UserDao(db)
    dao.delete_by_ids(param.ids)


def user_detail(db: Session, id: str) -> Optional[dict]:
    dao = UserDao(db)
    entity = dao.find_by_id(id)
    if not entity:
        return None
    return UserVO.model_validate(entity).model_dump()


async def user_grant_roles(db: Session, param: GrantRoleParam, request: Request) -> None:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    dao.grant_roles(param.user_id, param.role_ids, created_by=user_id)


async def user_grant_permissions(
    db: Session, param: GrantUserPermissionParam, request: Request,
) -> None:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    dao.grant_permissions(param.user_id, param.permissions, created_by=user_id)


def user_get_permission_details(db: Session, user_id: str) -> list:
    dao = UserDao(db)
    return dao.get_permission_details_by_user_id(user_id)


def user_get_role_ids(db: Session, user_id: str) -> List[str]:
    dao = UserDao(db)
    return dao.get_role_ids_by_user_id(user_id)


async def user_get_current(db: Session, request: Request) -> Optional[dict]:
    current_user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not current_user_id:
        return None
    dao = UserDao(db)
    entity = dao.find_by_id(current_user_id)
    if not entity:
        return None
    from ..org.models import SysOrg
    from ..group.models import SysGroup
    from ..position.models import SysPosition
    vo = UserVO.model_validate(entity).model_dump()
    if entity.org_id:
        vo["org_name"] = resolve_name_path(entity.org_id, db, SysOrg)
    if entity.group_id:
        vo["group_name"] = resolve_name_path(entity.group_id, db, SysGroup)
    if entity.position_id:
        pos = db.get(SysPosition, entity.position_id)
        vo["position_name"] = pos.name if pos else None
    return vo


async def user_get_menus(db: Session, request: Request) -> List[Dict]:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return []

    role_codes = dao.get_user_role_codes(user_id)
    if SUPER_ADMIN_CODE in role_codes:
        resources = dao.get_all_resources()
        return _build_menu_tree(resources)

    role_ids = dao.get_user_role_ids_all_sources(user_id)
    if not role_ids:
        return []

    resource_ids = dao.get_role_resource_ids(role_ids)
    if not resource_ids:
        return []

    resources = dao.get_resources_by_ids(resource_ids)
    return _build_menu_tree(resources)


def _build_menu_tree(resources: List[SysResource]) -> List[Dict]:
    resource_map = {}
    for r in resources:
        resource_map[r.id] = {
            "id": r.id, "code": r.code, "name": r.name,
            "type": r.type, "parent_id": r.parent_id,
            "route_path": r.route_path, "component_path": r.component_path,
            "redirect_path": r.redirect_path, "icon": r.icon,
            "is_visible": r.is_visible, "is_cache": r.is_cache,
            "is_affix": r.is_affix, "is_breadcrumb": r.is_breadcrumb,
            "sort_code": r.sort_code, "children": [],
        }
    tree = []
    for r in resources:
        node = resource_map[r.id]
        if r.parent_id and r.parent_id != "0" and r.parent_id in resource_map:
            resource_map[r.parent_id]["children"].append(node)
        else:
            tree.append(node)
    return tree


async def user_get_permissions(db: Session, request: Request) -> List[str]:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return []
    role_ids = dao.get_user_role_ids_all_sources(user_id)
    if not role_ids:
        return []
    codes = dao.get_role_permission_codes(role_ids)
    return sorted(codes)


async def user_update_profile(db: Session, param: UpdateProfileParam, request: Request) -> None:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        raise BusinessException("用户未登录")
    entity = dao.find_by_id(user_id)
    if not entity:
        raise BusinessException("用户不存在")
    now = datetime.now()
    up = {"updated_at": now}
    if param.username is not None:
        up["username"] = param.username
    if param.nickname is not None:
        up["nickname"] = param.nickname
    if param.motto is not None:
        up["motto"] = param.motto
    if param.gender is not None:
        up["gender"] = param.gender
    if param.birthday is not None:
        up["birthday"] = param.birthday
    if param.email is not None:
        up["email"] = param.email
    if param.github is not None:
        up["github"] = param.github
    if param.phone is not None:
        up["phone"] = param.phone
    up["updated_by"] = user_id
    dao.db.execute(sa_update(SysUser).where(SysUser.id == user_id).values(**up))
    dao.db.commit()
async def user_update_avatar(db: Session, param: UpdateAvatarParam, request: Request) -> None:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        raise BusinessException("用户未登录")
    entity = dao.find_by_id(user_id)
    if not entity:
        raise BusinessException("用户不存在")
    entity.avatar = param.avatar
    dao.update(entity, user_id=user_id)


async def user_update_password(db: Session, param: UpdatePasswordParam, request: Request) -> None:
    dao = UserDao(db)
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        raise BusinessException("用户未登录")
    entity = dao.find_by_id(user_id)
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
    hashed = (
        await asyncio.to_thread(
            lambda: bcrypt.hashpw(
                new_password.encode("utf-8"), bcrypt.gensalt()
            )
        )
    ).decode("utf-8")
    entity.password = hashed
    dao.update(entity, user_id=user_id)


async def _hash_password(password: str) -> str:
    return (
        await asyncio.to_thread(
            lambda: bcrypt.hashpw(
                password.encode("utf-8"), bcrypt.gensalt()
            )
        )
    ).decode("utf-8")


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class  —  delegates to standalone functions
# ═════════════════════════════════════════════════════════════════════


# ═════════════════════════════════════════════════════════════════════
# Missing standalone service functions  —  mirrors hei-gin service.go
# ═════════════════════════════════════════════════════════════════════

def user_reset_password(db: Session, user_id: str) -> None:
    """Reset a user's password to the configured default (or a random one).

    Mirrors hei-gin's UserResetPassword.
    """
    if not user_id:
        raise BusinessException("ID不能为空", 400)
    from config.settings import settings
    raw_pwd = settings.user.reset_password
    if not raw_pwd:
        raw_pwd = _generate_random_password()
    hashed = bcrypt.hashpw(raw_pwd.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
    dao = UserDao(db)
    entity = dao.find_by_id(user_id)
    if not entity:
        raise BusinessException("用户不存在", 404)
    entity.password = hashed
    dao.update(entity)


def _generate_random_password() -> str:
    """Generate a random password using snowflake ID (mirrors hei-gin's generateRandomPassword)."""
    from core.utils import generate_id
    pid = generate_id()
    return pid[:16] if len(pid) > 16 else pid


def user_batch_import(db: Session, param: BatchImportParam) -> None:
    """Batch import users from a list.

    Mirrors hei-gin's UserBatchImport.
    """
    if not param.users:
        return
    from core.utils import generate_id
    from datetime import datetime
    now = datetime.now()
    batch = []
    for u in param.users:
        entity = SysUser(
            id=generate_id(),
            status="ACTIVE",
            created_at=now,
            updated_at=now,
        )
        if u.username is not None:
            entity.username = u.username
        if u.nickname is not None:
            entity.nickname = u.nickname
        if u.phone is not None:
            entity.phone = u.phone
        if u.email is not None:
            entity.email = u.email
        if u.gender is not None:
            entity.gender = u.gender
        batch.append(entity)
    if batch:
        dao = UserDao(db)
        for entity in batch:
            dao.insert(entity)


def user_update_status(db: Session, param: UpdateStatusParam) -> None:
    """Batch update user status.

    Mirrors hei-gin's UserUpdateStatus.
    """
    if not param.ids:
        return
    from datetime import datetime
    dao = UserDao(db)
    entities = dao.find_by_ids(param.ids)
    now = datetime.now()
    for entity in entities:
        entity.status = param.status
        entity.updated_at = now
        dao.update(entity)



def user_export(db: Session, param: UserPageParam) -> List[dict]:
    """Export users matching the filter — mirrors hei-gin's UserExport().

    Returns all matching users with enriched names and role IDs, without pagination.
    """
    dao = UserDao(db)
    rows = dao.find_all_by_filters(param)
    vo_list = []
    for r in rows:
        vo = UserVO.model_validate(r).model_dump()
        vo_list.append(vo)
    if vo_list:
        from plugins.plugin_sys.role.models import RelUserRole
        user_ids = [r.id for r in rows]
        role_rows = db.execute(
            select(RelUserRole.user_id, RelUserRole.role_id).where(
                RelUserRole.user_id.in_(user_ids),
            )
        ).all()
        role_map: Dict[str, List[str]] = {}
        for uid, rid in role_rows:
            role_map.setdefault(uid, []).append(rid)
        for vo in vo_list:
            vo["role_ids"] = role_map.get(vo["id"], [])
        _batch_enrich(db, vo_list)
    return vo_list


class UserService:
    """Legacy class-based API.  New code should use the standalone functions above."""

    def __init__(self, db: Session):
        self.dao = UserDao(db)
        self.db = db

    def find_by_id(self, user_id: str) -> Optional[SysUser]:
        return user_find_by_id(self.db, user_id)

    def find_by_username(self, username: str) -> Optional[SysUser]:
        return user_find_by_username(self.db, username)

    def find_by_email(self, email: str) -> Optional[SysUser]:
        return user_find_by_email(self.db, email)

    def to_login_user_info(self, entity: Optional[SysUser]) -> Optional[LoginUserInfo]:
        return user_to_login_info(entity)

    def record_login(self, user_id: str, request: Request) -> None:
        return user_record_login(self.db, user_id, request)

    def page(self, param: UserPageParam) -> dict:
        return user_page(self.db, param)

    async def create(self, vo: UserVO, request: Request) -> None:
        return await user_create(self.db, vo, request)

    async def modify(self, vo: UserVO, request: Request) -> None:
        return await user_modify(self.db, vo, request)

    def remove(self, param: IdsParam) -> None:
        return user_remove(self.db, param)

    def detail(self, param: IdParam) -> Optional[dict]:
        return user_detail(self.db, param.id)

    async def grant_roles(self, param: GrantRoleParam, request: Request) -> None:
        return await user_grant_roles(self.db, param, request)

    async def grant_permissions(self, param: GrantUserPermissionParam, request: Request) -> None:
        return await user_grant_permissions(self.db, param, request)

    def get_user_permission_details(self, user_id: str) -> list:
        return user_get_permission_details(self.db, user_id)

    def get_user_role_ids(self, user_id: str) -> List[str]:
        return user_get_role_ids(self.db, user_id)

    async def get_current_user(self, request: Request) -> Optional[dict]:
        return await user_get_current(self.db, request)

    async def get_current_user_menus(self, request: Request) -> List[Dict]:
        return await user_get_menus(self.db, request)

    async def get_current_user_permissions(self, request: Request) -> List[str]:
        return await user_get_permissions(self.db, request)

    async def update_profile(self, param: UpdateProfileParam, request: Request) -> None:
        return await user_update_profile(self.db, param, request)

    async def update_avatar(self, param: UpdateAvatarParam, request: Request) -> None:
        return await user_update_avatar(self.db, param, request)

    async def update_password(self, param: UpdatePasswordParam, request: Request) -> None:
        return await user_update_password(self.db, param, request)


# ═════════════════════════════════════════════════════════════════════
# LoginUserApiProvider  —  used by plugin_sys (kept as-is)

    def reset_password(self, user_id: str) -> None:
        return user_reset_password(self.db, user_id)

    def batch_import(self, param: BatchImportParam) -> None:
        return user_batch_import(self.db, param)

    def update_status(self, param: UpdateStatusParam) -> None:

    def export(self, param: UserPageParam) -> List[dict]:
        return user_export(self.db, param)
        return user_update_status(self.db, param)


# ═════════════════════════════════════════════════════════════════════

class LoginUserApiProvider:
    def __init__(self, session_factory):
        self._session_factory = session_factory

    async def get_current_login_user_info(self, request) -> Optional[LoginUserInfo]:
        user_id = await HeiAuthTool.getLoginIdAsString(request)
        if not user_id:
            return None
        return self.get_login_user_info_by_id(user_id)

    def get_login_user_info_by_id(self, user_id: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = user_find_by_id(db, user_id)
            return user_to_login_info(entity)
        finally:
            db.close()

    def get_login_user_info_by_username(self, username: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = user_find_by_username(db, username)
            return user_to_login_info(entity)
        finally:
            db.close()

    def get_login_user_info_by_email(self, email: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            entity = user_find_by_email(db, email)
            return user_to_login_info(entity)
        finally:
            db.close()

"""User service — mirrors hei-gin plugins/plugin-sys/user/service.go."""

from __future__ import annotations

import asyncio
import logging
from typing import Optional, List, Dict
from datetime import datetime

import bcrypt
from fastapi import Request
from sqlalchemy.orm import Session
from sqlalchemy import select, update as sa_update, delete as sa_delete
from sqlalchemy.sql import func

from .models import SysUser, RelUserRole, RelUserPermission
from .params import (
    UpdateStatusParam, BatchImportParam, BatchImportUser,
    UserVO, UserPageParam, GrantRoleParam,
    GrantUserPermissionParam, UpdateProfileParam,
    UpdateAvatarParam, UpdatePasswordParam,
)
from .repository import UserRepository
from sdk.shared.types import IdParam, IdsParam
from sdk.web.result import page_data, PageDataField
from sdk.web.exception import BusinessException
from sdk.utils import decrypt, apply_update, generate_id
from sdk.auth import HeiAuthTool
from sdk.shared.contracts import LoginUserInfo
from sdk.utils.resolve_utils import resolve_name_path, resolve_path_from_map
from plugins.plugin_sys.resource.models import SysResource
from plugins.plugin_sys.home.models import SysQuickAction
from plugins.plugin_sys.org.models import SysOrg
from plugins.plugin_sys.group.models import SysGroup
from plugins.plugin_sys.position.models import SysPosition
from plugins.plugin_sys.role.models import RelRolePermission, RelRoleResource
from sdk.auth.permission.hei_permission_interface import SUPER_ADMIN_CODE

logger = logging.getLogger(__name__)


# ═════════════════════════════════════════════════════════════════════
# Helpers
# ═════════════════════════════════════════════════════════════════════

def _parse_date(s: Optional[str]) -> Optional[datetime]:
    if not s:
        return None
    try:
        return datetime.strptime(s, "%Y-%m-%d")
    except ValueError:
        return None


def _batch_role_ids(db: Session, user_ids: list[str]) -> dict[str, list[str]]:
    if not user_ids:
        return {}
    rows = db.execute(
        select(RelUserRole.user_id, RelUserRole.role_id).where(RelUserRole.user_id.in_(user_ids))
    ).all()
    m: dict[str, list[str]] = {}
    for r in rows:
        m.setdefault(r.user_id, []).append(r.role_id)
    return m


def _enrich_names(db: Session, vo_list: list[dict]) -> None:
    """Mirrors hei-gin's enrichNames — resolves org/group names with parent chain, position name."""
    from sqlalchemy import select as _s
    if not vo_list:
        return

    # Position names
    position_ids = {vo.get("position_id") for vo in vo_list if vo.get("position_id")}
    pos_name_map = {}
    if position_ids:
        rows = db.execute(
            _s(SysPosition.id, SysPosition.name).where(SysPosition.id.in_(position_ids))
        ).all()
        pos_name_map = {r.id: r.name for r in rows}

    # Org/group nodes for parent chain resolution
    org_rows = db.execute(_s(SysOrg.id, SysOrg.name, SysOrg.parent_id)).all()
    org_node_map = {r.id: {"name": r.name, "parent_id": r.parent_id} for r in org_rows}
    group_rows = db.execute(_s(SysGroup.id, SysGroup.name, SysGroup.parent_id)).all()
    group_node_map = {r.id: {"name": r.name, "parent_id": r.parent_id} for r in group_rows}

    org_paths, group_paths = {}, {}
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


# ═════════════════════════════════════════════════════════════════════
# UserFindById / UserFindByUsername / UserFindByEmail
# ═════════════════════════════════════════════════════════════════════

def user_find_by_id(db: Session, user_id: str) -> Optional[SysUser]:
    repository = UserRepository(db)
    return repository.find_by_id(user_id)


def user_find_by_username(db: Session, username: str) -> Optional[SysUser]:
    repository = UserRepository(db)
    return repository.find_by_username(username)


def user_find_by_email(db: Session, email: str) -> Optional[SysUser]:
    repository = UserRepository(db)
    return repository.find_by_email(email)


# ═════════════════════════════════════════════════════════════════════
# UserToLoginInfo / UserRecordLogin
# ═════════════════════════════════════════════════════════════════════

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
    repository = UserRepository(db)
    entity = repository.find_by_id(user_id)
    if not entity:
        return
    entity.last_login_at = datetime.now()
    entity.last_login_ip = request.client.host if request.client else None
    entity.login_count = (entity.login_count or 0) + 1
    repository.update(entity)


# ═════════════════════════════════════════════════════════════════════
# UserPage
# ═════════════════════════════════════════════════════════════════════

def user_page(db: Session, param: UserPageParam) -> dict:
    """Mirrors hei-gin's UserPage."""
    repository = UserRepository(db)
    result = repository.find_page_by_filters(param)
    records = result[PageDataField.RECORDS]
    user_ids = [r.id for r in records]

    role_map = _batch_role_ids(db, user_ids)

    vo_list = []
    for r in records:
        vo = UserVO.model_validate(r).model_dump()
        vo["role_ids"] = role_map.get(r.id, [])
        vo_list.append(vo)
    _enrich_names(db, vo_list)
    return page_data(records=vo_list, total=result[PageDataField.TOTAL],
                     page=param.current, size=param.size)


# ═════════════════════════════════════════════════════════════════════
# UserCreate
# ═════════════════════════════════════════════════════════════════════

async def user_create(db: Session, vo: UserVO, request: Request) -> None:
    """Mirrors hei-gin's UserCreate."""
    now = datetime.now()
    e = SysUser(id=generate_id(), status="ACTIVE", created_at=now, updated_at=now)

    if vo.username:
        existing = db.execute(select(SysUser).where(SysUser.username == vo.username)).scalar_one_or_none()
        if existing:
            raise BusinessException("账号已存在", 400)
        e.username = vo.username

    if vo.password:
        e.password = await _hash_password(vo.password)

    if vo.nickname:
        e.nickname = vo.nickname
    if vo.avatar:
        e.avatar = vo.avatar
    if vo.motto:
        e.motto = vo.motto
    if vo.gender:
        e.gender = vo.gender
    if vo.birthday:
        e.birthday = vo.birthday
    if vo.email:
        e.email = vo.email
    if vo.github:
        e.github = vo.github
    if vo.phone:
        e.phone = vo.phone

    if vo.org_id:
        e.org_id = vo.org_id
    if vo.position_id:
        e.position_id = vo.position_id
    if vo.group_id:
        e.group_id = vo.group_id

    try:
        uid = await HeiAuthTool.getLoginIdDefaultNull(request)
        if uid:
            e.created_by = uid
            e.updated_by = uid
    except Exception:
        pass

    db.add(e)
    db.flush()

    if vo.role_ids:
        for rid in vo.role_ids:
            db.add(RelUserRole(id=generate_id(), user_id=e.id, role_id=rid))
    db.commit()


# ═════════════════════════════════════════════════════════════════════
# UserDetail
# ═════════════════════════════════════════════════════════════════════

def user_detail(db: Session, id: str) -> Optional[dict]:
    """Mirrors hei-gin's UserDetail — returns enriched VO with role IDs."""
    if not id:
        return None
    repository = UserRepository(db)
    entity = repository.find_by_id(id)
    if not entity:
        return None
    vo = UserVO.model_validate(entity).model_dump()
    # Enrich names (mirrors Go: enrichNames)
    _enrich_names(db, [vo])
    # Resolve role IDs (mirrors Go: batchRoleIDs)
    role_map = _batch_role_ids(db, [entity.id])
    vo["role_ids"] = role_map.get(entity.id, [])
    return vo


# ═════════════════════════════════════════════════════════════════════
# UserModify
# ═════════════════════════════════════════════════════════════════════

async def user_modify(db: Session, vo: UserVO, request: Request) -> None:
    """Mirrors hei-gin's UserModify."""
    if not vo.id:
        raise BusinessException("ID不能为空", 400)

    old = db.execute(select(SysUser).where(SysUser.id == vo.id)).scalar_one_or_none()
    if not old:
        raise BusinessException("数据不存在", 400)

    up: dict = {}
    if vo.username:
        existing = db.execute(
            select(SysUser).where(SysUser.username == vo.username, SysUser.id != vo.id)
        ).scalar_one_or_none()
        if existing:
            raise BusinessException("账号已存在", 400)
        up["username"] = vo.username

    if vo.nickname:
        up["nickname"] = vo.nickname
    if vo.avatar:
        up["avatar"] = vo.avatar
    if vo.motto:
        up["motto"] = vo.motto
    if vo.gender:
        up["gender"] = vo.gender
    if vo.birthday:
        up["birthday"] = vo.birthday
    if vo.email:
        up["email"] = vo.email
    if vo.github:
        up["github"] = vo.github
    if vo.phone:
        up["phone"] = vo.phone
    if vo.org_id is not None:
        up["org_id"] = vo.org_id
    elif old.org_id is not None:
        up["org_id"] = None
    if vo.position_id is not None:
        up["position_id"] = vo.position_id
    elif old.position_id is not None:
        up["position_id"] = None
    if vo.group_id is not None:
        up["group_id"] = vo.group_id
    elif old.group_id is not None:
        up["group_id"] = None
    if vo.status:
        up["status"] = vo.status
    if vo.password:
        up["password"] = await _hash_password(vo.password)

    up["updated_at"] = datetime.now()
    try:
        uid = await HeiAuthTool.getLoginIdDefaultNull(request)
        if uid:
            up["updated_by"] = uid
    except Exception:
        pass

    if up:
        db.execute(sa_update(SysUser).where(SysUser.id == vo.id).values(**up))

    if vo.role_ids is not None:
        db.execute(sa_delete(RelUserRole).where(RelUserRole.user_id == vo.id))
        for rid in vo.role_ids:
            db.add(RelUserRole(id=generate_id(), user_id=vo.id, role_id=rid))
    db.commit()


# ═════════════════════════════════════════════════════════════════════
# UserRemove — mirrors Go: transaction with cascading deletes
# ═════════════════════════════════════════════════════════════════════

def user_remove(db: Session, ids: list[str]) -> None:
    """Mirrors hei-gin's UserRemove — deletes user + rels + quick actions in transaction."""
    if not ids:
        return
    try:
        db.execute(sa_delete(RelUserRole).where(RelUserRole.user_id.in_(ids)))
        db.execute(sa_delete(RelUserPermission).where(RelUserPermission.user_id.in_(ids)))
        db.execute(sa_delete(SysQuickAction).where(SysQuickAction.user_id.in_(ids)))
        db.execute(sa_delete(SysUser).where(SysUser.id.in_(ids)))
        db.commit()
    except Exception:
        db.rollback()
        raise


# ═════════════════════════════════════════════════════════════════════
# UserGrantRole
# ═════════════════════════════════════════════════════════════════════

def user_grant_role(db: Session, param: GrantRoleParam) -> None:
    """Mirrors hei-gin's UserGrantRole — with dedup + transaction."""
    if not param.user_id:
        raise BusinessException("用户ID不能为空", 400)
    try:
        db.execute(sa_delete(RelUserRole).where(RelUserRole.user_id == param.user_id))
        seen: set[str] = set()
        batch = []
        for rid in param.role_ids:
            if rid not in seen:
                seen.add(rid)
                batch.append(RelUserRole(id=generate_id(), user_id=param.user_id, role_id=rid))
        if batch:
            db.add_all(batch)
        db.commit()
    except Exception:
        db.rollback()
        raise


def user_grant_roles(db: Session, param: GrantRoleParam, request: Request) -> None:
    """Convenience wrapper — mirrors hei-gin's UserGrantRoles."""
    user_grant_role(db, param)


# ═════════════════════════════════════════════════════════════════════
# UserGrantPermission
# ═════════════════════════════════════════════════════════════════════

def user_grant_permission(db: Session, param: GrantUserPermissionParam) -> None:
    """Mirrors hei-gin's UserGrantPermission — with transaction."""
    if not param.user_id:
        raise BusinessException("用户ID不能为空", 400)
    try:
        db.execute(sa_delete(RelUserPermission).where(RelUserPermission.user_id == param.user_id))
        batch = []
        for pi in param.permissions or []:
            r = RelUserPermission(
                id=generate_id(), user_id=param.user_id,
                permission_code=pi.permission_code, scope=pi.scope or "ALL",
            )
            if pi.custom_scope_group_ids:
                r.custom_scope_group_ids = pi.custom_scope_group_ids
            if pi.custom_scope_org_ids:
                r.custom_scope_org_ids = pi.custom_scope_org_ids
            batch.append(r)
        if batch:
            db.add_all(batch)
        db.commit()
    except Exception:
        db.rollback()
        raise


def user_grant_permissions(db: Session, param: GrantUserPermissionParam, request: Request) -> None:
    """Convenience wrapper — mirrors hei-gin's UserGrantPermissions."""
    user_grant_permission(db, param)


# ═════════════════════════════════════════════════════════════════════
# UserOwnPermissionDetails / UserOwnRoleIDs / UserOwnRoles
# ═════════════════════════════════════════════════════════════════════

def user_get_permission_details(db: Session, user_id: str) -> list[dict]:
    """Mirrors hei-gin's UserOwnPermissionDetails."""
    if not user_id:
        return []
    rows = db.execute(
        select(RelUserPermission).where(RelUserPermission.user_id == user_id)
    ).scalars().all()
    return [
        {
            "permission_code": r.permission_code,
            "scope": r.scope,
            "custom_scope_group_ids": r.custom_scope_group_ids,
            "custom_scope_org_ids": r.custom_scope_org_ids,
        }
        for r in rows
    ]


def user_get_role_ids(db: Session, user_id: str) -> list[str]:
    """Mirrors hei-gin's UserOwnRoleIDs."""
    if not user_id:
        return []
    rows = db.execute(
        select(RelUserRole.role_id).where(RelUserRole.user_id == user_id)
    ).scalars().all()
    return list(rows)


def user_own_roles(db: Session, user_id: str) -> list[str]:
    """Mirrors hei-gin's UserOwnRoles — returns role IDs list."""
    return user_get_role_ids(db, user_id)


# ═════════════════════════════════════════════════════════════════════
# UserCurrent — mirrors Go: just calls UserDetail
# ═════════════════════════════════════════════════════════════════════

async def user_get_current(db: Session, request: Request) -> Optional[dict]:
    """Mirrors hei-gin's UserCurrent."""
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return None
    return user_detail(db, user_id)


# ═════════════════════════════════════════════════════════════════════
# UserMenus — mirrors Go: build menu tree from resources
# ═════════════════════════════════════════════════════════════════════

async def user_get_menus(db: Session, request: Request) -> list[dict]:
    """Mirrors hei-gin's UserMenus."""
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return []

    from plugins.plugin_sys.role.models import SysRole

    # Check super admin
    role_ids = user_get_role_ids(db, user_id)
    is_super_admin = False
    if role_ids:
        roles = db.execute(select(SysRole.code).where(SysRole.id.in_(role_ids))).scalars().all()
        is_super_admin = SUPER_ADMIN_CODE in roles

    if is_super_admin:
        resources = db.execute(
            select(SysResource).where(SysResource.status == "ENABLED").order_by(SysResource.sort_code)
        ).scalars().all()
        return _build_menu_tree(resources)

    if not role_ids:
        return []

    # Get resource IDs from role-resource relations
    rr_rows = db.execute(
        select(RelRoleResource.resource_id).where(RelRoleResource.role_id.in_(role_ids))
    ).scalars().all()
    if not rr_rows:
        return []

    resource_ids = list(set(rr_rows))
    resources = db.execute(
        select(SysResource).where(
            SysResource.id.in_(resource_ids),
            SysResource.status == "ENABLED",
        ).order_by(SysResource.sort_code)
    ).scalars().all()
    return _build_menu_tree(resources)


def _build_menu_tree(resources: list[SysResource]) -> list[dict]:
    """Mirrors hei-gin's buildUserMenuTree."""
    resource_map: dict[str, dict] = {}
    parent_set: set[str] = set()

    for r in resources:
        resource_map[r.id] = {
            "id": r.id, "code": r.code, "name": r.name,
            "category": r.category, "type": r.type,
            "parent_id": r.parent_id,
            "route_path": r.route_path, "component_path": r.component_path,
            "redirect_path": r.redirect_path,
            "icon": r.icon, "color": r.color,
            "is_visible": r.is_visible, "is_cache": r.is_cache,
            "is_affix": r.is_affix, "is_breadcrumb": r.is_breadcrumb,
            "external_url": r.external_url,
            "description": r.description,
            "sort_code": r.sort_code, "status": r.status,
            "children": [],
        }
        if r.parent_id:
            parent_set.add(r.parent_id)

    tree: list[dict] = []
    for r_id, node in resource_map.items():
        pid = node.get("parent_id")
        if pid and pid in resource_map:
            resource_map[pid]["children"].append(node)
        else:
            tree.append(node)

    tree.sort(key=lambda x: x.get("sort_code", 0) or 0)
    return tree


# ═════════════════════════════════════════════════════════════════════
# UserPermissions — mirrors Go: role permissions + direct user permissions
# ═════════════════════════════════════════════════════════════════════

async def user_get_permissions(db: Session, request: Request) -> list[str]:
    """Mirrors hei-gin's UserPermissions — combines role perms + direct user perms."""
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not user_id:
        return []

    perm_codes: list[str] = []

    # Role permissions
    role_ids = user_get_role_ids(db, user_id)
    if role_ids:
        codes = db.execute(
            select(RelRolePermission.permission_code).distinct().where(
                RelRolePermission.role_id.in_(role_ids)
            )
        ).scalars().all()
        perm_codes.extend(codes)

    # Direct user permissions
    direct_codes = db.execute(
        select(RelUserPermission.permission_code).distinct().where(
            RelUserPermission.user_id == user_id
        )
    ).scalars().all()
    perm_codes.extend(direct_codes)

    return sorted(set(perm_codes))


# ═════════════════════════════════════════════════════════════════════
# UserUpdateProfile
# ═════════════════════════════════════════════════════════════════════

async def user_update_profile(db: Session, param: UpdateProfileParam, request: Request) -> None:
    """Mirrors hei-gin's UserUpdateProfile."""
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not uid:
        raise BusinessException("用户未登录", 401)

    up: dict = {}
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

    if up:
        up["updated_at"] = datetime.now()
        db.execute(sa_update(SysUser).where(SysUser.id == uid).values(**up))
        db.commit()


# ═════════════════════════════════════════════════════════════════════
# UserUpdateAvatar — mirrors Go: compress base64 + update
# ═════════════════════════════════════════════════════════════════════

async def user_update_avatar(db: Session, param: UpdateAvatarParam, request: Request) -> None:
    """Mirrors hei-gin's UserUpdateAvatar."""
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not uid:
        raise BusinessException("用户未登录", 401)
    if not param.avatar:
        raise BusinessException("头像不能为空", 400)

    from sdk.utils.image_utils import compress_base64_image
    compressed = compress_base64_image(param.avatar, max_width=512, max_height=512, quality=80)

    entity = db.execute(select(SysUser).where(SysUser.id == uid)).scalar_one_or_none()
    if not entity:
        raise BusinessException("用户不存在", 404)

    entity.avatar = compressed
    db.commit()


# ═════════════════════════════════════════════════════════════════════
# UserUpdatePassword — mirrors Go: decrypt + bcrypt
# ═════════════════════════════════════════════════════════════════════

async def user_update_password(db: Session, param: UpdatePasswordParam, request: Request) -> None:
    """Mirrors hei-gin's UserUpdatePassword."""
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    if not uid:
        raise BusinessException("用户未登录", 401)

    entity = db.execute(select(SysUser).where(SysUser.id == uid)).scalar_one_or_none()
    if not entity:
        raise BusinessException("用户不存在", 404)

    if not entity.password:
        raise BusinessException("未设置密码，无法修改", 400)

    if not bcrypt.checkpw(
        decrypt(param.current_password).encode("utf-8"),
        entity.password.encode("utf-8"),
    ):
        raise BusinessException("当前密码不正确", 400)

    new_hashed = bcrypt.hashpw(
        decrypt(param.new_password).encode("utf-8"), bcrypt.gensalt()
    ).decode("utf-8")
    entity.password = new_hashed
    entity.updated_at = datetime.now()
    db.commit()


# ═════════════════════════════════════════════════════════════════════
# UserResetPassword
# ═════════════════════════════════════════════════════════════════════

def user_reset_password(db: Session, user_id: str) -> None:
    """Mirrors hei-gin's UserResetPassword."""
    if not user_id:
        raise BusinessException("ID不能为空", 400)
    from sdk.config.settings import settings
    raw_pwd = settings.user_config.reset_password
    if not raw_pwd:
        raw_pwd = _generate_random_password()
    hashed = bcrypt.hashpw(raw_pwd.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
    db.execute(
        sa_update(SysUser).where(SysUser.id == user_id).values(
            password=hashed, updated_at=datetime.now()
        )
    )
    db.commit()


def _generate_random_password() -> str:
    """Mirrors hei-gin's generateRandomPassword."""
    pid = generate_id()
    return pid[:16] if len(pid) > 16 else pid


# ═════════════════════════════════════════════════════════════════════
# UserBatchImport — mirrors Go: with transaction
# ═════════════════════════════════════════════════════════════════════

def user_batch_import(db: Session, param: BatchImportParam) -> None:
    """Mirrors hei-gin's UserBatchImport."""
    if not param.users:
        return
    now = datetime.now()
    batch = []
    for u in param.users:
        e = SysUser(id=generate_id(), status="ACTIVE", created_at=now, updated_at=now)
        if u.username is not None:
            e.username = u.username
        if u.nickname is not None:
            e.nickname = u.nickname
        if u.phone is not None:
            e.phone = u.phone
        if u.email is not None:
            e.email = u.email
        if u.gender is not None:
            e.gender = u.gender
        batch.append(e)

    if batch:
        try:
            db.add_all(batch)
            db.commit()
        except Exception:
            db.rollback()
            raise


# ═════════════════════════════════════════════════════════════════════
# UserUpdateStatus
# ═════════════════════════════════════════════════════════════════════

def user_update_status(db: Session, param: UpdateStatusParam) -> None:
    """Mirrors hei-gin's UserUpdateStatus."""
    if not param.ids:
        return
    db.execute(
        sa_update(SysUser).where(SysUser.id.in_(param.ids)).values(
            status=param.status, updated_at=datetime.now()
        )
    )
    db.commit()


# ═════════════════════════════════════════════════════════════════════
# UserExport
# ═════════════════════════════════════════════════════════════════════

def user_export(db: Session, param: UserPageParam) -> list[dict]:
    """Mirrors hei-gin's UserExport."""
    repository = UserRepository(db)
    rows = repository.find_all_by_filters(param)
    vo_list = []
    for r in rows:
        vo = UserVO.model_validate(r).model_dump()
        vo_list.append(vo)

    if vo_list:
        user_ids = [r.id for r in rows]
        role_map = _batch_role_ids(db, user_ids)
        for vo in vo_list:
            vo["role_ids"] = role_map.get(vo["id"], [])
        _enrich_names(db, vo_list)
    return vo_list


# ═════════════════════════════════════════════════════════════════════
# _hash_password
# ═════════════════════════════════════════════════════════════════════

async def _hash_password(password: str) -> str:
    return (
        await asyncio.to_thread(
            lambda: bcrypt.hashpw(
                password.encode("utf-8"), bcrypt.gensalt()
            )
        )
    ).decode("utf-8")


# ═════════════════════════════════════════════════════════════════════
# CheckSuperAdmin
# ═════════════════════════════════════════════════════════════════════

def check_super_admin(db: Session, user_id: str) -> bool:
    """Mirrors hei-gin's CheckSuperAdmin."""
    if not user_id:
        return False
    role_ids = user_get_role_ids(db, user_id)
    if not role_ids:
        return False
    from plugins.plugin_sys.role.models import SysRole
    roles = db.execute(
        select(SysRole.code).where(SysRole.id.in_(role_ids))
    ).scalars().all()
    return SUPER_ADMIN_CODE in roles


# ═════════════════════════════════════════════════════════════════════
# UserService — legacy class-based API
# ═════════════════════════════════════════════════════════════════════

class UserService:
    """Legacy class-based API.  New code should use the standalone functions above."""

    def __init__(self, db: Session):
        self.db = db
        self.repository = UserRepository(db)

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
        return user_remove(self.db, param.ids)

    def detail(self, param: IdParam) -> Optional[dict]:
        return user_detail(self.db, param.id)

    def grant_role(self, param: GrantRoleParam) -> None:
        return user_grant_role(self.db, param)

    def grant_permission(self, param: GrantUserPermissionParam) -> None:
        return user_grant_permission(self.db, param)

    def get_user_permission_details(self, user_id: str) -> list[dict]:
        return user_get_permission_details(self.db, user_id)

    def get_user_role_ids(self, user_id: str) -> list[str]:
        return user_get_role_ids(self.db, user_id)

    def own_roles(self, user_id: str) -> list[str]:
        return user_own_roles(self.db, user_id)

    async def get_current_user(self, request: Request) -> Optional[dict]:
        return await user_get_current(self.db, request)

    async def get_current_user_menus(self, request: Request) -> list[dict]:
        return await user_get_menus(self.db, request)

    async def get_current_user_permissions(self, request: Request) -> list[str]:
        return await user_get_permissions(self.db, request)

    async def update_profile(self, param: UpdateProfileParam, request: Request) -> None:
        return await user_update_profile(self.db, param, request)

    async def update_avatar(self, param: UpdateAvatarParam, request: Request) -> None:
        return await user_update_avatar(self.db, param, request)

    async def update_password(self, param: UpdatePasswordParam, request: Request) -> None:
        return await user_update_password(self.db, param, request)

    def reset_password(self, user_id: str) -> None:
        return user_reset_password(self.db, user_id)

    def batch_import(self, param: BatchImportParam) -> None:
        return user_batch_import(self.db, param)

    def update_status(self, param: UpdateStatusParam) -> None:
        return user_update_status(self.db, param)

    def export(self, param: UserPageParam) -> list[dict]:
        return user_export(self.db, param)


# ═════════════════════════════════════════════════════════════════════
# LoginUserApiProvider
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

"""User service."""

from __future__ import annotations

import asyncio
from datetime import datetime
from typing import Optional

import bcrypt
from fastapi import Depends, Request
from sqlalchemy import delete as sa_delete
from sqlalchemy import select, update as sa_update
from sqlalchemy.orm import Session

from plugins.plugin_sys.group.models import SysGroup
from plugins.plugin_sys.home.models import SysQuickAction
from plugins.plugin_sys.org.models import SysOrg
from plugins.plugin_sys.position.models import SysPosition
from plugins.plugin_sys.resource.models import SysResource
from plugins.plugin_sys.role.models import RelRolePermission, RelRoleResource
from sdk.auth import HeiAuthTool
from sdk.auth.permission.hei_permission_interface import SUPER_ADMIN_CODE
from sdk.config.settings import settings
from sdk.infra.db import get_db
from sdk.shared.contracts import LoginUserInfo
from sdk.shared.di import ActorContext
from sdk.shared.types import IdParam, IdsParam
from sdk.utils import decrypt, generate_id
from sdk.utils.resolve_utils import resolve_path_from_map
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data

from .models import RelUserPermission, RelUserRole, SysUser
from .params import (
    BatchImportParam,
    GrantRoleParam,
    GrantUserPermissionParam,
    UpdateAvatarParam,
    UpdatePasswordParam,
    UpdateProfileParam,
    UpdateStatusParam,
    UserPageParam,
    UserVO,
)
from .repository import UserRepository


def _batch_role_ids(db: Session, user_ids: list[str]) -> dict[str, list[str]]:
    if not user_ids:
        return {}
    rows = db.execute(
        select(RelUserRole.user_id, RelUserRole.role_id).where(RelUserRole.user_id.in_(user_ids))
    ).all()
    role_map: dict[str, list[str]] = {}
    for row in rows:
        role_map.setdefault(row.user_id, []).append(row.role_id)
    return role_map


def _enrich_names(db: Session, vo_list: list[dict]) -> None:
    if not vo_list:
        return

    position_ids = {vo.get("position_id") for vo in vo_list if vo.get("position_id")}
    position_name_map: dict[str, str] = {}
    if position_ids:
        rows = db.execute(
            select(SysPosition.id, SysPosition.name).where(SysPosition.id.in_(position_ids))
        ).all()
        position_name_map = {row.id: row.name for row in rows}

    org_rows = db.execute(select(SysOrg.id, SysOrg.name, SysOrg.parent_id)).all()
    org_node_map = {row.id: {"name": row.name, "parent_id": row.parent_id} for row in org_rows}
    group_rows = db.execute(select(SysGroup.id, SysGroup.name, SysGroup.parent_id)).all()
    group_node_map = {row.id: {"name": row.name, "parent_id": row.parent_id} for row in group_rows}

    org_paths: dict[str, list[str]] = {}
    group_paths: dict[str, list[str]] = {}
    for vo in vo_list:
        org_id = vo.get("org_id")
        if org_id:
            if org_id not in org_paths:
                org_paths[org_id] = resolve_path_from_map(org_id, org_node_map)
            vo["org_names"] = org_paths[org_id]

        group_id = vo.get("group_id")
        if group_id:
            if group_id not in group_paths:
                group_paths[group_id] = resolve_path_from_map(group_id, group_node_map)
            vo["group_names"] = group_paths[group_id]

        position_id = vo.get("position_id")
        if position_id:
            vo["position_name"] = position_name_map.get(position_id)


def _build_menu_tree(resources: list[SysResource]) -> list[dict]:
    node_map: dict[str, dict] = {}
    roots: list[dict] = []

    for resource in resources:
        node_map[resource.id] = {
            "id": resource.id,
            "code": resource.code,
            "name": resource.name,
            "category": resource.category,
            "type": resource.type,
            "parent_id": resource.parent_id,
            "route_path": resource.route_path,
            "component_path": resource.component_path,
            "redirect_path": resource.redirect_path,
            "icon": resource.icon,
            "color": resource.color,
            "is_visible": resource.is_visible,
            "is_cache": resource.is_cache,
            "is_affix": resource.is_affix,
            "is_breadcrumb": resource.is_breadcrumb,
            "external_url": resource.external_url,
            "description": resource.description,
            "sort_code": resource.sort_code,
            "status": resource.status,
            "children": [],
        }

    for node in node_map.values():
        parent_id = node.get("parent_id")
        if parent_id and parent_id in node_map:
            node_map[parent_id]["children"].append(node)
        else:
            roots.append(node)

    roots.sort(key=lambda item: item.get("sort_code", 0) or 0)
    return roots


async def _hash_password(password: str) -> str:
    hashed = await asyncio.to_thread(
        lambda: bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())
    )
    return hashed.decode("utf-8")


def _generate_random_password() -> str:
    value = generate_id()
    return value[:16] if len(value) > 16 else value


def user_find_by_id(db: Session, user_id: str) -> Optional[SysUser]:
    return UserRepository(db).find_by_id(user_id)


def user_find_by_username(db: Session, username: str) -> Optional[SysUser]:
    return UserRepository(db).find_by_username(username)


def user_find_by_email(db: Session, email: str) -> Optional[SysUser]:
    return UserRepository(db).find_by_email(email)


def user_to_login_info(entity: Optional[SysUser]) -> Optional[LoginUserInfo]:
    if not entity:
        return None
    return LoginUserInfo(
        id=entity.id,
        username=entity.username,
        password=entity.password,
        nickname=entity.nickname,
        avatar=entity.avatar,
        motto=entity.motto,
        gender=entity.gender,
        birthday=entity.birthday,
        email=entity.email,
        github=entity.github,
        status=entity.status,
        last_login_at=entity.last_login_at,
        last_login_ip=entity.last_login_ip,
        login_count=entity.login_count,
    )


class UserService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, UserRepository):
            self.repository = repository_or_db
        else:
            self.repository = UserRepository(repository_or_db)
        self.db = self.repository.db

    @classmethod
    def from_db(cls, db: Session) -> "UserService":
        return cls(UserRepository(db))

    def find_by_id(self, user_id: str) -> Optional[SysUser]:
        return self.repository.find_by_id(user_id)

    def find_by_username(self, username: str) -> Optional[SysUser]:
        return self.repository.find_by_username(username)

    def find_by_email(self, email: str) -> Optional[SysUser]:
        return self.repository.find_by_email(email)

    def to_login_user_info(self, entity: Optional[SysUser]) -> Optional[LoginUserInfo]:
        return user_to_login_info(entity)

    def record_login(self, user_id: str, request: Request) -> None:
        entity = self.find_by_id(user_id)
        if not entity:
            return
        entity.last_login_at = datetime.now()
        entity.last_login_ip = request.client.host if request.client else None
        entity.login_count = (entity.login_count or 0) + 1
        self.repository.update(entity)

    def page(self, param: UserPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = result[PageDataField.RECORDS]
        role_map = _batch_role_ids(self.db, [record.id for record in records])

        vo_list = []
        for record in records:
            data = UserVO.model_validate(record).model_dump()
            data["role_ids"] = role_map.get(record.id, [])
            vo_list.append(data)
        _enrich_names(self.db, vo_list)

        return page_data(
            records=vo_list,
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size,
        )

    async def create(self, vo: UserVO, actor: Optional[ActorContext] = None) -> None:
        entity = SysUser(
            id=generate_id(),
            status="ACTIVE",
            created_at=datetime.now(),
            updated_at=datetime.now(),
        )

        if vo.username:
            existing = self.db.execute(
                select(SysUser).where(SysUser.username == vo.username)
            ).scalar_one_or_none()
            if existing:
                raise BusinessException("账号已存在", 400)
            entity.username = vo.username

        if vo.password:
            entity.password = await _hash_password(vo.password)

        entity.nickname = vo.nickname
        entity.avatar = vo.avatar
        entity.motto = vo.motto
        entity.gender = vo.gender
        entity.birthday = vo.birthday
        entity.email = vo.email
        entity.github = vo.github
        entity.phone = vo.phone
        entity.org_id = vo.org_id
        entity.position_id = vo.position_id
        entity.group_id = vo.group_id

        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id

        self.db.add(entity)
        self.db.flush()

        if vo.role_ids:
            for role_id in vo.role_ids:
                self.db.add(RelUserRole(id=generate_id(), user_id=entity.id, role_id=role_id))

        self.db.commit()

    async def modify(self, vo: UserVO, actor: Optional[ActorContext] = None) -> None:
        if not vo.id:
            raise BusinessException("ID不能为空", 400)

        entity = self.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在", 400)

        updates: dict = {}
        if vo.username:
            existing = self.db.execute(
                select(SysUser).where(SysUser.username == vo.username, SysUser.id != vo.id)
            ).scalar_one_or_none()
            if existing:
                raise BusinessException("账号已存在", 400)
            updates["username"] = vo.username

        for field in ["nickname", "avatar", "motto", "gender", "birthday", "email", "github", "phone"]:
            value = getattr(vo, field)
            if value:
                updates[field] = value

        for field in ["org_id", "position_id", "group_id"]:
            value = getattr(vo, field)
            if value is not None:
                updates[field] = value
            elif getattr(entity, field) is not None:
                updates[field] = None

        if vo.status:
            updates["status"] = vo.status
        if vo.password:
            updates["password"] = await _hash_password(vo.password)

        updates["updated_at"] = datetime.now()
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id

        self.db.execute(sa_update(SysUser).where(SysUser.id == vo.id).values(**updates))

        if vo.role_ids is not None:
            self.db.execute(sa_delete(RelUserRole).where(RelUserRole.user_id == vo.id))
            for role_id in vo.role_ids:
                self.db.add(RelUserRole(id=generate_id(), user_id=vo.id, role_id=role_id))

        self.db.commit()

    def remove(self, param: IdsParam) -> None:
        if not param.ids:
            return
        try:
            self.db.execute(sa_delete(RelUserRole).where(RelUserRole.user_id.in_(param.ids)))
            self.db.execute(sa_delete(RelUserPermission).where(RelUserPermission.user_id.in_(param.ids)))
            self.db.execute(sa_delete(SysQuickAction).where(SysQuickAction.user_id.in_(param.ids)))
            self.db.execute(sa_delete(SysUser).where(SysUser.id.in_(param.ids)))
            self.db.commit()
        except Exception:
            self.db.rollback()
            raise

    def detail(self, param: IdParam) -> Optional[dict]:
        if not param.id:
            return None
        entity = self.find_by_id(param.id)
        if not entity:
            return None

        data = UserVO.model_validate(entity).model_dump()
        data["role_ids"] = _batch_role_ids(self.db, [entity.id]).get(entity.id, [])
        _enrich_names(self.db, [data])
        return data

    def grant_role(self, param: GrantRoleParam) -> None:
        if not param.user_id:
            raise BusinessException("用户ID不能为空", 400)
        try:
            self.db.execute(sa_delete(RelUserRole).where(RelUserRole.user_id == param.user_id))
            seen: set[str] = set()
            for role_id in param.role_ids:
                if role_id in seen:
                    continue
                seen.add(role_id)
                self.db.add(RelUserRole(id=generate_id(), user_id=param.user_id, role_id=role_id))
            self.db.commit()
        except Exception:
            self.db.rollback()
            raise

    def grant_permission(self, param: GrantUserPermissionParam) -> None:
        if not param.user_id:
            raise BusinessException("用户ID不能为空", 400)
        try:
            self.db.execute(
                sa_delete(RelUserPermission).where(RelUserPermission.user_id == param.user_id)
            )
            for item in param.permissions or []:
                entity = RelUserPermission(
                    id=generate_id(),
                    user_id=param.user_id,
                    permission_code=item.permission_code,
                    scope=item.scope or "ALL",
                )
                if item.custom_scope_group_ids:
                    entity.custom_scope_group_ids = item.custom_scope_group_ids
                if item.custom_scope_org_ids:
                    entity.custom_scope_org_ids = item.custom_scope_org_ids
                self.db.add(entity)
            self.db.commit()
        except Exception:
            self.db.rollback()
            raise

    def get_user_permission_details(self, user_id: str) -> list[dict]:
        if not user_id:
            return []
        rows = self.db.execute(
            select(RelUserPermission).where(RelUserPermission.user_id == user_id)
        ).scalars().all()
        return [
            {
                "permission_code": row.permission_code,
                "scope": row.scope,
                "custom_scope_group_ids": row.custom_scope_group_ids,
                "custom_scope_org_ids": row.custom_scope_org_ids,
            }
            for row in rows
        ]

    def get_user_role_ids(self, user_id: str) -> list[str]:
        if not user_id:
            return []
        return list(
            self.db.execute(
                select(RelUserRole.role_id).where(RelUserRole.user_id == user_id)
            ).scalars().all()
        )

    def get_current_user(self, actor: Optional[ActorContext] = None) -> Optional[dict]:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return None
        return self.detail(IdParam(id=user_id))

    def _is_super_admin(self, role_ids: list[str]) -> bool:
        if not role_ids:
            return False
        from plugins.plugin_sys.role.models import SysRole

        codes = self.db.execute(select(SysRole.code).where(SysRole.id.in_(role_ids))).scalars().all()
        return SUPER_ADMIN_CODE in codes

    async def get_current_user_menus(self, actor: Optional[ActorContext] = None) -> list[dict]:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return []

        role_ids = self.get_user_role_ids(user_id)
        if self._is_super_admin(role_ids):
            resources = self.db.execute(
                select(SysResource)
                .where(SysResource.status == "ENABLED")
                .order_by(SysResource.sort_code.asc())
            ).scalars().all()
            return _build_menu_tree(resources)

        if not role_ids:
            return []

        resource_ids = list(
            set(
                self.db.execute(
                    select(RelRoleResource.resource_id).where(RelRoleResource.role_id.in_(role_ids))
                ).scalars().all()
            )
        )
        if not resource_ids:
            return []

        resources = self.db.execute(
            select(SysResource)
            .where(SysResource.id.in_(resource_ids), SysResource.status == "ENABLED")
            .order_by(SysResource.sort_code.asc())
        ).scalars().all()
        return _build_menu_tree(resources)

    async def get_current_user_permissions(self, actor: Optional[ActorContext] = None) -> list[str]:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return []

        permission_codes = set()
        role_ids = self.get_user_role_ids(user_id)
        if role_ids:
            permission_codes.update(
                self.db.execute(
                    select(RelRolePermission.permission_code)
                    .distinct()
                    .where(RelRolePermission.role_id.in_(role_ids))
                ).scalars().all()
            )

        permission_codes.update(
            self.db.execute(
                select(RelUserPermission.permission_code)
                .distinct()
                .where(RelUserPermission.user_id == user_id)
            ).scalars().all()
        )
        return sorted(permission_codes)

    async def update_profile(
        self,
        param: UpdateProfileParam,
        actor: Optional[ActorContext] = None,
    ) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            raise BusinessException("用户未登录", 401)

        updates: dict = {}
        for field in ["username", "nickname", "motto", "gender", "birthday", "email", "github", "phone"]:
            value = getattr(param, field)
            if value is not None:
                updates[field] = value

        if updates:
            updates["updated_at"] = datetime.now()
            self.db.execute(sa_update(SysUser).where(SysUser.id == user_id).values(**updates))
            self.db.commit()

    async def update_avatar(
        self,
        param: UpdateAvatarParam,
        actor: Optional[ActorContext] = None,
    ) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            raise BusinessException("用户未登录", 401)
        if not param.avatar:
            raise BusinessException("头像不能为空", 400)

        from sdk.utils.image_utils import compress_base64_image

        entity = self.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在", 404)

        entity.avatar = compress_base64_image(
            param.avatar,
            max_width=512,
            max_height=512,
            quality=80,
        )
        self.db.commit()

    async def update_password(
        self,
        param: UpdatePasswordParam,
        actor: Optional[ActorContext] = None,
    ) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            raise BusinessException("用户未登录", 401)

        entity = self.find_by_id(user_id)
        if not entity:
            raise BusinessException("用户不存在", 404)
        if not entity.password:
            raise BusinessException("未设置密码，无法修改", 400)

        current_password = decrypt(param.current_password).encode("utf-8")
        if not bcrypt.checkpw(current_password, entity.password.encode("utf-8")):
            raise BusinessException("当前密码不正确", 400)

        entity.password = bcrypt.hashpw(
            decrypt(param.new_password).encode("utf-8"),
            bcrypt.gensalt(),
        ).decode("utf-8")
        entity.updated_at = datetime.now()
        self.db.commit()

    def reset_password(self, user_id: str) -> None:
        if not user_id:
            raise BusinessException("ID不能为空", 400)
        raw_password = settings.user_config.reset_password or _generate_random_password()
        hashed_password = bcrypt.hashpw(
            raw_password.encode("utf-8"),
            bcrypt.gensalt(),
        ).decode("utf-8")
        self.db.execute(
            sa_update(SysUser).where(SysUser.id == user_id).values(
                password=hashed_password,
                updated_at=datetime.now(),
            )
        )
        self.db.commit()

    def batch_import(self, param: BatchImportParam) -> None:
        if not param.users:
            return
        now = datetime.now()
        entities = []
        for item in param.users:
            entities.append(
                SysUser(
                    id=generate_id(),
                    username=item.username,
                    nickname=item.nickname,
                    phone=item.phone,
                    email=item.email,
                    gender=item.gender,
                    status="ACTIVE",
                    created_at=now,
                    updated_at=now,
                )
            )
        try:
            self.db.add_all(entities)
            self.db.commit()
        except Exception:
            self.db.rollback()
            raise

    def update_status(self, param: UpdateStatusParam) -> None:
        if not param.ids:
            return
        self.db.execute(
            sa_update(SysUser).where(SysUser.id.in_(param.ids)).values(
                status=param.status,
                updated_at=datetime.now(),
            )
        )
        self.db.commit()

    def export(self, param: UserPageParam) -> list[dict]:
        rows = self.repository.find_all_by_filters(param)
        role_map = _batch_role_ids(self.db, [row.id for row in rows])
        data = []
        for row in rows:
            item = UserVO.model_validate(row).model_dump()
            item["role_ids"] = role_map.get(row.id, [])
            data.append(item)
        _enrich_names(self.db, data)
        return data


def get_user_service(db: Session = Depends(get_db)) -> UserService:
    return UserService.from_db(db)


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
            service = UserService.from_db(db)
            return service.to_login_user_info(service.find_by_id(user_id))
        finally:
            db.close()

    def get_login_user_info_by_username(self, username: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            service = UserService.from_db(db)
            return service.to_login_user_info(service.find_by_username(username))
        finally:
            db.close()

    def get_login_user_info_by_email(self, email: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            service = UserService.from_db(db)
            return service.to_login_user_info(service.find_by_email(email))
        finally:
            db.close()

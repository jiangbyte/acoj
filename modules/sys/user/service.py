from typing import Optional, List, Dict
from sqlalchemy.orm import Session
from sqlalchemy import select
from fastapi import Request
from .models import SysUser
from .params import UserVO, UserPageParam, UserExportParam, UserImportParam, GrantRoleParam, GrantGroupParam
from .dao import UserDao
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.exception import BusinessException
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template
from core.auth import HeiAuthTool, LoginUserInfo
from core.db.redis import get_client
from modules.sys.resource.models import SysResource
from modules.sys.permission.models import SysPermission
from modules.sys.role.models import RalRolePermission, RalRoleResource
from modules.sys.user.models import RalUserRole, RalUserGroup
from modules.sys.group.models import RalGroupRole
from modules.sys.org.models import RalOrgRole
import json
import logging

logger = logging.getLogger(__name__)


class UserService:
    def __init__(self, db: Session):
        self.dao = UserDao(db)
        self.db = db

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def find_by_id(self, user_id: str) -> Optional[SysUser]:
        return self.db.execute(
            select(SysUser).where(SysUser.id == user_id, SysUser.is_deleted == SoftDeleteEnum.NO)
        ).scalar_one_or_none()

    def find_by_account(self, account: str) -> Optional[SysUser]:
        return self.db.execute(
            select(SysUser).where(SysUser.account == account, SysUser.is_deleted == SoftDeleteEnum.NO)
        ).scalar_one_or_none()

    def find_by_email(self, email: str) -> Optional[SysUser]:
        return self.db.execute(
            select(SysUser).where(SysUser.email == email, SysUser.is_deleted == SoftDeleteEnum.NO)
        ).scalar_one_or_none()

    def to_login_user_info(self, entity: Optional[SysUser]) -> Optional[LoginUserInfo]:
        if not entity:
            return None
        return LoginUserInfo(
            id=entity.id,
            account=entity.account,
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

    def page(self, param: UserPageParam) -> dict:
        result = self.dao.find_page(param.current, param.size)
        records = []
        for r in result["records"]:
            vo = UserVO.model_validate(r).model_dump()
            self._enrich_vo(r.id, vo)
            records.append(vo)
        return page_data(
            records=records,
            total=result["total"],
            page=param.current,
            size=param.size
        )

    def _enrich_vo(self, user_id: str, vo: dict):
        """Add relation IDs to VO"""
        vo["role_ids"] = self.dao.get_role_ids_by_user_id(user_id)
        vo["group_ids"] = self.dao.get_group_ids_by_user_id(user_id)

    async def create(self, vo: UserVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)

        if vo.account and self.find_by_account(vo.account):
            raise BusinessException("账号已存在")
        if vo.email and self.find_by_email(vo.email):
            raise BusinessException("邮箱已存在")

        entity = SysUser(**strip_system_fields(vo.model_dump(), extra_fields={'role_ids', 'group_ids'}))
        entity.created_by = created_by
        self.dao.insert(entity)

        # Grant roles/groups if provided
        if vo.role_ids:
            self.dao.grant_roles(entity.id, vo.role_ids, created_by)
        if vo.group_ids:
            self.dao.grant_groups(entity.id, vo.group_ids, created_by)

    async def modify(self, vo: UserVO, request: Optional[Request] = None) -> None:
        updated_by = await self._get_current_user_id(request)
        entity = self.dao.find_by_id(vo.id)

        if not entity:
            raise BusinessException("数据不存在")

        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data, extra_protected={'password', 'role_ids', 'group_ids'})

        entity.updated_by = updated_by
        self.dao.update(entity)

        # Sync role/group assignments if provided
        # Use truthiness check (not None check) so empty list [] won't wipe existing assignments
        if vo.role_ids:
            self.dao.grant_roles(vo.id, vo.role_ids, updated_by)
        if vo.group_ids:
            self.dao.grant_groups(vo.id, vo.group_ids, updated_by)

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[UserVO]:
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        vo = UserVO.model_validate(entity)
        vo_dict = vo.model_dump()
        self._enrich_vo(param.id, vo_dict)
        return UserVO(**vo_dict)

    def export(self, param: UserExportParam):
        records: List[SysUser] = []
        if param.export_type == ExportTypeEnum.CURRENT.value:
            result = self.dao.find_page(param.current or 1, param.size or 10)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_id or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [UserVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "用户数据", "用户数据")

    def download_template(self):
        return export_excel(make_template(SysUser, extra_exclude={'password', 'org_id', 'position_id', 'last_login_at', 'last_login_ip', 'login_count'}), "用户导入模板", "用户数据")

    async def import_data(self, param: UserImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")

        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = SysUser(**strip_system_fields(vo.model_dump(), extra_fields={'role_ids', 'group_ids'}))
            entity.created_by = created_by
            entities.append(entity)

        self.dao.insert_batch(entities)
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    # ---- Grant APIs ----

    async def grant_roles(self, param: GrantRoleParam, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        self.dao.grant_roles(param.user_id, param.role_ids, created_by, param.scope, param.custom_scope_group_ids)

    async def grant_groups(self, param: GrantGroupParam, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        self.dao.grant_groups(param.user_id, param.group_ids, created_by)

    # ---- User role/group query ----
    def get_user_role_ids(self, user_id: str) -> List[str]:
        return self.dao.get_role_ids_by_user_id(user_id)

    def get_user_group_ids(self, user_id: str) -> List[str]:
        return self.dao.get_group_ids_by_user_id(user_id)

    # ---- Current user info for frontend ----

    async def get_current_user(self, request: Request) -> Optional[Dict]:
        user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            return None
        entity = self.find_by_id(user_id)
        if not entity:
            return None
        return {
            "id": entity.id,
            "account": entity.account,
            "nickname": entity.nickname,
            "avatar": entity.avatar,
            "status": entity.status,
        }

    async def get_current_user_menus(self, request: Request) -> List[Dict]:
        user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            return []

        role_ids = self._get_user_role_ids(user_id)
        if not role_ids:
            return []

        resource_ids = set()
        for role_id in role_ids:
            rids = (
                self.db.query(RalRoleResource.resource_id)
                .filter(RalRoleResource.role_id == role_id, RalRoleResource.is_deleted == "NO")
                .all()
            )
            resource_ids.update(r[0] for r in rids)

        if not resource_ids:
            return []

        resources = (
            self.db.query(SysResource)
            .filter(
                SysResource.id.in_(resource_ids),
                SysResource.category == "BACKEND_MENU",
                SysResource.type.in_(["DIRECTORY", "MENU"]),
                SysResource.status == "ENABLED",
                SysResource.is_deleted == "NO",
            )
            .order_by(SysResource.sort_code.asc())
            .all()
        )

        return self._build_menu_tree(resources)

    def _get_user_role_ids(self, user_id: str) -> List[str]:
        role_ids = set()

        # Direct role assignments
        direct = (
            self.db.query(RalUserRole.role_id)
            .filter(RalUserRole.user_id == user_id, RalUserRole.is_deleted == "NO")
            .all()
        )
        role_ids.update(r[0] for r in direct)

        # Via user groups
        group_ids = (
            self.db.query(RalUserGroup.group_id)
            .filter(RalUserGroup.user_id == user_id, RalUserGroup.is_deleted == "NO")
            .all()
        )
        if group_ids:
            group_role_ids = (
                self.db.query(RalGroupRole.role_id)
                .filter(RalGroupRole.group_id.in_(g[0] for g in group_ids), RalGroupRole.is_deleted == "NO")
                .all()
            )
            role_ids.update(r[0] for r in group_role_ids)

        # Via org (user's org_id → RalOrgRole)
        entity = self.find_by_id(user_id)
        if entity and entity.org_id:
            org_role_ids = (
                self.db.query(RalOrgRole.role_id)
                .filter(
                    RalOrgRole.org_id == entity.org_id,
                    RalOrgRole.is_deleted == "NO",
                )
                .all()
            )
            role_ids.update(r[0] for r in org_role_ids)

        return list(role_ids)

    def _build_menu_tree(self, resources: List[SysResource]) -> List[Dict]:
        resource_map = {}
        for r in resources:
            resource_map[r.id] = {
                "id": r.id,
                "code": r.code,
                "name": r.name,
                "type": r.type,
                "parent_id": r.parent_id,
                "route_path": r.route_path,
                "component_path": r.component_path,
                "redirect_path": r.redirect_path,
                "icon": r.icon,
                "is_visible": r.is_visible,
                "is_cache": r.is_cache,
                "is_affix": r.is_affix,
                "is_hidden": r.is_hidden,
                "is_breadcrumb": r.is_breadcrumb,
                "sort_code": r.sort_code,
                "children": [],
            }

        tree = []
        for r in resources:
            node = resource_map[r.id]
            if r.parent_id and r.parent_id in resource_map:
                resource_map[r.parent_id]["children"].append(node)
            else:
                tree.append(node)

        return tree

    async def get_current_user_permissions(self, request: Request) -> List[str]:
        user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
        if not user_id:
            return []

        role_ids = self._get_user_role_ids(user_id)
        if not role_ids:
            return []

        permission_ids = set()
        for role_id in role_ids:
            pids = (
                self.db.query(RalRolePermission.permission_id)
                .filter(RalRolePermission.role_id == role_id, RalRolePermission.is_deleted == "NO")
                .all()
            )
            permission_ids.update(p[0] for p in pids)

        if not permission_ids:
            return []

        permissions = (
            self.db.query(SysPermission.code)
            .filter(SysPermission.id.in_(permission_ids), SysPermission.is_deleted == "NO")
            .all()
        )

        return sorted(set(p[0] for p in permissions))


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
            service = UserService(db)
            entity = service.find_by_id(user_id)
            return service.to_login_user_info(entity)
        finally:
            db.close()

    def get_login_user_info_by_account(self, account: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            service = UserService(db)
            entity = service.find_by_account(account)
            return service.to_login_user_info(entity)
        finally:
            db.close()

    def get_login_user_info_by_email(self, email: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            service = UserService(db)
            entity = service.find_by_email(email)
            return service.to_login_user_info(entity)
        finally:
            db.close()

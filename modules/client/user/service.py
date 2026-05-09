from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from fastapi import Request
from .models import ClientUser
from .params import ClientUserVO, ClientUserPageParam, ClientUserExportParam, ClientUserImportParam
from .dao import ClientUserDao
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.exception import BusinessException
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiClientAuthTool, LoginUserInfo
import logging

logger = logging.getLogger(__name__)


class ClientUserService:
    def __init__(self, db: Session):
        self.dao = ClientUserDao(db)
        self.db = db

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: ClientUserPageParam) -> dict:
        result = self.dao.find_page(param.current, param.size)
        return page_data(
            records=[ClientUserVO.model_validate(r).model_dump() for r in result["records"]],
            total=result["total"],
            page=param.current,
            size=param.size
        )

    async def create(self, vo: ClientUserVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        entity = ClientUser(**strip_system_fields(vo.model_dump()))
        entity.created_by = created_by
        self.dao.insert(entity)

    async def modify(self, vo: ClientUserVO, request: Optional[Request] = None) -> None:
        updated_by = await self._get_current_user_id(request)
        entity = self.dao.find_by_id(vo.id)

        if not entity:
            raise BusinessException("数据不存在")

        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data, extra_protected={'password'})

        entity.updated_by = updated_by
        self.dao.update(entity)

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[ClientUserVO]:
        entity = self.dao.find_by_id(param.id)
        return ClientUserVO.model_validate(entity) if entity else None

    def export(self, param: ClientUserExportParam):
        records: List[ClientUser] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            result = self.dao.find_page(param.current or 1, param.size or 10)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_id or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [ClientUserVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "C端用户数据", "C端用户数据")

    def download_template(self):
        return export_excel(make_template(ClientUser, extra_exclude={'password', 'last_login_at', 'last_login_ip', 'login_count'}), "C端用户导入模板", "C端用户数据")

    async def import_data(self, param: ClientUserImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")

        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = ClientUser(**strip_system_fields(vo.model_dump()))
            entity.created_by = created_by
            entities.append(entity)

        self.dao.insert_batch(entities)
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    def find_by_account(self, account: str) -> Optional[ClientUser]:
        return self.db.execute(
            select(ClientUser).where(ClientUser.account == account, ClientUser.is_deleted == SoftDeleteEnum.NO)
        ).scalar_one_or_none()

    def to_login_user_info(self, entity: Optional[ClientUser]) -> Optional[LoginUserInfo]:
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
        )


class LoginUserApiProvider:
    def __init__(self, session_factory):
        self._session_factory = session_factory

    def get_login_user_info_by_account(self, account: str) -> Optional[LoginUserInfo]:
        db = self._session_factory()
        try:
            service = ClientUserService(db)
            entity = service.find_by_account(account)
            return service.to_login_user_info(entity)
        finally:
            db.close()

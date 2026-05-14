from typing import Optional
from datetime import datetime
from sqlalchemy.orm import Session
from fastapi import Request
from .params import ClientUserVO, ClientUserPageParam
from .dao import ClientUserDao
from .models import ClientUser
from core.exception import BusinessException
from core.utils import apply_update, export_excel, make_template
from core.auth import HeiClientAuthTool, LoginUserInfo
from core.db.base_service import BaseCrudService


class ClientUserService(BaseCrudService):
    model_class = ClientUser
    vo_class = ClientUserVO
    dao_class = ClientUserDao
    page_param_class = ClientUserPageParam
    export_name = "C端用户数据"

    @property
    def _auth_tool(self):
        return HeiClientAuthTool

    async def modify(self, vo: ClientUserVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data, extra_protected={'password'})
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def download_template(self):
        return export_excel(
            make_template(ClientUser, extra_exclude={'password', 'last_login_at', 'last_login_ip', 'login_count'}),
            "C端用户导入模板", "C端用户数据"
        )

    def find_by_account(self, account: str) -> Optional[ClientUser]:
        return self.dao.find_by_account(account)

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
            email=entity.email,
            status=entity.status,
            last_login_at=entity.last_login_at,
            last_login_ip=entity.last_login_ip,
            login_count=entity.login_count,
        )

    def record_login(self, user_id: str, request: Request) -> None:
        entity = self.dao.find_by_id(user_id)
        if not entity:
            return
        entity.last_login_at = datetime.now()
        entity.last_login_ip = request.client.host if request.client else None
        entity.login_count = (entity.login_count or 0) + 1
        self.dao.update(entity)


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

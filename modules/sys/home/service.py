from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from core.utils import generate_id, strip_system_fields
from core.auth import HeiAuthTool
from .dao import QuickActionDao
from .params import (
    QuickActionVO, HomeVO, HomeNotice, HomeStats,
    AddQuickActionParam, RemoveQuickActionParam, SortQuickActionParam,
)
from .models import SysQuickAction


class HomeService:
    def __init__(self, db: Session):
        self.dao = QuickActionDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    async def home(self, request: Request) -> HomeVO:
        user_id = await self._get_current_user_id(request)
        quick_actions: list[QuickActionVO] = []
        available_resources: list[QuickActionVO] = []

        if user_id:
            quick_actions = [QuickActionVO(**item) for item in self.dao.find_by_user_id(user_id)]
            available_resources = [
                QuickActionVO(resource_id=item["id"], **item) for item in self.dao.get_available_resources(user_id)
            ]

        notices = [HomeNotice(**item) for item in self.dao.get_notices()]
        stats = HomeStats(**self.dao.get_stats())

        return HomeVO(
            quick_actions=quick_actions,
            available_resources=available_resources,
            notices=notices,
            stats=stats,
        )

    async def add_quick_action(self, param: AddQuickActionParam, request: Request) -> None:
        user_id = await self._get_current_user_id(request)
        if not user_id:
            return

        existing = self.dao.find_by_user_and_resource(user_id, param.resource_id)
        if existing:
            return

        count = self.dao.count_quick_actions(user_id)
        entity = SysQuickAction(
            user_id=user_id,
            resource_id=param.resource_id,
            sort_code=(count + 1) * 10,
        )
        self.dao.insert(entity, user_id=user_id)

    async def remove_quick_action(self, param: RemoveQuickActionParam, request: Request) -> None:
        user_id = await self._get_current_user_id(request)
        if not user_id:
            return
        self.dao.delete_by_id(param.id)

    async def sort_quick_actions(self, param: SortQuickActionParam, request: Request) -> None:
        user_id = await self._get_current_user_id(request)
        if not user_id:
            return
        for idx, qa_id in enumerate(param.ids):
            entity = self.dao.find_by_id(qa_id)
            if entity and entity.user_id == user_id:
                entity.sort_code = (idx + 1) * 10
                self.dao.update(entity, user_id=user_id)

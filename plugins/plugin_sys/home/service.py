from typing import Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext

from .models import SysQuickAction
from .params import AddQuickActionParam, HomeNotice, HomeStats, HomeVO, QuickActionVO, RemoveQuickActionParam, SortQuickActionParam
from .repository import QuickActionRepository


class HomeService:
    def __init__(self, repository: QuickActionRepository):
        self.repository = repository

    @classmethod
    def from_db(cls, db: Session) -> "HomeService":
        return cls(QuickActionRepository(db))

    def home(self, actor: Optional[ActorContext] = None) -> HomeVO:
        user_id = actor.user_id if actor and actor.user_id else ""
        quick_actions: list[QuickActionVO] = []
        available_resources: list[QuickActionVO] = []
        if user_id:
            quick_actions = [QuickActionVO(**item) for item in self.repository.find_by_user_id(user_id)]
            available_resources = [
                QuickActionVO(resource_id=item["id"], **item) for item in self.repository.get_available_resources(user_id)
            ]
        notices = [HomeNotice(**item) for item in self.repository.get_notices()]
        stats = HomeStats(**self.repository.get_stats())
        return HomeVO(
            quick_actions=quick_actions,
            available_resources=available_resources,
            notices=notices,
            stats=stats,
        )

    def add_quick_action(self, param: AddQuickActionParam, actor: Optional[ActorContext] = None) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return
        existing = self.repository.find_by_user_and_resource(user_id, param.resource_id)
        if existing:
            return
        count = self.repository.count_quick_actions(user_id)
        entity = SysQuickAction(user_id=user_id, resource_id=param.resource_id, sort_code=(count + 1) * 10)
        self.repository.insert(entity, user_id=user_id)

    def remove_quick_action(self, param: RemoveQuickActionParam, actor: Optional[ActorContext] = None) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return
        self.repository.delete_by_id(param.id)

    def sort_quick_actions(self, param: SortQuickActionParam, actor: Optional[ActorContext] = None) -> None:
        user_id = actor.user_id if actor and actor.user_id else ""
        if not user_id:
            return
        entities = self.repository.find_by_ids(param.ids)
        entity_map = {entity.id: entity for entity in entities if entity.user_id == user_id}
        for index, quick_action_id in enumerate(param.ids):
            entity = entity_map.get(quick_action_id)
            if entity:
                entity.sort_code = (index + 1) * 10
        self.repository.db.commit()


def get_home_service(db: Session = Depends(get_db)) -> HomeService:
    return HomeService.from_db(db)

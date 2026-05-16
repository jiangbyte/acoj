from typing import Optional, List, Dict
from sqlalchemy.orm import Session
from fastapi import Request
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import strip_system_fields, apply_update
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


def _resolve_path_from_map(entity_id: Optional[str], node_map: Dict) -> List[str]:
    """Resolve a name path from a pre-built node_map (id -> {name, parent_id})."""
    if not entity_id:
        return []
    names = []
    current = entity_id
    while current and current in node_map:
        names.append(node_map[current]["name"])
        current = node_map[current].get("parent_id")
        if current == "0":
            break
    return list(reversed(names))


def _resolve_name_path(entity_id: Optional[str], db: Session, model_class) -> List[str]:
    """Resolve a hierarchical entity ID to a list of names from root to current."""
    if not entity_id:
        return []
    from sqlalchemy import select
    rows = db.execute(select(model_class.id, model_class.name, model_class.parent_id)).all()
    node_map = {r.id: {"name": r.name, "parent_id": r.parent_id} for r in rows}
    return _resolve_path_from_map(entity_id, node_map)


class BaseCrudService:
    """Standardized CRUD service base class.

    Subclasses must set class attributes:
        model_class   - SQLAlchemy model class
        vo_class      - Pydantic VO class (must have model_validate, model_dump)
        dao_class     - DAO class extending BaseDAO

    Subclasses override page(), create(), modify(), remove() etc.
    when custom logic is needed.
    """

    model_class = None
    vo_class = None
    dao_class = None
    page_param_class = None

    def __init__(self, db: Session):
        self.dao = self.dao_class(db)

    @property
    def _auth_tool(self):
        return HeiAuthTool

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await self._auth_tool.getLoginIdDefaultNull(request)
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param) -> dict:
        result = self.dao.find_page(param)
        records = [self.vo_class.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]]
        return page_data(
            records=records,
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size,
        )

    def detail(self, param: IdParam):
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        return self.vo_class.model_validate(entity).model_dump()

    async def create(self, vo, request: Optional[Request] = None) -> None:
        entity = self.model_class(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        apply_update(entity, vo.model_dump(exclude_unset=True))
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

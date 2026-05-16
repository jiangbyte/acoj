from typing import Optional, List, Dict, Set
from sqlalchemy.orm import Session
from fastapi import Request
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import strip_system_fields, apply_update
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


def _batch_resolve_user_nicknames(user_ids: Set[str], db: Session) -> Dict[str, str]:
    """Batch resolve user IDs to nicknames in one query."""
    if not user_ids:
        return {}
    from modules.sys.user.models import SysUser
    from sqlalchemy import select
    rows = db.execute(
        select(SysUser.id, SysUser.nickname).where(SysUser.id.in_(user_ids))
    ).all()
    return {row.id: row.nickname for row in rows}


def batch_enrich_creator_updater(vo_list: List[dict], db: Session) -> None:
    """Batch resolve created_by/updated_by for all VOs in one query — no N+1."""
    if not vo_list:
        return
    all_ids: Set[str] = set()
    for vo in vo_list:
        if vo.get("created_by"):
            all_ids.add(vo["created_by"])
        if vo.get("updated_by"):
            all_ids.add(vo["updated_by"])
    if not all_ids:
        return
    user_map = _batch_resolve_user_nicknames(all_ids, db)
    for vo in vo_list:
        if vo.get("created_by") and vo["created_by"] in user_map:
            vo["created_name"] = user_map[vo["created_by"]]
        if vo.get("updated_by") and vo["updated_by"] in user_map:
            vo["updated_name"] = user_map[vo["updated_by"]]


def enrich_creator_updater(vo: dict, db: Session) -> None:
    """Single-VO variant (used by detail()). Falls back to dict lookup."""
    from modules.sys.user.models import SysUser
    from sqlalchemy import select
    if "created_by" in vo and vo.get("created_by"):
        row = db.execute(
            select(SysUser.nickname).where(SysUser.id == vo["created_by"])
        ).scalar()
        if row:
            vo["created_name"] = row
    if "updated_by" in vo and vo.get("updated_by"):
        row = db.execute(
            select(SysUser.nickname).where(SysUser.id == vo["updated_by"])
        ).scalar()
        if row:
            vo["updated_name"] = row


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

    def _enrich_vo(self, vo: dict) -> None:
        """Hook to enrich a single VO dict (used by detail())."""
        enrich_creator_updater(vo, self.dao.db)

    def _batch_enrich(self, vo_list: List[dict]) -> None:
        """Batch enrichment hook for page() — resolves all creator/updater in one query.
        Subclasses override this to add batch-specific enrichment.
        """
        batch_enrich_creator_updater(vo_list, self.dao.db)

    def page(self, param) -> dict:
        result = self.dao.find_page(param)
        records = [self.vo_class.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]]
        self._batch_enrich(records)
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
        vo = self.vo_class.model_validate(entity).model_dump()
        self._enrich_vo(vo)
        return vo

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

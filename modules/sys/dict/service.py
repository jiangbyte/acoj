from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select
from fastapi import Request
from .models import SysDict
from .params import DictVO, DictTreeVO, DictPageParam, DictListParam, DictTreeParam, DictExportParam, DictImportParam
from .dao import DictDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, success
from core.exception import BusinessException
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.utils import export_excel, generate_id, strip_system_fields, apply_update, make_template
from core.auth import HeiAuthTool
from core.db.redis import get_client
from core.constants import DICT_CACHE_KEY
import json
import logging

logger = logging.getLogger(__name__)

class DictService:
    def __init__(self, db: Session):
        self.dao = DictDao(db)
        self.db = db

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: DictPageParam) -> dict:
        from sqlalchemy import select as q, func

        base_filters = [SysDict.is_deleted == SoftDeleteEnum.NO]
        if param.parent_id:
            base_filters.append(
                (SysDict.parent_id == param.parent_id) | (SysDict.id == param.parent_id)
            )
        if param.category:
            base_filters.append(SysDict.category == param.category)
        if param.keyword:
            keyword = f"%{param.keyword}%"
            base_filters.append(SysDict.label.ilike(keyword))

        count_query = select(func.count()).select_from(SysDict).where(*base_filters)
        total = self.db.execute(count_query).scalar() or 0

        offset = (param.current - 1) * param.size
        query = select(SysDict).where(*base_filters).order_by(SysDict.sort_code).offset(offset).limit(param.size)
        records = [DictVO.model_validate(r).model_dump() for r in self.db.execute(query).scalars().all()]
        return page_data(
            records=records,
            total=total,
            page=param.current,
            size=param.size
        )

    def list(self, param: DictListParam) -> List[dict]:
        base_filters = [SysDict.is_deleted == SoftDeleteEnum.NO]
        if param.parent_id is not None:
            base_filters.append(SysDict.parent_id == param.parent_id)
        if param.category is not None:
            base_filters.append(SysDict.category == param.category)

        query = select(SysDict).where(*base_filters).order_by(SysDict.sort_code)
        return [DictVO.model_validate(r).model_dump() for r in self.db.execute(query).scalars().all()]

    def tree(self, param: DictTreeParam) -> List[dict]:
        base_filters = [SysDict.is_deleted == SoftDeleteEnum.NO]
        if param.category:
            base_filters.append(SysDict.category == param.category)

        query = select(SysDict).where(*base_filters).order_by(SysDict.sort_code)
        records = list(self.db.execute(query).scalars().all())

        node_map = {}
        roots = []
        for r in records:
            r_dict = DictVO.model_validate(r).model_dump()
            r_dict["children"] = []
            node_map[r.id] = r_dict

        for r_dict in node_map.values():
            pid = r_dict.get("parent_id")
            if pid and pid in node_map:
                node_map[pid]["children"].append(r_dict)
            else:
                roots.append(r_dict)

        self._sort_tree(roots)
        return roots

    @staticmethod
    def _sort_tree(nodes: List[dict]):
        nodes.sort(key=lambda x: x.get("sort_code", 0) or 0)
        for n in nodes:
            children = n.get("children")
            if children:
                DictService._sort_tree(children)

    async def create(self, vo: DictVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)

        parent_id = vo.parent_id or "0"
        self._check_duplicate(parent_id, vo.label, vo.value, None)

        entity_data = strip_system_fields(vo.model_dump(), extra_fields={'parent_id'})
        if not entity_data.get("code"):
            entity_data["code"] = str(generate_id())
        entity = SysDict(**entity_data, parent_id=parent_id)
        entity.created_by = created_by
        self.dao.insert(entity)
        await self._sync_cache()

    def _check_duplicate(self, parent_id: str, label: Optional[str], value: Optional[str], exclude_id: Optional[str]):
        if label:
            if self.dao.count_by_parent_and_label(parent_id, label, exclude_id) > 0:
                raise BusinessException(f"同一父字典下已存在相同标签: {label}")
        if value:
            if self.dao.count_by_parent_and_value(parent_id, value, exclude_id) > 0:
                raise BusinessException(f"同一父字典下已存在相同值: {value}")

    async def modify(self, vo: DictVO, request: Optional[Request] = None) -> None:
        updated_by = await self._get_current_user_id(request)
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        parent_id = vo.parent_id or "0"
        self._check_duplicate(parent_id, vo.label, vo.value, vo.id)

        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)
        entity.parent_id = parent_id
        entity.updated_by = updated_by
        self.dao.update(entity)
        await self._sync_cache()

    def remove(self, param: IdsParam) -> None:
        for rid in param.ids:
            children = self.dao.find_by_parent_id(rid)
            if children:
                raise BusinessException(f"字典包含子节点，无法删除: {rid}")
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[DictVO]:
        entity = self.dao.find_by_id(param.id)
        return DictVO.model_validate(entity) if entity else None

    def export(self, param: DictExportParam):
        records: List[SysDict] = []
        if param.export_type == ExportTypeEnum.CURRENT.value:
            result = self.dao.find_page(param.current or 1, param.size or 10)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [DictVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "字典数据", "字典数据")

    def download_template(self):
        return export_excel(make_template(SysDict), "字典导入模板", "字典数据")

    async def import_data(self, param: DictImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")

        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = SysDict(**strip_system_fields(vo.model_dump()))
            entity.created_by = created_by
            entities.append(entity)

        self.dao.insert_batch(entities)
        await self._sync_cache()
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    # ---- Dict translation helpers ----

    def get_dict_label(self, type_code: str, value: str) -> Optional[str]:
        root = self.dao.find_by_code(type_code)
        if not root:
            return None
        children = self.dao.find_by_parent_id(root.id)
        for child in children:
            if child.value == value:
                return child.label
        return None

    def get_dict_children(self, type_code: str) -> List[dict]:
        root = self.dao.find_by_code(type_code)
        if not root:
            return []
        children = self.dao.find_by_parent_id(root.id)
        return [DictVO.model_validate(c).model_dump() for c in children]

    # ---- Redis cache ----

    async def _sync_cache(self):
        redis_client = get_client()
        if not redis_client:
            return
        try:
            base_filters = [SysDict.is_deleted == SoftDeleteEnum.NO]
            query = select(SysDict).where(*base_filters).order_by(SysDict.sort_code)
            records = list(self.db.execute(query).scalars().all())

            cache = {}
            for r in records:
                code = r.code
                if code and r.parent_id in ("0", None):
                    children_data = []
                    for c in records:
                        if c.parent_id == r.id:
                            children_data.append({"label": c.label, "value": c.value, "color": c.color})
                    if children_data:
                        cache[code] = children_data

            await redis_client.set(DICT_CACHE_KEY, json.dumps(cache, ensure_ascii=False))
        except Exception as e:
            logger.error(f"Failed to sync dict cache: {e}")

    @staticmethod
    async def get_cached_dicts() -> dict:
        redis_client = get_client()
        if not redis_client:
            return {}
        data = await redis_client.get(DICT_CACHE_KEY)
        if data:
            return json.loads(data)
        return {}

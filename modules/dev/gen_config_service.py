from typing import Optional, List
from sqlalchemy.orm import Session

from .params import GenConfigVO, GenConfigListParam, GenConfigIdParam
from .dao import GenConfigDao
from core.exception import BusinessException
from core.utils import apply_update


class GenConfigService:
    def __init__(self, db: Session):
        self.dao = GenConfigDao(db)

    def list(self, param: GenConfigListParam) -> list:
        configs = self.dao.find_by_basic_id(param.basic_id, param.table_type)
        results = []
        for c in configs:
            results.append({
                "id": c.id,
                "basic_id": c.basic_id,
                "is_table_key": c.is_table_key,
                "field_name": c.field_name,
                "field_remark": c.field_remark,
                "field_type": c.field_type,
                "field_language_type": c.field_language_type,
                "effect_type": c.effect_type,
                "dict_type_code": c.dict_type_code,
                "whether_table": c.whether_table,
                "whether_retract": c.whether_retract,
                "whether_add_update": c.whether_add_update,
                "whether_required": c.whether_required,
                "whether_unique": c.whether_unique,
                "query_whether": c.query_whether,
                "query_type": c.query_type,
                "table_type": c.table_type,
                "sort_code": c.sort_code,
                "is_deleted": c.is_deleted,
                "created_at": c.created_at,
                "created_by": c.created_by,
                "updated_at": c.updated_at,
                "updated_by": c.updated_by,
            })
        return results

    def modify(self, vo: GenConfigVO) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("配置数据不存在")

        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)
        self.dao.update(entity)

    def modify_batch(self, vo_list: List[GenConfigVO]) -> None:
        for vo in vo_list:
            self.modify(vo)

    def detail(self, param: GenConfigIdParam) -> Optional[GenConfigVO]:
        entity = self.dao.find_by_id(param.id)
        return GenConfigVO.model_validate(entity) if entity else None

    def delete(self, param: GenConfigIdParam) -> None:
        self.dao.delete_by_id(param.id)

    def delete_by_basic_id(self, basic_id: str) -> None:
        self.dao.delete_by_basic_id(basic_id)

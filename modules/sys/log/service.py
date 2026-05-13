from sqlalchemy import delete as sa_delete
from sqlalchemy.orm import Session
from . import SysLog
from .params import LogVO, LogPageParam, LogExportParam, LogImportParam, LogDeleteByCategoryParam
from .dao import LogDao
from core.db.base_service import BaseCrudService


class LogService(BaseCrudService):
    model_class = SysLog
    vo_class = LogVO
    dao_class = LogDao
    page_param_class = LogPageParam
    export_name = "操作日志数据"

    def delete_by_category(self, param: LogDeleteByCategoryParam) -> None:
        db = self.dao.db
        stmt = sa_delete(SysLog).where(SysLog.category == param.category)
        db.execute(stmt)
        db.commit()

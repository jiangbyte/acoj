from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from .params import NoticeVO, NoticePageParam, NoticeExportParam, NoticeImportParam
from .dao import NoticeDao
from .models import SysNotice
from core.pojo import IdParam
from core.exception import BusinessException
from core.db.base_service import BaseCrudService


class NoticeService(BaseCrudService):
    model_class = SysNotice
    vo_class = NoticeVO
    dao_class = NoticeDao
    page_param_class = NoticePageParam
    export_name = "通知数据"

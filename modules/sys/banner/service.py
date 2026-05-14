from sqlalchemy.orm import Session
from .models import SysBanner
from .params import BannerVO, BannerPageParam, BannerExportParam, BannerImportParam
from .dao import BannerDao
from core.db.base_service import BaseCrudService


class BannerService(BaseCrudService):
    model_class = SysBanner
    vo_class = BannerVO
    dao_class = BannerDao
    page_param_class = BannerPageParam
    export_name = "Banner数据"

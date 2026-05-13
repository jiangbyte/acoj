
from fastapi import APIRouter, Depends, Query, Request, UploadFile, File
from sqlalchemy.orm import Session
from core.result import Result, PageData, success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission
from core.utils.excel_utils import handle_import
from ...params import BannerVO, BannerPageParam, BannerExportParam, BannerImportParam
from ...service import BannerService

router = APIRouter()


@router.get(
    "/api/v1/sys/banner/page",
    summary="获取Banner分页",
    response_model=Result[PageData[BannerVO]]
)
@HeiCheckPermission("sys:banner:page")
async def page(
    request: Request,
    param: BannerPageParam = Depends(),
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    return success(service.page(param))


@router.post(
    "/api/v1/sys/banner/create",
    summary="添加Banner",
    response_model=Result
)
@HeiCheckPermission("sys:banner:create")
async def create(
    request: Request,
    vo: BannerVO,
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    await service.create(vo, request)
    return success()


@router.post(
    "/api/v1/sys/banner/modify",
    summary="编辑Banner",
    response_model=Result
)
@HeiCheckPermission("sys:banner:modify")
async def modify(
    request: Request,
    vo: BannerVO,
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    await service.modify(vo, request)
    return success()


@router.post(
    "/api/v1/sys/banner/remove",
    summary="删除Banner",
    response_model=Result
)
@HeiCheckPermission("sys:banner:remove")
async def remove(
    request: Request,
    param: IdsParam,
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    service.remove(param)
    return success()


@router.get(
    "/api/v1/sys/banner/detail",
    summary="获取Banner详情",
    response_model=Result[BannerVO]
)
@HeiCheckPermission("sys:banner:detail")
async def detail(
    request: Request,
    id: str = Query(...),
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    data = service.detail(IdParam(id=id))
    return success(data.model_dump() if data else None)


@router.get(
    "/api/v1/sys/banner/export",
    summary="导出Banner数据")
@HeiCheckPermission("sys:banner:export")
async def export(
    request: Request,
    param: BannerExportParam = Depends(),
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    return service.export(param)


@router.get(
    "/api/v1/sys/banner/template",
    summary="下载Banner导入模板")
@HeiCheckPermission("sys:banner:template")
async def download_template(
    request: Request,
    db: Session = Depends(get_db)
):
    service = BannerService(db)
    return service.download_template()


@router.post(
    "/api/v1/sys/banner/import",
    summary="导入Banner数据",
    response_model=Result
)
@HeiCheckPermission("sys:banner:import")
async def import_data(
    request: Request,
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    return await handle_import(file, BannerService, BannerVO, BannerImportParam, db, request)

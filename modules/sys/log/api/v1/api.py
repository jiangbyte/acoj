from fastapi import APIRouter, Depends, Query, Request, UploadFile, File
from sqlalchemy.orm import Session
from core.result import Result, PageData, success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission
from core.utils.excel_utils import handle_import
from ...params import LogVO, LogPageParam, LogExportParam, LogImportParam, LogDeleteByCategoryParam
from ...service import LogService

router = APIRouter()


@router.get(
    "/api/v1/sys/log/page",
    summary="获取操作日志分页",
    response_model=Result[PageData[LogVO]]
)
@HeiCheckPermission("sys:log:page")
async def page(
    request: Request,
    param: LogPageParam = Depends(),
    db: Session = Depends(get_db)
):
    service = LogService(db)
    return success(service.page(param))


@router.post(
    "/api/v1/sys/log/create",
    summary="添加操作日志",
    response_model=Result
)
@HeiCheckPermission("sys:log:create")
async def create(
    request: Request,
    vo: LogVO,
    db: Session = Depends(get_db)
):
    service = LogService(db)
    await service.create(vo, request)
    return success()


@router.post(
    "/api/v1/sys/log/modify",
    summary="编辑操作日志",
    response_model=Result
)
@HeiCheckPermission("sys:log:modify")
async def modify(
    request: Request,
    vo: LogVO,
    db: Session = Depends(get_db)
):
    service = LogService(db)
    await service.modify(vo, request)
    return success()


@router.post(
    "/api/v1/sys/log/remove",
    summary="删除操作日志",
    response_model=Result
)
@HeiCheckPermission("sys:log:remove")
async def remove(
    request: Request,
    param: IdsParam,
    db: Session = Depends(get_db)
):
    service = LogService(db)
    service.remove(param)
    return success()


@router.get(
    "/api/v1/sys/log/detail",
    summary="获取操作日志详情",
    response_model=Result[LogVO]
)
@HeiCheckPermission("sys:log:detail")
async def detail(
    request: Request,
    id: str = Query(...),
    db: Session = Depends(get_db)
):
    service = LogService(db)
    data = service.detail(IdParam(id=id))
    return success(data.model_dump() if data else None)


@router.get(
    "/api/v1/sys/log/export",
    summary="导出操作日志数据")
@HeiCheckPermission("sys:log:export")
async def export_data(
    request: Request,
    param: LogExportParam = Depends(),
    db: Session = Depends(get_db)
):
    service = LogService(db)
    return service.export(param)


@router.get(
    "/api/v1/sys/log/template",
    summary="下载操作日志导入模板")
@HeiCheckPermission("sys:log:template")
async def download_template(
    request: Request,
    db: Session = Depends(get_db)
):
    service = LogService(db)
    return service.download_template()


@router.post(
    "/api/v1/sys/log/import",
    summary="导入操作日志数据",
    response_model=Result
)
@HeiCheckPermission("sys:log:import")
async def import_data(
    request: Request,
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    return await handle_import(file, LogService, LogVO, LogImportParam, db, request)


@router.post(
    "/api/v1/sys/log/delete-by-category",
    summary="按分类清空日志",
    response_model=Result
)
@HeiCheckPermission("sys:log:remove")
async def delete_by_category(
    request: Request,
    param: LogDeleteByCategoryParam,
    db: Session = Depends(get_db)
):
    service = LogService(db)
    service.delete_by_category(param)
    return success()

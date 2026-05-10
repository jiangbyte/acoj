from typing import List
from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session
from core.result import Result, success
from core.db import get_db
from core.auth.decorator import HeiCheckPermission
from ...params import (
    GenBasicVO, GenBasicPageParam,
    GenBasicIdParam, GenBasicTableColumnParam,
    GenConfigVO, GenConfigListParam, GenConfigIdParam,
)
from ...gen_basic_service import GenBasicService
from ...gen_config_service import GenConfigService
from ...type_utils import TYPE_MAP

router = APIRouter()


# ========== GenBasic CRUD ==========

@router.get("/api/v1/sys/dev/gen/basic/page", summary="获取代码生成基础分页", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-page")
async def gen_basic_page(
    request: Request,
    param: GenBasicPageParam = Depends(),
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    return success(service.page(param))


@router.post("/api/v1/sys/dev/gen/basic/create", summary="添加代码生成基础", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-create")
async def gen_basic_create(
    request: Request,
    vo: GenBasicVO,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    result = await service.create(vo, request)
    return success(result)


@router.post("/api/v1/sys/dev/gen/basic/modify", summary="编辑代码生成基础", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-modify")
async def gen_basic_modify(
    request: Request,
    vo: GenBasicVO,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/dev/gen/basic/delete", summary="删除代码生成基础", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-delete")
async def gen_basic_delete(
    request: Request,
    param_list: List[GenBasicIdParam],
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    service.delete(param_list)
    return success()


@router.get("/api/v1/sys/dev/gen/basic/detail", summary="获取代码生成基础详情", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-detail")
async def gen_basic_detail(
    request: Request,
    id: str,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    data = service.detail(GenBasicIdParam(id=id))
    return success(data)


@router.get("/api/v1/sys/dev/gen/basic/language_types", summary="获取语言类型列表", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-language-types")
async def gen_basic_language_types(
    request: Request,
):
    types = sorted(set(item[1] for item in TYPE_MAP))
    return success([{"label": t, "value": t} for t in types])


@router.get("/api/v1/sys/dev/gen/basic/tables", summary="获取所有表信息", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-tables")
async def gen_basic_tables(
    request: Request,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    return success([t.model_dump() for t in service.tables()])


@router.get("/api/v1/sys/dev/gen/basic/table_columns", summary="获取表内所有字段信息", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-table-columns")
async def gen_basic_table_columns(
    request: Request,
    table_name: str,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    return success([t.model_dump() for t in service.table_columns(GenBasicTableColumnParam(table_name=table_name))])


@router.post("/api/v1/sys/dev/gen/basic/exec_gen_pro", summary="执行代码生成（项目内）", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-exec-gen-pro")
async def gen_basic_exec_gen_pro(
    request: Request,
    param: GenBasicIdParam,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    service.exec_gen_pro(param)
    return success()


@router.get("/api/v1/sys/dev/gen/basic/preview_gen", summary="预览代码生成", response_model=Result)
@HeiCheckPermission("sys:dev:gen-basic-preview-gen")
async def gen_basic_preview_gen(
    request: Request,
    id: str,
    db: Session = Depends(get_db)
):
    service = GenBasicService(db)
    result = service.preview_gen(GenBasicIdParam(id=id))
    return success(result.model_dump())


# ========== GenConfig CRUD ==========

@router.get("/api/v1/sys/dev/gen/config/list", summary="获取代码生成配置列表", response_model=Result)
@HeiCheckPermission("sys:dev:gen-config-list")
async def gen_config_list(
    request: Request,
    basic_id: str,
    table_type: str = None,
    db: Session = Depends(get_db)
):
    service = GenConfigService(db)
    return success(service.list(GenConfigListParam(basic_id=basic_id, table_type=table_type)))


@router.post("/api/v1/sys/dev/gen/config/modify", summary="编辑代码生成配置", response_model=Result)
@HeiCheckPermission("sys:dev:gen-config-modify")
async def gen_config_modify(
    request: Request,
    vo: GenConfigVO,
    db: Session = Depends(get_db)
):
    service = GenConfigService(db)
    service.modify(vo)
    return success()


@router.post("/api/v1/sys/dev/gen/config/delete", summary="删除代码生成配置", response_model=Result)
@HeiCheckPermission("sys:dev:gen-config-delete")
async def gen_config_delete(
    request: Request,
    param: GenConfigIdParam,
    db: Session = Depends(get_db)
):
    service = GenConfigService(db)
    service.delete(param)
    return success()


@router.get("/api/v1/sys/dev/gen/config/detail", summary="获取代码生成配置详情", response_model=Result)
@HeiCheckPermission("sys:dev:gen-config-detail")
async def gen_config_detail(
    request: Request,
    id: str,
    db: Session = Depends(get_db)
):
    service = GenConfigService(db)
    data = service.detail(GenConfigIdParam(id=id))
    return success(data)


@router.post("/api/v1/sys/dev/gen/config/modify_batch", summary="批量编辑代码生成配置", response_model=Result)
@HeiCheckPermission("sys:dev:gen-config-modify-batch")
async def gen_config_modify_batch(
    request: Request,
    vo_list: List[GenConfigVO],
    db: Session = Depends(get_db)
):
    service = GenConfigService(db)
    service.modify_batch(vo_list)
    return success()

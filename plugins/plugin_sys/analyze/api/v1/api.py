"""Analyze API — mirrors hei-gin plugins/plugin-sys/analyze/api/v1/api.go 1:1."""

from fastapi import APIRouter, Depends
from sdk.web.result import success
from ...service import AnalyzeService, get_analyze_service

router = APIRouter()


@router.get("/api/v1/sys/analyze/dashboard", summary="获取仪表盘数据")
def dashboard(service: AnalyzeService = Depends(get_analyze_service)):
    return success(service.dashboard())

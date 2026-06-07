"""Analyze API — mirrors hei-gin plugins/plugin-sys/analyze/api/v1/api.go 1:1."""

from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session
from core.result import success
from core.db import get_db
from ...service import AnalyzeService

router = APIRouter()


@router.get("/api/v1/sys/analyze/dashboard", summary="获取仪表盘数据")
async def dashboard(request: Request, db: Session = Depends(get_db)):
    return success(AnalyzeService(db).dashboard())

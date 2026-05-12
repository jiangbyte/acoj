from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session
from core.db import get_db
from core.result import Result, success
from ...service import AnalyzeService

router = APIRouter()


@router.get("/api/v1/sys/analyze/dashboard", summary="获取仪表盘统计数据", response_model=Result)
async def dashboard(request: Request, db: Session = Depends(get_db)):
    service = AnalyzeService(db)
    return success(service.dashboard().model_dump())

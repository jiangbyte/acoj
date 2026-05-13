from typing import Generic, TypeVar, Optional, List, Any, Dict
from pydantic import BaseModel, Field
from core.enums import PageDataField
from core.utils.trace_utils import get_trace_id


T = TypeVar('T')


def success(data: Optional[T] = None) -> Dict[str, Any]:
    return {
        "code": 200,
        "message": "请求成功",
        "data": data,
        "success": True,
        "traceId": get_trace_id()
    }


def failure(message: str = "请求参数格式错误", code: int = 400, data: Optional[T] = None) -> Dict[str, Any]:
    return {
        "code": code,
        "message": message,
        "data": data,
        "success": False,
        "traceId": get_trace_id()
    }


class Result(BaseModel, Generic[T]):
    code: int = Field(default=200, description="状态码")
    message: str = Field(default="请求成功", description="消息")
    data: Optional[T] = Field(default=None, description="数据")
    success: bool = Field(default=True, description="是否成功")
    traceId: str = Field(default="", description="跟踪ID")


class PageData(BaseModel, Generic[T]):
    records: List[Any] = Field(default_factory=list, description="数据列表")
    total: int = Field(default=0, description="总数")
    page: int = Field(default=1, description="当前页")
    size: int = Field(default=10, description="每页大小")
    pages: int = Field(default=0, description="总页数")


def page_data(records: List[T], total: int, page: int, size: int) -> Dict[str, Any]:
    pages = (total + size - 1) // size if size > 0 else 0
    return {
        PageDataField.RECORDS: records,
        PageDataField.TOTAL: total,
        PageDataField.PAGE: page,
        PageDataField.SIZE: size,
        PageDataField.PAGES: pages
    }

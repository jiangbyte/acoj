from typing import Optional, List
from pydantic import BaseModel


class PageBounds(BaseModel):
    current: int = 1
    size: int = 10

    @property
    def offset(self) -> int:
        return (self.current - 1) * self.size


class IdParam(BaseModel):
    id: str


class IdsParam(BaseModel):
    ids: List[str]

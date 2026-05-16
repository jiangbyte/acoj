from typing import List
from pydantic import BaseModel


class IdParam(BaseModel):
    id: str


class IdsParam(BaseModel):
    ids: List[str]

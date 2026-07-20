from pydantic import Field

from app.core.schema.base import ApiSchema


class OjPortalSubmitRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    language_id: str = Field(min_length=1, max_length=64)
    source: str = Field(min_length=1, max_length=65536)


class OjPortalSubmitResponse(ApiSchema):
    submission_id: str
    status: str

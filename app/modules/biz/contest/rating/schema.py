"""Rating settlement schemas."""

from datetime import datetime

from app.core.schema.base import ApiSchema


class OjContestRatingSchema(ApiSchema):
    id: str
    contest_id: str
    account_id: str
    participation_id: str
    rank: int
    rating: int
    delta: int
    performance: int
    rated_at: datetime
    created_at: datetime | None = None
    created_by: str | None = None
    updated_at: datetime | None = None
    updated_by: str | None = None


class OjContestRateResultSchema(ApiSchema):
    contest_id: str
    rated: int
    contests_rerated: int = 0
    ratings_cleared: int = 0


class OjContestUndoRateResultSchema(ApiSchema):
    contest_id: str
    deleted: int

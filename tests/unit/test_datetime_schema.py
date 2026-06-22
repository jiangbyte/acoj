from datetime import UTC, datetime, timedelta

import pytest

from app.core.datetime import format_utc_iso8601
from app.core.schema.base import ApiSchema, to_schema


class SampleSchema(ApiSchema):
    created_at: datetime


def test_schema_normalizes_datetime_to_utc():
    value = datetime(2026, 6, 17, 16, 0, 0, tzinfo=UTC) + timedelta(hours=8)
    schema = SampleSchema(created_at=value)
    assert schema.created_at.tzinfo is not None
    assert format_utc_iso8601(schema.created_at).endswith("Z")


def test_schema_rejects_naive_datetime():
    with pytest.raises(ValueError):
        SampleSchema(created_at=datetime(2026, 6, 17, 12, 0, 0))


class SampleOrm:
    def __init__(self):
        self.created_at = datetime(2026, 6, 17, 12, 0, 0)


def test_to_schema_assumes_orm_naive_datetime_is_utc():
    schema = to_schema(SampleSchema, SampleOrm())

    assert schema.created_at.tzinfo is UTC
    assert format_utc_iso8601(schema.created_at) == "2026-06-17T12:00:00Z"

from datetime import UTC, datetime, timedelta

from sqlalchemy import event

from app.core.config.enums import StatusEnum
from app.core.schema.base import IdQuery
from app.modules.banner.model import SysBanner
from app.modules.banner.schema import (
    BannerCreateRequest,
    BannerPublicListQuery,
)
from app.modules.banner.service import BannerService, flush_interaction_deltas
from app.platform.cache.keys import banner_interaction_delta_key


class FakeRedis:
    def __init__(self) -> None:
        self.hashes: dict[str, dict[str, str]] = {}

    async def hincrby(self, key: str, field: str, amount: int) -> int:
        current = int(self.hashes.setdefault(key, {}).get(field, "0"))
        next_value = current + amount
        self.hashes[key][field] = str(next_value)
        return next_value

    async def hgetall(self, key: str):
        return dict(self.hashes.get(key, {}))

    async def hdel(self, key: str, *fields: str) -> int:
        removed = 0
        for field in fields:
            if field in self.hashes.get(key, {}):
                removed += 1
                del self.hashes[key][field]
        return removed


def _banner_create_request(**overrides) -> BannerCreateRequest:
    data = {
        "title": "Home Banner",
        "image": "https://example.com/banner.png",
        "url": "https://example.com",
        "category": "home",
        "type": "carousel",
        "position": "home_top",
        "display_scope": "PORTAL",
        "sort": 10,
        "status": StatusEnum.ENABLED,
    }
    data.update(overrides)
    return BannerCreateRequest(**data)


async def test_public_banner_filters_time_status_scope_and_sorts(db_session):
    service = BannerService(db_session)
    now = datetime.now(UTC)
    visible_late = await service.create(_banner_create_request(title="B", sort=20))
    visible_first = await service.create(_banner_create_request(title="A", sort=1))
    await service.create(_banner_create_request(title="Admin", display_scope="ADMIN"))
    await service.create(_banner_create_request(title="Disabled", status=StatusEnum.DISABLED))
    await service.create(_banner_create_request(title="Future", start_at=now + timedelta(days=1)))
    await service.create(_banner_create_request(title="Expired", end_at=now - timedelta(days=1)))

    items = await service.list_public(BannerPublicListQuery(position="home_top"))

    assert [item.id for item in items] == [visible_first.id, visible_late.id]


async def test_record_interaction_writes_redis_delta(db_session, monkeypatch):
    fake_redis = FakeRedis()
    monkeypatch.setattr("app.modules.banner.service.get_redis", lambda: fake_redis)

    banner = await BannerService(db_session).create(_banner_create_request())
    await BannerService(db_session).record_interaction(IdQuery(id=banner.id))

    assert fake_redis.hashes[banner_interaction_delta_key()][banner.id] == "1"


async def test_flush_interaction_deltas_accumulates_and_clears(db_session):
    fake_redis = FakeRedis()
    banner = SysBanner(
        title="Flush",
        image="https://example.com/flush.png",
        category="home",
        type="carousel",
        position="home_top",
        display_scope="PORTAL",
        interaction_count=2,
    )
    db_session.add(banner)
    await db_session.commit()
    fake_redis.hashes[banner_interaction_delta_key()] = {banner.id: "3"}

    flushed = await flush_interaction_deltas(db_session, fake_redis)
    await db_session.refresh(banner)

    assert flushed == 1
    assert banner.interaction_count == 5
    assert fake_redis.hashes[banner_interaction_delta_key()] == {}


async def test_flush_interaction_deltas_updates_multiple_banners_in_one_statement(db_session):
    fake_redis = FakeRedis()
    first = SysBanner(
        title="Flush A",
        image="https://example.com/a.png",
        category="home",
        type="carousel",
        position="home_top",
        display_scope="PORTAL",
        interaction_count=1,
    )
    second = SysBanner(
        title="Flush B",
        image="https://example.com/b.png",
        category="home",
        type="carousel",
        position="home_top",
        display_scope="PORTAL",
        interaction_count=10,
    )
    db_session.add_all([first, second])
    await db_session.commit()
    fake_redis.hashes[banner_interaction_delta_key()] = {
        first.id: "2",
        second.id: "4",
    }
    update_statements: list[str] = []

    def capture_update(conn, cursor, statement, parameters, context, executemany):
        if statement.lstrip().upper().startswith("UPDATE SYS_BANNER"):
            update_statements.append(statement)

    sync_engine = db_session.bind.sync_engine
    event.listen(sync_engine, "before_cursor_execute", capture_update)
    try:
        flushed = await flush_interaction_deltas(db_session, fake_redis)
    finally:
        event.remove(sync_engine, "before_cursor_execute", capture_update)
    await db_session.refresh(first)
    await db_session.refresh(second)

    assert flushed == 2
    assert first.interaction_count == 3
    assert second.interaction_count == 14
    assert len(update_statements) == 1

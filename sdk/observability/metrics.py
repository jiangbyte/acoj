from __future__ import annotations

from starlette.responses import Response

from sdk.infra.db.mysql import engine
from sdk.infra.db.redis import get_client as get_redis

try:
    from prometheus_client import CONTENT_TYPE_LATEST, Counter, Gauge, Histogram, generate_latest
except ModuleNotFoundError:  # pragma: no cover - local env may not have project deps installed
    CONTENT_TYPE_LATEST = "text/plain; version=0.0.4; charset=utf-8"

    class _Metric:
        def __init__(self, name: str, *_args, **_kwargs) -> None:
            self.name = name
            self.value = 0.0
            self.children: dict[tuple[str, ...], _Metric] = {}

        def labels(self, *values: str) -> "_Metric":
            key = tuple(values)
            if key not in self.children:
                self.children[key] = _Metric(self.name)
            return self.children[key]

        def inc(self, amount: float = 1.0) -> None:
            self.value += amount

        def dec(self, amount: float = 1.0) -> None:
            self.value -= amount

        def set(self, value: float) -> None:
            self.value = value

        def observe(self, amount: float) -> None:
            self.value += amount

    Counter = Gauge = Histogram = _Metric

    def generate_latest() -> bytes:
        lines: list[str] = []
        for metric in _ALL_METRICS:
            lines.append(f"# TYPE {metric.name} gauge")
            lines.append(f"{metric.name} {metric.value}")
            for labels, child in metric.children.items():
                label_text = ",".join(f'label{index}="{value}"' for index, value in enumerate(labels))
                lines.append(f"{metric.name}{{{label_text}}} {child.value}")
        return ("\n".join(lines) + "\n").encode("utf-8")


http_requests_total = Counter(
    "hei_http_requests_total",
    "Total HTTP requests processed.",
    ("method", "route", "status"),
)
http_request_duration = Histogram(
    "hei_http_request_duration_seconds",
    "HTTP request duration in seconds.",
    ("method", "route", "status"),
)
http_inflight_requests = Gauge(
    "hei_http_inflight_requests",
    "Current number of in-flight HTTP requests.",
)
http_panics_total = Counter(
    "hei_http_panics_total",
    "Total number of recovered HTTP panics.",
)
ws_connections_total = Counter(
    "hei_ws_connections_total",
    "Total accepted WebSocket connections.",
)
ws_rejected_connections_total = Counter(
    "hei_ws_rejected_connections_total",
    "Total rejected WebSocket connections.",
)
ws_disconnected_total = Counter(
    "hei_ws_disconnections_total",
    "Total disconnected WebSocket connections.",
)
ws_messages_sent_total = Counter(
    "hei_ws_messages_sent_total",
    "Total WebSocket messages sent.",
    ("channel",),
)
ws_current_connections = Gauge(
    "hei_ws_current_connections",
    "Current number of WebSocket connections.",
)

db_open_connections = Gauge("hei_db_open_connections", "Current number of open database connections.")
db_in_use_connections = Gauge("hei_db_in_use_connections", "Current number of in-use database connections.")
db_idle_connections = Gauge("hei_db_idle_connections", "Current number of idle database connections.")
db_wait_count_total = Gauge("hei_db_wait_count_total", "Total database connection wait count.")
db_wait_duration_seconds = Gauge("hei_db_wait_duration_seconds", "Total time blocked waiting for a new database connection.")

redis_pool_hits = Gauge("hei_redis_pool_hits", "Redis connection pool hits.")
redis_pool_misses = Gauge("hei_redis_pool_misses", "Redis connection pool misses.")
redis_pool_timeouts = Gauge("hei_redis_pool_timeouts", "Redis connection pool timeouts.")
redis_total_connections = Gauge("hei_redis_total_connections", "Current total Redis pool connections.")
redis_idle_connections = Gauge("hei_redis_idle_connections", "Current idle Redis pool connections.")
redis_stale_connections = Gauge("hei_redis_stale_connections", "Current stale Redis pool connections.")
log_queue_size = Gauge("hei_log_queue_size", "Current async log queue size.")
log_queue_dropped_total = Gauge("hei_log_queue_dropped_total", "Total dropped async log entries.")

_ALL_METRICS = [
    http_requests_total,
    http_request_duration,
    http_inflight_requests,
    http_panics_total,
    ws_connections_total,
    ws_rejected_connections_total,
    ws_disconnected_total,
    ws_messages_sent_total,
    ws_current_connections,
    db_open_connections,
    db_in_use_connections,
    db_idle_connections,
    db_wait_count_total,
    db_wait_duration_seconds,
    redis_pool_hits,
    redis_pool_misses,
    redis_pool_timeouts,
    redis_total_connections,
    redis_idle_connections,
    redis_stale_connections,
    log_queue_size,
    log_queue_dropped_total,
]


def handler() -> Response:
    _update_db_metrics()
    _update_redis_metrics()
    _update_log_metrics()
    return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)


def observe_http_request(method: str, route: str, status: int, seconds: float) -> None:
    status_text = str(status)
    http_requests_total.labels(method, route, status_text).inc()
    http_request_duration.labels(method, route, status_text).observe(seconds)


def inc_http_inflight() -> None:
    http_inflight_requests.inc()


def dec_http_inflight() -> None:
    http_inflight_requests.dec()


def inc_http_panic() -> None:
    http_panics_total.inc()


def inc_ws_connection() -> None:
    ws_connections_total.inc()
    ws_current_connections.inc()


def dec_ws_connection() -> None:
    ws_disconnected_total.inc()
    ws_current_connections.dec()


def inc_ws_rejected() -> None:
    ws_rejected_connections_total.inc()


def observe_ws_message(channel: str, count: int) -> None:
    if count <= 0:
        return
    ws_messages_sent_total.labels(channel).inc(count)


def snapshot() -> dict[str, object]:
    _update_db_metrics()
    _update_redis_metrics()
    _update_log_metrics()
    return {
        "metrics": generate_latest().decode("utf-8"),
    }


def _update_log_metrics() -> None:
    try:
        from sdk.log import log_persister_snapshot

        data = log_persister_snapshot()
        log_queue_size.set(_safe_int(data.get("queue_size", 0)))
        log_queue_dropped_total.set(_safe_int(data.get("dropped", 0)))
    except Exception:
        log_queue_size.set(0)
        log_queue_dropped_total.set(0)


def _update_db_metrics() -> None:
    pool = engine.pool
    checked_in = _safe_int(getattr(pool, "checkedin", lambda: 0)())
    checked_out = _safe_int(getattr(pool, "checkedout", lambda: 0)())
    overflow = _safe_int(getattr(pool, "overflow", lambda: 0)())
    db_open_connections.set(max(0, checked_in + checked_out))
    db_in_use_connections.set(max(0, checked_out))
    db_idle_connections.set(max(0, checked_in))
    db_wait_count_total.set(0)
    db_wait_duration_seconds.set(0)
    if overflow > 0:
        db_open_connections.set(max(checked_in + checked_out, checked_in + checked_out + overflow))


def _update_redis_metrics() -> None:
    client = get_redis()
    if client is None:
        redis_pool_hits.set(0)
        redis_pool_misses.set(0)
        redis_pool_timeouts.set(0)
        redis_total_connections.set(0)
        redis_idle_connections.set(0)
        redis_stale_connections.set(0)
        return

    pool = getattr(client, "connection_pool", None)
    available = _safe_len(getattr(pool, "_available_connections", []))
    in_use = _safe_len(getattr(pool, "_in_use_connections", []))
    redis_pool_hits.set(0)
    redis_pool_misses.set(0)
    redis_pool_timeouts.set(0)
    redis_total_connections.set(available + in_use)
    redis_idle_connections.set(available)
    redis_stale_connections.set(0)


def _safe_len(value: object) -> int:
    try:
        return len(value)  # type: ignore[arg-type]
    except TypeError:
        return 0


def _safe_int(value: object) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0

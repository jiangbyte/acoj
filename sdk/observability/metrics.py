from __future__ import annotations

from collections import Counter
from threading import Lock


_lock = Lock()
_http_inflight = 0
_http_requests_total: Counter[tuple[str, str, str]] = Counter()
_http_request_duration_seconds: Counter[tuple[str, str, str]] = Counter()


def observe_http_request(method: str, route: str, status: int, seconds: float) -> None:
    key = (method, route, str(status))
    with _lock:
        _http_requests_total[key] += 1
        _http_request_duration_seconds[key] += max(0.0, float(seconds))


def inc_http_inflight() -> None:
    global _http_inflight
    with _lock:
        _http_inflight += 1


def dec_http_inflight() -> None:
    global _http_inflight
    with _lock:
        _http_inflight = max(0, _http_inflight - 1)


def snapshot() -> dict[str, object]:
    with _lock:
        return {
            "http_inflight": _http_inflight,
            "http_requests_total": dict(_http_requests_total),
            "http_request_duration_seconds": dict(_http_request_duration_seconds),
        }
